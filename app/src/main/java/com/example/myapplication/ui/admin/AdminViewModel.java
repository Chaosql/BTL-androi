package com.example.myapplication.ui.admin;

import android.app.Application;
import com.example.myapplication.data.local.PreferenceManager;
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
        requireAdmin(); return execute(repository.getUsers(page, size));
    }
    public UiOperation<BaseResponse<AdminUserDto>> getAdminUser(String id) {
        requireAdmin();
        return execute(repository.getUser(id), response -> {
            if (response.data == null || !id.equals(response.data.id)) throw new IllegalArgumentException();
            return response;
        });
    }
    public UiOperation<BaseResponse<AdminUserDto>> lockAdminUser(String id) { return change(id, false); }
    public UiOperation<BaseResponse<AdminUserDto>> unlockAdminUser(String id) { return change(id, true); }
    private UiOperation<BaseResponse<AdminUserDto>> change(String id, boolean unlock) {
        requireAdmin();
        if (id.equals(preferences.getUserId())) throw new IllegalArgumentException("Cannot lock your own account");
        return execute(unlock ? repository.unlock(id) : repository.lock(id), response -> {
            if (response.data == null || !id.equals(response.data.id)
                    || !(unlock ? "ACTIVE" : "LOCKED").equals(response.data.status)) throw new IllegalArgumentException();
            users().replace(response.data);
            return response;
        });
    }
    private PagedList<AdminUserDto> users;
    public PagedList<AdminUserDto> users() {
        if (users == null) users = new PagedList<>(this, (keyword, page) -> getAdminUsers(page, 20), item -> item.id);
        return users;
    }
}
