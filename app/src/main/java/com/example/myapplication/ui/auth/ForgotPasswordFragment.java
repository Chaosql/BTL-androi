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
        bindFlow(binding.tvConfirm);
        enableScreenCatalog(binding.header, binding.ivPic);
        binding.btnBack.setOnClickListener(view -> back());
        toggle(binding.ivToggleConfirmPassword, binding.edtConfirmPassword);
        toggle(binding.ivTogglePassword, binding.edtNewPassword);
        binding.tvConfirm.setOnClickListener(view -> {
            if (email().isEmpty()) { go(UiScreen.INPUT_EMAIL); return; }
            if (!binding.edtNewPassword.getText().toString().equals(binding.edtConfirmPassword.getText().toString())) {
                binding.edtConfirmPassword.setError(getString(R.string.api_password_mismatch)); return;
            }
            if (!AuthViewModel.validResetPassword(binding.edtNewPassword.getText().toString())) {
                binding.edtNewPassword.setError(getString(R.string.reset_password_policy)); return;
            }
            ResetPasswordRequest request = new ResetPasswordRequest(email(), binding.edtNewPassword.getText().toString(),
                    binding.edtConfirmPassword.getText().toString());
            authModel().resetAccountPassword(request);
        });
    }
}
