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
        binding.tvForgotPassword.setOnClickListener(bindingView -> go(UiScreen.INPUT_EMAIL));
        binding.btnOk.setOnClickListener(bindingView -> {
            String userId = new com.example.myapplication.data.local.PreferenceManager(requireContext()).getUserId();
            if (userId.isEmpty()) { go(UiScreen.LOGIN); return; }
            String oldPassword = binding.etOldPassword.getText().toString();
            String password = binding.etNewPassword.getText().toString();
            String confirm = binding.etConfirmPassword.getText().toString();
            if (oldPassword.isEmpty() || password.isEmpty()) {
                binding.etNewPassword.setError(getString(R.string.api_required)); return;
            }
            if (!password.equals(confirm)) {
                binding.etConfirmPassword.setError(getString(R.string.api_password_mismatch)); return;
            }
            binding.btnOk.setEnabled(false);
            request(new com.example.myapplication.data.repository.profile.ProfileRepository(requireContext())
                    .changePassword(userId, new com.example.myapplication.data.remote.dto.request.ChangePasswordRequest(oldPassword, password, confirm)),
                    response -> {
                        binding.btnOk.setEnabled(true);
                        binding.etOldPassword.setText(""); binding.etNewPassword.setText(""); binding.etConfirmPassword.setText("");
                        android.widget.Toast.makeText(requireContext(), R.string.api_password_changed, android.widget.Toast.LENGTH_LONG).show();
                    }, error -> {
                        binding.btnOk.setEnabled(true);
                        binding.tvOldPasswordError.setText(error.message);
                        binding.tvOldPasswordError.setVisibility(View.VISIBLE);
                    });
        });
    }
}
