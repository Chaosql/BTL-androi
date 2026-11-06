package com.example.myapplication.ui.auth;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentForgotPasswordBinding;
import com.example.myapplication.ui.main.UiScreen;
import android.widget.Toast;
import com.example.myapplication.data.remote.dto.request.ResetPasswordRequest;

public class ForgotPasswordFragment extends BaseAuthFragment<FragmentForgotPasswordBinding> {
    public ForgotPasswordFragment() { super(FragmentForgotPasswordBinding::inflate); }
    @Override protected void onScreenCreated(View root, Bundle state) {
        toggle(root, R.id.ivTogglePassword, R.id.edtNewPassword);
        binding.tvConfirm.setOnClickListener(view -> {
            if (email().isEmpty()) { go(UiScreen.INPUT_EMAIL); return; }
            if (!binding.edtNewPassword.getText().toString().equals(binding.edtConfirmPassword.getText().toString())) {
                binding.edtConfirmPassword.setError(getString(R.string.api_password_mismatch)); return;
            }
            if (!binding.edtNewPassword.getText().toString().matches("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
                binding.edtNewPassword.setError(getString(R.string.reset_password_policy)); return;
            }
            ResetPasswordRequest request = new ResetPasswordRequest(email(), binding.edtNewPassword.getText().toString(),
                    binding.edtConfirmPassword.getText().toString());
            submit(repository().resetPassword(request), binding.tvConfirm, response -> {
                Toast.makeText(requireContext(), R.string.api_reset_success, Toast.LENGTH_LONG).show();
                go(UiScreen.LOGIN);
            });
        });
    }
}
