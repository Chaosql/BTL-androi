package com.example.myapplication.ui.friends;

import android.app.Application;
import com.example.myapplication.ui.base.BaseViewModel;
import com.example.myapplication.ui.base.UiOperation;
import com.example.myapplication.data.repository.social.FriendRepository;
import com.example.myapplication.data.remote.dto.request.*;
import com.example.myapplication.data.remote.dto.response.*;
import java.util.List;
import okhttp3.MultipartBody;

public final class FriendViewModel extends BaseViewModel {
    private final FriendRepository repository;

    public FriendViewModel(Application application) {
        super(application);
        repository = new FriendRepository(application);
    }

    public UiOperation<BaseResponse<List<UserSearchDto>>> searchUsers(String keyword) {
        return execute(repository.searchUsers(keyword), response -> { if (response.data != null) response.data = visibleUsers(response.data); return response; });
    }

    public UiOperation<BaseResponse<FriendActionResponse>> sendFriendRequest(String targetUserId) {
        return execute(repository.sendFriendRequest(targetUserId));
    }

    public UiOperation<BaseResponse<FriendActionResponse>> acceptFriendRequest(String requestId) {
        return execute(repository.acceptFriendRequest(requestId));
    }

    public UiOperation<BaseResponse<FriendActionResponse>> rejectFriendRequest(String requestId) {
        return execute(repository.rejectFriendRequest(requestId));
    }

    public UiOperation<BaseResponse<PageResponse<FriendDto>>> getFriends(Integer page, Integer size, String sort) {
        return execute(repository.getFriends(page, size, sort));
    }

    public UiOperation<BaseResponse<PageResponse<FriendDto>>> searchFriends(String keyword, Integer page, Integer size, String sort) {
        return execute(repository.searchFriends(keyword, page, size, sort));
    }

    public UiOperation<BaseResponse<PageResponse<FriendDto>>> getPendingFriendRequests(Integer page, Integer size, String sort) {
        return execute(repository.getPendingFriendRequests(page, size, sort));
    }

    public UiOperation<BaseResponse<FriendActionResponse>> unfriend(String friendId) {
        return execute(repository.unfriend(friendId));
    }

    public UiOperation<BaseResponse<OnlineStatusDto>> getUserOnlineStatus(String userId) {
        return execute(repository.getUserOnlineStatus(userId));
    }
    private com.example.myapplication.ui.base.PagedList<FriendDto> friendsPage, pendingPage;
    public com.example.myapplication.ui.base.PagedList<FriendDto> friendsPage() {
        if (friendsPage == null) friendsPage = new com.example.myapplication.ui.base.PagedList<>(this,
                (keyword, page) -> keyword.isEmpty() ? getFriends(page, 20, null) : searchFriends(keyword, page, 20, null), item -> item.userId);
        return friendsPage;
    }
    public com.example.myapplication.ui.base.PagedList<FriendDto> pendingPage() {
        if (pendingPage == null) pendingPage = new com.example.myapplication.ui.base.PagedList<>(this,
                (keyword, page) -> getPendingFriendRequests(page, 20, null), item -> item.requestId);
        return pendingPage;
    }
    private final java.util.Set<String> sent = new java.util.HashSet<>();
    public boolean sent(String id) { return sent.contains(id); }
    public void rememberSent(String id) { sent.add(id); }
    public List<UserSearchDto> visibleUsers(List<UserSearchDto> data) {
        String self = new com.example.myapplication.data.local.PreferenceManager(getApplication()).getUserId();
        List<UserSearchDto> users = new java.util.ArrayList<>();
        for (UserSearchDto item : data) {
            if (item == null || item.id == null || self.equals(item.id)) continue;
            if (sent.contains(item.id) && !SocialUi.friend(item.friendshipStatus)) item.friendshipStatus = "PENDING";
            users.add(item);
        }
        return users;
    }
}
