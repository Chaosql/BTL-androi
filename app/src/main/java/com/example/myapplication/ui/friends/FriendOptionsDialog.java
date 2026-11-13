package com.example.myapplication.ui.friends;

import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.R;
import com.example.myapplication.data.remote.dto.response.FriendDto;
import com.example.myapplication.databinding.DialogFriendOptionsBinding;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;

public class FriendOptionsDialog extends BasePreviewDialog<DialogFriendOptionsBinding> {
    private final FriendDto friend;
    private boolean blocked;
    public FriendOptionsDialog(MainActivity activity) { this(activity, null); }
    public FriendOptionsDialog(MainActivity activity, FriendDto friend) {
        super(activity, DialogFriendOptionsBinding::inflate); this.friend = friend;
    }
    @Override protected void bind(View content, AlertDialog dialog) {
        boolean valid = friend != null && friend.userId != null && !friend.userId.isEmpty();
        binding.tvOptionViewProfile.setEnabled(valid);
        binding.tvOptionBlock.setEnabled(false);
        if (valid) request(viewModel(BlockViewModel.class).getBlockedUsers(), response -> {
            if (response.data == null) {
                android.widget.Toast.makeText(activity, R.string.social_empty_response, android.widget.Toast.LENGTH_LONG).show();
                return;
            }
            blocked = false;
            for (com.example.myapplication.data.remote.dto.response.BlockedUserDto user : response.data) {
                if (user != null && friend.userId.equals(user.id)) { blocked = true; break; }
            }
            binding.tvOptionBlock.setText(blocked ? R.string.ui_item_blocked_user_text_1 : R.string.option_block);
            binding.tvOptionBlock.setEnabled(true);
        }, error -> android.widget.Toast.makeText(activity, error.message, android.widget.Toast.LENGTH_LONG).show());
        binding.tvOptionDelete.setEnabled(valid);
        binding.tvOptionViewProfile.setOnClickListener(view -> {
            if (!valid) return;
            dialog.dismiss();
            new UserInfoDialog(activity, friend.userId, "FRIEND").show();
        });
        binding.tvOptionBlock.setOnClickListener(view -> {
            if (!valid || !binding.tvOptionBlock.isEnabled()) return;
            dialog.dismiss();
            new ConfirmDeleteDialog(activity, activity.getString(blocked ? R.string.social_unblock_confirm : R.string.social_block_confirm,
                    SocialUi.name(friend.firstName, friend.lastName)),
                    blocked ? ConfirmDeleteDialog.Action.UNBLOCK : ConfirmDeleteDialog.Action.BLOCK, friend.userId).show();
        });
        binding.tvOptionDelete.setOnClickListener(view -> {
            if (!valid) return;
            dialog.dismiss();
            new ConfirmDeleteDialog(activity, activity.getString(R.string.social_unfriend_confirm,
                    SocialUi.name(friend.firstName, friend.lastName)),
                    ConfirmDeleteDialog.Action.UNFRIEND, friend.userId).show();
        });
    }
}
