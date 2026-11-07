package com.example.myapplication.ui.auth;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentVerifyOtpBinding;
import android.widget.Toast;

public class VerifyOtpFragment extends BaseAuthFragment<FragmentVerifyOtpBinding> {
    public static final String ARG_RESET_PASSWORD = "reset_password";
    public VerifyOtpFragment() { super(FragmentVerifyOtpBinding::inflate); }
    @Override protected void onScreenCreated(View root, Bundle state) {
        bindFlow(binding.btnVerify, binding.btnResend);
        enableScreenCatalog(binding.header, binding.ivPic);
        binding.ivBack.setOnClickListener(view -> back());
        boolean reset = getArguments() != null && getArguments().getBoolean(ARG_RESET_PASSWORD);
        binding.btnVerify.setOnClickListener(view -> {
            String otp = value(binding.edtOtp1) + value(binding.edtOtp2) + value(binding.edtOtp3)
                    + value(binding.edtOtp4) + value(binding.edtOtp5);
            if (!AuthViewModel.validOtp(otp) || email().isEmpty()) {
                Toast.makeText(requireContext(), R.string.api_invalid_otp, Toast.LENGTH_LONG).show(); return;
            }
            authModel().verifyOtp(email(), otp, reset);
        });
        binding.btnResend.setOnClickListener(view -> {
            if (email().isEmpty()) return;
            authModel().resendOtp(email(), reset);
        });
    }
}
