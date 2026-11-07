package com.example.myapplication.ui.friends;

import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.R;
import com.example.myapplication.databinding.DialogConfirmDeleteBinding;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import com.example.myapplication.ui.base.UiOperation;

public class ConfirmDeleteDialog extends BasePreviewDialog<DialogConfirmDeleteBinding> {
    private final String message;
    public enum Action { BLOCK, UNBLOCK, UNFRIEND }
    private final Action action;
    private final String userId;
    private boolean submitting;
    public ConfirmDeleteDialog(MainActivity activity) { this(activity, null, null, null); }
    public ConfirmDeleteDialog(MainActivity activity, String message, Action action, String userId) {
        super(activity, DialogConfirmDeleteBinding::inflate); this.message = message; this.action = action; this.userId = userId;
    }
    @Override protected void bind(View content, AlertDialog dialog) {
        binding.btnCancel.setOnClickListener(view -> dialog.dismiss());
        if (message != null) binding.tvMessage.setText(message);
        binding.btnConfirm.setEnabled(action != null && userId != null && !userId.isEmpty());
        binding.btnConfirm.setText(R.string.confirm);
        binding.btnConfirm.setOnClickListener(view -> {
            if (action == null || submitting) return;
            submitting = true;
            binding.btnConfirm.setEnabled(false);
            binding.btnCancel.setEnabled(false);
            dialog.setCancelable(false);
            UiOperation<?> operation;
            switch (action) {
                case BLOCK: operation = viewModel(BlockViewModel.class).blockUser(userId); break;
                case UNBLOCK: operation = viewModel(BlockViewModel.class).unblockUser(userId); break;
                default: operation = viewModel(FriendViewModel.class).unfriend(userId);
            }
            request(operation, response -> {
                SocialUi.updated(activity);
                Toast.makeText(activity, R.string.social_success, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }, failure -> {
                submitting = false;
                binding.btnConfirm.setEnabled(true);
                binding.btnCancel.setEnabled(true);
                dialog.setCancelable(true);
                Toast.makeText(activity, failure.message, Toast.LENGTH_LONG).show();
            });
        });
    }
}
