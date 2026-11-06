package com.example.myapplication.data.repository.social;

import android.content.Context;
import com.example.myapplication.data.repository.BaseRepository;
import com.example.myapplication.data.remote.api.ApiService;
import com.example.myapplication.data.remote.dto.request.*;
import com.example.myapplication.data.remote.dto.response.*;
import java.util.List;
import okhttp3.MultipartBody;
import retrofit2.Call;

public final class BlockRepository extends BaseRepository {
    public BlockRepository(Context context) { super(context); }
    public BlockRepository(ApiService api) { super(api); }

    public Call<BaseResponse<CommonResponse>> blockUser(String userId) {
        return api.blockUser(userId);
    }

    public Call<BaseResponse<CommonResponse>> unblockUser(String userId) {
        return api.unblockUser(userId);
    }

    public Call<BaseResponse<List<BlockedUserDto>>> getBlockedUsers() {
        return api.getBlockedUsers();
    }
}
