package com.example.myapplication.ui.admin;

import android.app.Application;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.request.AccountStatusRequest;
import com.example.myapplication.data.remote.dto.response.*;
import com.example.myapplication.data.repository.admin.AdminRepository;
import com.example.myapplication.ui.base.*;

public final class AdminViewModel extends BaseViewModel {
    private final AdminRepository repository;
    private final PreferenceManager preferences;
    public AdminViewModel(Application application) {
        super(application); repository = new AdminRepository(application); preferences = new PreferenceManager(application);
    }
    private void requireAdmin() {
        if (!preferences.isAdmin()) throw new IllegalStateException("Admin access required");
    }
    public UiOperation<BaseResponse<PageResponse<AdminUserDto>>> getAdminUsers(int page, int size) {
        requireAdmin();
        return execute(repository.getUsers(page, size), response -> {
            PageResponse<AdminUserDto> data = response.data;
            if (data == null || data.content == null || data.meta == null)
                throw new IllegalArgumentException(getApplication().getString(com.example.myapplication.R.string.admin_list_unavailable));
            if (data.meta.totalPages == null || data.meta.totalPages < 0)
                throw new IllegalArgumentException(getApplication().getString(com.example.myapplication.R.string.admin_list_unavailable));
            data.totalPages = data.meta.totalPages;
            data.totalElements = data.meta.totalElements;
            data.last = page + 1 >= data.totalPages;
            data.first = page == 0;
            data.empty = data.content.isEmpty();
            return response;
        });
    }
    public UiOperation<BaseResponse<AdminUserDto>> getAdminUser(String id) {
        requireAdmin();
        return execute(repository.getUser(id), response -> {
            if (response.data == null || !id.equals(response.data.id)) throw new IllegalArgumentException();
            return response;
        });
    }
    public UiOperation<BaseResponse<Object>> lockAdminUser(String id, AccountStatusRequest request) { return change(id, request, false); }
    public UiOperation<BaseResponse<Object>> unlockAdminUser(String id, AccountStatusRequest request) { return change(id, request, true); }
    private UiOperation<BaseResponse<Object>> change(String id, AccountStatusRequest request, boolean unlock) {
        requireAdmin();
        if (id.equals(preferences.getUserId())) throw new IllegalArgumentException("Cannot lock your own account");
        return execute(unlock ? repository.unlock(id, request) : repository.lock(id, request));
    }
    private PagedList<AdminUserDto> users;
    public PagedList<AdminUserDto> users() {
        if (users == null) users = new PagedList<>(this, (keyword, page) -> getAdminUsers(page, 20), item -> item.id);
        return users;
    }
}
