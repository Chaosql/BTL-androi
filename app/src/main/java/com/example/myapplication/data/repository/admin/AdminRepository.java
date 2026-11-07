package com.example.myapplication.data.repository.admin;

import android.content.Context;
import com.example.myapplication.data.repository.BaseRepository;
import com.example.myapplication.data.remote.dto.response.*;
import retrofit2.Call;

public final class AdminRepository extends BaseRepository {
    public AdminRepository(Context context) { super(context); }
    public Call<BaseResponse<PageResponse<AdminUserDto>>> getUsers(int page, int size) { return api.getAdminUsers(page, size); }
    public Call<BaseResponse<AdminUserDto>> getUser(String id) { return api.getAdminUser(id); }
    public Call<BaseResponse<AdminUserDto>> lock(String id) { return api.lockAdminUser(id); }
    public Call<BaseResponse<AdminUserDto>> unlock(String id) { return api.unlockAdminUser(id); }
}
