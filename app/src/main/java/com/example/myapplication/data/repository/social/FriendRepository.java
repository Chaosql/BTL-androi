package com.example.myapplication.data.repository.social;

import android.content.Context;
import com.example.myapplication.data.repository.BaseRepository;
import com.example.myapplication.data.remote.api.ApiService;
import com.example.myapplication.data.remote.dto.request.*;
import com.example.myapplication.data.remote.dto.response.*;
import java.util.List;
import okhttp3.MultipartBody;
import retrofit2.Call;

public final class FriendRepository extends BaseRepository {
    public FriendRepository(Context context) { super(context); }
    public FriendRepository(ApiService api) { super(api); }

    public Call<BaseResponse<List<UserSearchDto>>> searchUsers(String keyword) {
        return api.searchUsers(keyword);
    }

    public Call<BaseResponse<FriendActionResponse>> sendFriendRequest(String targetUserId) {
        return api.sendFriendRequest(targetUserId);
    }

    public Call<BaseResponse<FriendActionResponse>> acceptFriendRequest(String requestId) {
        return api.acceptFriendRequest(requestId);
    }

    public Call<BaseResponse<FriendActionResponse>> rejectFriendRequest(String requestId) {
        return api.rejectFriendRequest(requestId);
    }

    public Call<BaseResponse<PageResponse<FriendDto>>> getFriends(Integer page, Integer size, String sort) {
        return api.getFriends(page, size, sort);
    }

    public Call<BaseResponse<PageResponse<FriendDto>>> searchFriends(String keyword, Integer page, Integer size, String sort) {
        return api.searchFriends(keyword, page, size, sort);
    }

    public Call<BaseResponse<PageResponse<FriendDto>>> getPendingFriendRequests(Integer page, Integer size, String sort) {
        return api.getPendingFriendRequests(page, size, sort);
    }

    public Call<BaseResponse<FriendActionResponse>> unfriend(String friendId) {
        return api.unfriend(friendId);
    }

    public Call<BaseResponse<OnlineStatusDto>> getUserOnlineStatus(String userId) {
        return api.getUserOnlineStatus(userId);
    }
}
