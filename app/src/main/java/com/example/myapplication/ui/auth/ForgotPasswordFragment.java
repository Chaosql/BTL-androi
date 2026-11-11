package com.example.myapplication.ui.auth;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentForgotPasswordBinding;
import com.example.myapplication.ui.main.UiScreen;
import com.example.myapplication.data.remote.dto.request.ResetPasswordRequest;

public class ForgotPasswordFragment extends BaseAuthFragment<FragmentForgotPasswordBinding> {
    public ForgotPasswordFragment() { super(FragmentForgotPasswordBinding::inflate); }
    @Override protected void onScreenCreated(View root, Bundle state) {
        bindErrors(binding.tvPasswordError, binding.edtNewPassword, binding.edtConfirmPassword);
        bindFlow(binding.tvConfirm);
        enableScreenCatalog(binding.header, binding.ivPic);
        Runnable artwork = passwordArtwork(binding.ivPic, binding.edtNewPassword, binding.edtConfirmPassword);
        toggle(binding.ivTogglePassword, binding.edtNewPassword, artwork);
        toggle(binding.ivToggleConfirmPassword, binding.edtConfirmPassword, artwork);
        binding.btnBack.setOnClickListener(view -> back());
        binding.tvConfirm.setOnClickListener(view -> {
            clearError();
            if (email().isEmpty()) { go(UiScreen.INPUT_EMAIL); return; }
            if (!required(binding.edtNewPassword)) return;
            if (binding.edtConfirmPassword.length() == 0) { fieldError(binding.edtConfirmPassword, R.string.auth_confirm_required); return; }
            if (!binding.edtNewPassword.getText().toString().equals(binding.edtConfirmPassword.getText().toString())) {
                fieldError(binding.edtConfirmPassword, R.string.api_password_mismatch); return;
            }
            if (!AuthViewModel.validResetPassword(binding.edtNewPassword.getText().toString())) {
                fieldError(binding.edtNewPassword, R.string.reset_password_policy); return;
            }
            ResetPasswordRequest request = new ResetPasswordRequest(email(), binding.edtNewPassword.getText().toString(),
                    binding.edtConfirmPassword.getText().toString());
            authModel().resetAccountPassword(request);
        });
    }
}
