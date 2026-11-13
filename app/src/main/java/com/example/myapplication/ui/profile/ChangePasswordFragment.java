package com.example.myapplication.ui.profile;

import com.example.myapplication.databinding.FragmentChangePasswordBinding;
import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.main.UiScreen;

public class ChangePasswordFragment extends BaseFragment<FragmentChangePasswordBinding> {
    public ChangePasswordFragment() { super(FragmentChangePasswordBinding::inflate); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        binding.btnBack.setOnClickListener(view -> back());
        ProfileUi.clearOnEdit(binding.etOldPassword, binding.tvOldPasswordError, binding.tvFormError);
        ProfileUi.clearOnEdit(binding.etNewPassword, binding.tvNewPasswordError, binding.tvConfirmPasswordError, binding.tvFormError);
        ProfileUi.clearOnEdit(binding.etConfirmPassword, binding.tvConfirmPasswordError, binding.tvFormError);
        binding.tvForgotPassword.setOnClickListener(bindingView -> go(UiScreen.INPUT_EMAIL));
        binding.btnOk.setOnClickListener(bindingView -> {
            String userId = new com.example.myapplication.data.local.PreferenceManager(requireContext()).getUserId();
            if (userId.isEmpty()) { go(UiScreen.LOGIN); return; }
            String oldPassword = binding.etOldPassword.getText().toString();
            String password = binding.etNewPassword.getText().toString();
            String confirm = binding.etConfirmPassword.getText().toString();
            ProfileUi.clearErrors(binding.tvOldPasswordError, binding.tvNewPasswordError,
                    binding.tvConfirmPasswordError, binding.tvFormError);
            if (oldPassword.isEmpty()) {
                ProfileUi.showError(binding.tvOldPasswordError, getString(R.string.api_required));
                binding.etOldPassword.requestFocus(); return;
            }
            if (password.isEmpty()) {
                ProfileUi.showError(binding.tvNewPasswordError, getString(R.string.api_required));
                binding.etNewPassword.requestFocus(); return;
            }
            if (!com.example.myapplication.ui.auth.AuthViewModel.validPassword(password)) {
                ProfileUi.showError(binding.tvNewPasswordError, getString(R.string.password_policy));
                binding.etNewPassword.requestFocus(); return;
            }
            if (confirm.isEmpty() || !password.equals(confirm)) {
                ProfileUi.showError(binding.tvConfirmPasswordError, getString(confirm.isEmpty()
                        ? R.string.api_required : R.string.api_password_mismatch));
                binding.etConfirmPassword.requestFocus(); return;
            }
            binding.btnOk.setEnabled(false);
            ProfileUi.inputs(binding.getRoot(), false);
            request(viewModel(com.example.myapplication.ui.profile.ProfileViewModel.class)
                    .changePassword(userId, new com.example.myapplication.data.remote.dto.request.ChangePasswordRequest(oldPassword, password, confirm)),
                    response -> {
                        binding.btnOk.setEnabled(true);
                        ProfileUi.inputs(binding.getRoot(), true);
                        binding.etOldPassword.setText(""); binding.etNewPassword.setText(""); binding.etConfirmPassword.setText("");
                        android.widget.Toast.makeText(requireContext(), R.string.api_password_changed, android.widget.Toast.LENGTH_LONG).show();
                    }, error -> {
                        binding.btnOk.setEnabled(true);
                        ProfileUi.inputs(binding.getRoot(), true);
                        ProfileUi.showError(binding.tvFormError, error.message);
                    });
        });
    }
}
