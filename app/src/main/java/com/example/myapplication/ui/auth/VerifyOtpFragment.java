package com.example.myapplication.ui.auth;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentVerifyOtpBinding;
import com.example.myapplication.ui.main.UiScreen;
import android.widget.Toast;
import com.example.myapplication.data.remote.dto.request.VerifyOtpRequest;
import com.example.myapplication.data.remote.dto.request.SendOtpRequest;

public class VerifyOtpFragment extends BaseAuthFragment<FragmentVerifyOtpBinding> {
    public static final String ARG_RESET_PASSWORD = "reset_password";
    public VerifyOtpFragment() { super(FragmentVerifyOtpBinding::inflate); }
    @Override protected void onScreenCreated(View root, Bundle state) {
        boolean reset = getArguments() != null && getArguments().getBoolean(ARG_RESET_PASSWORD);
        binding.btnVerify.setOnClickListener(view -> {
            String otp = value(binding.edtOtp1) + value(binding.edtOtp2) + value(binding.edtOtp3)
                    + value(binding.edtOtp4) + value(binding.edtOtp5);
            if (!otp.matches("[0-9]{5}") || email().isEmpty()) {
                Toast.makeText(requireContext(), R.string.api_invalid_otp, Toast.LENGTH_LONG).show(); return;
            }
            VerifyOtpRequest request = new VerifyOtpRequest(email(), otp);
            submit(reset ? repository().forgotPasswordVerifyOtp(request) : repository().verifyRegisterOtp(request),
                    binding.btnVerify, response -> {
                        if (reset) go(UiScreen.FORGOT_PASSWORD, flow(email(), true));
                        else {
                            Toast.makeText(requireContext(), R.string.api_verified, Toast.LENGTH_LONG).show();
                            go(UiScreen.LOGIN);
                        }
                    });
        });
        binding.btnResend.setOnClickListener(view -> {
            if (email().isEmpty()) return;
            SendOtpRequest request = new SendOtpRequest(email());
            submit(reset ? repository().forgotPasswordSendOtp(request) : repository().sendOtp(request),
                    binding.btnResend, response -> Toast.makeText(requireContext(), R.string.api_otp_sent, Toast.LENGTH_LONG).show());
        });
    }
}
