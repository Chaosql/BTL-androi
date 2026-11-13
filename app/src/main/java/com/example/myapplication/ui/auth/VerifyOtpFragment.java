package com.example.myapplication.ui.auth;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentVerifyOtpBinding;

public class VerifyOtpFragment extends BaseAuthFragment<FragmentVerifyOtpBinding> {
    public static final String ARG_OTP_SENT = "otp_sent";
    public static final String ARG_RESET_PASSWORD = "reset_password";
    public VerifyOtpFragment() { super(FragmentVerifyOtpBinding::inflate); }
    @Override protected void onScreenCreated(View root, Bundle state) {
        bindErrors(binding.tvOtpError, binding.edtOtp1, binding.edtOtp2, binding.edtOtp3, binding.edtOtp4, binding.edtOtp5);
        bindFlow(binding.btnVerify, binding.btnResend);
        binding.ivBack.setOnClickListener(view -> back());
        boolean reset = getArguments() != null && getArguments().getBoolean(ARG_RESET_PASSWORD);
        boolean alreadySent = getArguments() != null && getArguments().getBoolean(ARG_OTP_SENT);
        binding.tvOtpDescription.setText(getString(R.string.otp_recipient_email, email()));
        authModel().otpCooldown().observe(getViewLifecycleOwner(), seconds -> updateResend());
        authModel().flowState().observe(getViewLifecycleOwner(), flow -> updateResend());
        setupOtpFocus();
        if (email().isEmpty()) showError(getString(R.string.auth_email_required));
        else authModel().ensureInitialOtp(email(), reset, alreadySent);
        binding.btnVerify.setOnClickListener(view -> {
            clearError();
            String otp = value(binding.edtOtp1) + value(binding.edtOtp2) + value(binding.edtOtp3)
                    + value(binding.edtOtp4) + value(binding.edtOtp5);
            if (!AuthViewModel.validOtp(otp) || email().isEmpty()) {
                showError(getString(R.string.api_invalid_otp)); return;
            }
            authModel().verifyOtp(email(), otp, reset);
        });
        binding.btnResend.setOnClickListener(view -> {
            clearError();
            if (email().isEmpty()) return;
            authModel().resendOtp(email(), reset);
        });
    }
    private void updateResend() {
        Integer seconds = authModel().otpCooldown().getValue();
        int remaining = seconds == null ? 0 : seconds;
        com.example.myapplication.ui.base.UiState<AuthViewModel.FlowResult> flow = authModel().flowState().getValue();
        boolean loading = flow != null && flow.status == com.example.myapplication.ui.base.UiState.Status.LOADING;
        binding.btnResend.setEnabled(!loading && remaining == 0);
        binding.btnResend.setAlpha(binding.btnResend.isEnabled() ? 1f : 0.5f);
        binding.btnResend.setText(remaining > 0 ? getString(R.string.resend_otp_countdown, remaining) : getString(R.string.resend_otp));
    }
    private void setupOtpFocus() {
        android.widget.EditText[] inputs = {binding.edtOtp1, binding.edtOtp2, binding.edtOtp3, binding.edtOtp4, binding.edtOtp5};
        for (int i = 0; i < inputs.length; i++) {
            int index = i;
            android.widget.EditText input = inputs[i];
            input.setImeOptions(i == inputs.length - 1 ? android.view.inputmethod.EditorInfo.IME_ACTION_DONE
                    : android.view.inputmethod.EditorInfo.IME_ACTION_NEXT);
            input.addTextChangedListener(new android.text.TextWatcher() {
                private int previousLength;
                @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) { previousLength = text.length(); }
                @Override public void onTextChanged(CharSequence text, int start, int before, int count) { }
                @Override public void afterTextChanged(android.text.Editable text) {
                    if (!input.hasFocus() || !getViewLifecycleOwner().getLifecycle().getCurrentState()
                            .isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) return;
                    if (text.length() == 1 && index < inputs.length - 1) {
                        inputs[index + 1].requestFocus(); inputs[index + 1].setSelection(inputs[index + 1].length());
                    } else if (text.length() == 0 && previousLength > 0 && index > 0) {
                        inputs[index - 1].requestFocus(); inputs[index - 1].setSelection(inputs[index - 1].length());
                    }
                }
            });
            input.setOnKeyListener((view, keyCode, event) -> {
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL && event.getAction() == android.view.KeyEvent.ACTION_DOWN
                        && input.length() == 0 && index > 0) {
                    inputs[index - 1].requestFocus(); inputs[index - 1].setSelection(inputs[index - 1].length()); return true;
                }
                return false;
            });
        }
    }

}
