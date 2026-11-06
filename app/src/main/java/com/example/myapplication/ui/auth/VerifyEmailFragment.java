package com.example.myapplication.ui.auth;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentVerifyEmailBinding;
import com.example.myapplication.ui.main.UiScreen;
import com.example.myapplication.data.remote.dto.request.SendOtpRequest;

public class VerifyEmailFragment extends BaseAuthFragment<FragmentVerifyEmailBinding> {
    public VerifyEmailFragment() { super(FragmentVerifyEmailBinding::inflate); }
    @Override protected void onScreenCreated(View root, Bundle state) {
        binding.tvDescription.setText(getString(R.string.api_verify_email_description, email()));
        binding.btnSendEmail.setOnClickListener(view -> {
            if (email().isEmpty()) { go(UiScreen.REGISTER); return; }
            submit(repository().sendOtp(new SendOtpRequest(email())), binding.btnSendEmail,
                    response -> go(UiScreen.VERIFY_OTP, flow(email(), false)));
        });
    }
}
