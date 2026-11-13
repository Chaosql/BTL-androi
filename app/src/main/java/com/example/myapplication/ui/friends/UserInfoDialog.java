package com.example.myapplication.ui.friends;

import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.R;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.ui.profile.ProfileViewModel;
import com.example.myapplication.ui.friends.FriendViewModel;
import com.example.myapplication.databinding.DialogUserInfoBinding;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;

public class UserInfoDialog extends BasePreviewDialog<DialogUserInfoBinding> {
    private final String userId;
    private String status;
    private boolean loaded;
    private final Double distance;
    public UserInfoDialog(MainActivity activity) { this(activity, null, null); }
    public UserInfoDialog(MainActivity activity, String userId, String status) {
        this(activity, userId, status, null);
    }
    public UserInfoDialog(MainActivity activity, String userId, String status, Double distance) {
        super(activity, DialogUserInfoBinding::inflate); this.userId = userId; this.status = status; this.distance = distance;
    }
    @Override protected void bind(View content, AlertDialog dialog) {
        binding.tvDistance.setVisibility(distance != null && Double.isFinite(distance) && distance >= 0 ? View.VISIBLE : View.GONE);
        if (distance != null) binding.tvDistance.setText(activity.getString(com.example.myapplication.R.string.radar_distance, distance));
        binding.btnAddFriend.setEnabled(false);
        if (userId == null || userId.isEmpty() || !new PreferenceManager(activity).isLogin()) return;
        FriendViewModel friends = viewModel(com.example.myapplication.ui.friends.FriendViewModel.class);
        request(viewModel(com.example.myapplication.ui.profile.ProfileViewModel.class).getProfile(userId), response -> {
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
            if (SocialUi.friend(status)) {
                request(viewModel(com.example.myapplication.ui.chat.ConversationViewModel.class)
                        .createOrGetDirectConversation(new com.example.myapplication.data.remote.dto.request.DirectChatRequest(userId)), response -> {
                    if (response.data == null || response.data.id == null) {
                        action(); Toast.makeText(activity, R.string.chat_missing_data, Toast.LENGTH_LONG).show(); return;
                    }
                    String name = binding.tvUserName.getText().toString();
                    dialog.dismiss();
                    activity.showScreen(com.example.myapplication.ui.main.UiScreen.CHAT,
                            com.example.myapplication.ui.chat.ChatUi.args(response.data.id, name, userId));
                }, failure -> { action(); Toast.makeText(activity, failure.message, Toast.LENGTH_LONG).show(); });
                return;
            }
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
        binding.btnAddFriend.setText(friend ? R.string.chat_message : pending ? R.string.social_sent : R.string.social_add);
        binding.btnAddFriend.setEnabled(loaded && !pending && !userId.equals(new PreferenceManager(activity).getUserId()));
    }
}
