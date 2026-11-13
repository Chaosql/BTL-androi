package com.example.myapplication.data.repository.admin;

import android.content.Context;
import com.example.myapplication.data.repository.BaseRepository;
import com.example.myapplication.data.remote.dto.response.*;
import com.example.myapplication.data.remote.dto.request.AccountStatusRequest;
import retrofit2.Call;

public final class AdminRepository extends BaseRepository {
    public AdminRepository(Context context) { super(context); }
    public Call<BaseResponse<PageResponse<AdminUserDto>>> getUsers(int page, int size) { return api.getAdminUsers(page + 1, size); }
    public Call<BaseResponse<AdminUserDto>> getUser(String id) { return api.getAdminUser(id); }
    public Call<BaseResponse<Object>> lock(String id, AccountStatusRequest request) { return api.lockAdminUser(id, request); }
    public Call<BaseResponse<Object>> unlock(String id, AccountStatusRequest request) { return api.unlockAdminUser(id, request); }
}
