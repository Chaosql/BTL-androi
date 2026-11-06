package com.example.myapplication.ui.friends;

import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.R;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.repository.profile.ProfileRepository;
import com.example.myapplication.data.repository.social.FriendRepository;
import com.example.myapplication.databinding.DialogUserInfoBinding;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;

public class UserInfoDialog extends BasePreviewDialog<DialogUserInfoBinding> {
    private final String userId;
    private String status;
    private boolean loaded;
    public UserInfoDialog(MainActivity activity) { this(activity, null, null); }
    public UserInfoDialog(MainActivity activity, String userId, String status) {
        super(activity, DialogUserInfoBinding::inflate); this.userId = userId; this.status = status;
    }
    @Override protected void bind(View content, AlertDialog dialog) {
        binding.tvDistance.setVisibility(View.GONE);
        binding.btnViewProfile.setVisibility(View.GONE);
        binding.btnAddFriend.setEnabled(false);
        if (userId == null || userId.isEmpty() || !new PreferenceManager(activity).isLogin()) return;
        FriendRepository friends = new FriendRepository(activity);
        request(new ProfileRepository(activity).getProfile(userId), response -> {
            if (response.data == null) {
                Toast.makeText(activity, R.string.profile_empty_response, Toast.LENGTH_LONG).show(); return;
            }
            com.example.myapplication.data.remote.dto.response.UserDto user = response.data;
            loaded = true;
            binding.tvUserName.setText(SocialUi.name(user.firstName, user.lastName));
            binding.tvStatusTag.setText(SocialUi.value(user.statusTag));
            binding.tvSchool.setText(SocialUi.value(user.university));
            binding.tvMajor.setText(SocialUi.value(user.majorName));
            SocialUi.avatar(binding.ivAvatar, user.avatar);
            action();
        }, failure -> Toast.makeText(activity, failure.message, Toast.LENGTH_LONG).show());
        request(friends.getUserOnlineStatus(userId), response -> {
            if (response.data == null || response.data.isOnline == null) return;
            boolean online = Boolean.TRUE.equals(response.data.isOnline);
            binding.tvOnlineStatus.setVisibility(View.VISIBLE);
            binding.tvOnlineStatus.setText(online ? R.string.social_online : R.string.social_offline);
            binding.viewOnlineBadge.setVisibility(online ? View.VISIBLE : View.GONE);
        }, failure -> { });
        binding.btnAddFriend.setOnClickListener(view -> {
            if (!binding.btnAddFriend.isEnabled()) return;
            binding.btnAddFriend.setEnabled(false);
            request(friends.sendFriendRequest(userId), response -> {
                status = "PENDING";
                action();
                SocialUi.updated(activity);
                Toast.makeText(activity, R.string.social_request_sent, Toast.LENGTH_SHORT).show();
            }, failure -> {
                action();
                Toast.makeText(activity, failure.message, Toast.LENGTH_LONG).show();
            });
        });
    }
    private void action() {
        boolean friend = SocialUi.friend(status);
        boolean pending = SocialUi.pending(status);
        binding.btnAddFriend.setText(friend ? R.string.social_friend : pending ? R.string.social_sent : R.string.social_add);
        binding.btnAddFriend.setEnabled(loaded && !friend && !pending && !userId.equals(new PreferenceManager(activity).getUserId()));
    }
}
