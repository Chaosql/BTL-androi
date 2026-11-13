package com.example.myapplication.ui.admin;

import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.R;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.request.AccountStatusRequest;
import com.example.myapplication.data.remote.dto.response.AdminUserDto;
import com.example.myapplication.databinding.DialogAdminUserDetailBinding;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.friends.SocialUi;
import com.example.myapplication.ui.main.MainActivity;
import java.util.Locale;
import java.util.function.Consumer;

public final class AdminUserDetailDialog extends BasePreviewDialog<DialogAdminUserDetailBinding> {
    private AdminUserDto user;
    private final Consumer<AdminUserDto> changed;
    private boolean busy, awaitingRefresh, unlock;
    private AdminViewModel model;

    public AdminUserDetailDialog(MainActivity activity, AdminUserDto user, Consumer<AdminUserDto> changed) {
        super(activity, DialogAdminUserDetailBinding::inflate);
        this.user = user;
        this.changed = changed;
    }

    @Override protected void bind(View content, AlertDialog dialog) {
        model = viewModel(AdminViewModel.class);
        render();
        binding.btnClose.setOnClickListener(view -> { if (!busy) dialog.dismiss(); });
        binding.btnAccountStatus.setOnClickListener(view -> {
            if (busy || !canChange()) return;
            unlock = "LOCKED".equalsIgnoreCase(user.status);
            binding.btnConfirmAccountStatus.setText(unlock ? R.string.admin_unlock : R.string.admin_lock);
            binding.layoutAccountStatus.setVisibility(View.VISIBLE);
            binding.btnAccountStatus.setVisibility(View.GONE);
            binding.tvAccountStatusError.setVisibility(View.GONE);
        });
        binding.btnCancelAccountStatus.setOnClickListener(view -> {
            if (busy) return;
            binding.layoutAccountStatus.setVisibility(View.GONE);
            render();
        });
        binding.btnConfirmAccountStatus.setOnClickListener(view -> {
            if (busy || !canChange()) return;
            if (awaitingRefresh) { refresh(dialog); return; }
            String reason = binding.etAccountReason.getText().toString().trim();
            if (reason.isEmpty()) {
                error(activity.getString(R.string.admin_reason_required));
                binding.etAccountReason.requestFocus(); return;
            }
            AccountStatusRequest change = new AccountStatusRequest(reason, binding.etReportId.getText().toString());
            binding.tvAccountStatusError.setVisibility(View.GONE);
            setBusy(true, dialog);
            request(unlock ? model.unlockAdminUser(user.id, change) : model.lockAdminUser(user.id, change), response -> {
                awaitingRefresh = true;
                refresh(dialog);
            }, failure -> { setBusy(false, dialog); error(failure.message); });
        });
        android.text.TextWatcher edit = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence text, int start, int before, int count) { }
            @Override public void afterTextChanged(android.text.Editable text) {
                if (binding != null && !busy && !awaitingRefresh) binding.tvAccountStatusError.setVisibility(View.GONE);
            }
        };
        binding.etAccountReason.addTextChangedListener(edit);
        binding.etReportId.addTextChangedListener(edit);
    }

    private void refresh(AlertDialog dialog) {
        setBusy(true, dialog);
        request(model.getAdminUser(user.id), response -> {
            if (!(unlock ? "ACTIVE" : "LOCKED").equalsIgnoreCase(response.data.status)) {
                setBusy(false, dialog);
                error(activity.getString(R.string.admin_status_unconfirmed));
                return;
            }
            user = response.data;
            awaitingRefresh = false;
            setBusy(false, dialog);
            binding.layoutAccountStatus.setVisibility(View.GONE);
            binding.etAccountReason.setText(""); binding.etReportId.setText("");
            render(); changed.accept(user);
            android.widget.Toast.makeText(activity, unlock ? R.string.admin_unlocked_success : R.string.admin_locked_success,
                    android.widget.Toast.LENGTH_SHORT).show();
        }, failure -> { setBusy(false, dialog); error(failure.message); });
    }

    private void setBusy(boolean value, AlertDialog dialog) {
        busy = value;
        dialog.setCancelable(!value);
        binding.btnClose.setEnabled(!value);
        binding.btnConfirmAccountStatus.setEnabled(!value);
        binding.btnConfirmAccountStatus.setText(awaitingRefresh ? R.string.admin_reload : unlock ? R.string.admin_unlock : R.string.admin_lock);
        binding.btnCancelAccountStatus.setEnabled(!value && !awaitingRefresh);
        binding.etAccountReason.setEnabled(!value && !awaitingRefresh);
        binding.etReportId.setEnabled(!value && !awaitingRefresh);
    }
    private boolean canChange() {
        PreferenceManager preferences = new PreferenceManager(activity);
        return preferences.isAdmin() && user.id != null && !user.id.equals(preferences.getUserId())
                && ("ACTIVE".equalsIgnoreCase(user.status) || "LOCKED".equalsIgnoreCase(user.status));
    }
    private void render() {
        binding.tvUserName.setText(SocialUi.name(user.firstName, user.lastName));
        binding.tvUserDetails.setText(activity.getString(R.string.admin_detail,
                SocialUi.value(user.email), SocialUi.value(user.phone), SocialUi.value(user.university),
                SocialUi.value(user.majorName), SocialUi.value(user.role), activity.getString(statusLabel(user))));
        binding.btnAccountStatus.setText("LOCKED".equalsIgnoreCase(user.status) ? R.string.admin_unlock : R.string.admin_lock);
        binding.btnAccountStatus.setVisibility(canChange() ? View.VISIBLE : View.GONE);
    }
    private void error(String message) {
        binding.tvAccountStatusError.setText(message);
        binding.tvAccountStatusError.setVisibility(View.VISIBLE);
    }
    static int statusLabel(AdminUserDto user) {
        switch (SocialUi.value(user.status).toUpperCase(Locale.ROOT)) {
            case "ACTIVE": return R.string.admin_active;
            case "LOCKED": return R.string.admin_locked;
            case "UNVERIFIED": return R.string.admin_unverified;
            default: return R.string.admin_unknown;
        }
    }
}
