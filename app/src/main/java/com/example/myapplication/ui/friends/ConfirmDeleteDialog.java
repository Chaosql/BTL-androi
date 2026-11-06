package com.example.myapplication.ui.friends;

import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.R;
import com.example.myapplication.databinding.DialogConfirmDeleteBinding;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import java.util.function.Supplier;
import retrofit2.Call;

public class ConfirmDeleteDialog extends BasePreviewDialog<DialogConfirmDeleteBinding> {
    private final String message;
    private final Supplier<Call<?>> action;
    private boolean submitting;
    public ConfirmDeleteDialog(MainActivity activity) { this(activity, null, null); }
    public ConfirmDeleteDialog(MainActivity activity, String message, Supplier<Call<?>> action) {
        super(activity, DialogConfirmDeleteBinding::inflate); this.message = message; this.action = action;
    }
    @Override protected void bind(View content, AlertDialog dialog) {
        if (message != null) binding.tvMessage.setText(message);
        binding.btnConfirm.setEnabled(action != null);
        binding.btnConfirm.setText(R.string.confirm);
        binding.btnConfirm.setOnClickListener(view -> {
            if (action == null || submitting) return;
            submitting = true;
            binding.btnConfirm.setEnabled(false);
            binding.btnCancel.setEnabled(false);
            dialog.setCancelable(false);
            request(action.get(), response -> {
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
