package com.example.myapplication.ui.friends;

import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.R;
import com.example.myapplication.data.remote.dto.response.FriendDto;
import com.example.myapplication.data.repository.social.BlockRepository;
import com.example.myapplication.data.repository.social.FriendRepository;
import com.example.myapplication.databinding.DialogFriendOptionsBinding;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;

public class FriendOptionsDialog extends BasePreviewDialog<DialogFriendOptionsBinding> {
    private final FriendDto friend;
    public FriendOptionsDialog(MainActivity activity) { this(activity, null); }
    public FriendOptionsDialog(MainActivity activity, FriendDto friend) {
        super(activity, DialogFriendOptionsBinding::inflate); this.friend = friend;
    }
    @Override protected void bind(View content, AlertDialog dialog) {
        binding.tvOptionPin.setVisibility(View.GONE);
        binding.tvOptionCreateGroup.setVisibility(View.GONE);
        boolean valid = friend != null && friend.userId != null && !friend.userId.isEmpty();
        binding.tvOptionViewProfile.setEnabled(valid);
        binding.tvOptionBlock.setEnabled(valid);
        binding.tvOptionDelete.setEnabled(valid);
        binding.tvOptionViewProfile.setOnClickListener(view -> {
            if (!valid) return;
            dialog.dismiss();
            new UserInfoDialog(activity, friend.userId, "FRIEND").show();
        });
        binding.tvOptionBlock.setOnClickListener(view -> {
            if (!valid) return;
            dialog.dismiss();
            new ConfirmDeleteDialog(activity, activity.getString(R.string.social_block_confirm,
                    SocialUi.name(friend.firstName, friend.lastName)),
                    () -> new BlockRepository(activity).blockUser(friend.userId)).show();
        });
        binding.tvOptionDelete.setOnClickListener(view -> {
            if (!valid) return;
            dialog.dismiss();
            new ConfirmDeleteDialog(activity, activity.getString(R.string.social_unfriend_confirm,
                    SocialUi.name(friend.firstName, friend.lastName)),
                    () -> new FriendRepository(activity).unfriend(friend.userId)).show();
        });
    }
}
