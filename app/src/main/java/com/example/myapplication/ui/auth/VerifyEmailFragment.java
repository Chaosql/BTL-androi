package com.example.myapplication.ui.auth;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentVerifyEmailBinding;
import com.example.myapplication.ui.main.UiScreen;

public class VerifyEmailFragment extends BaseAuthFragment<FragmentVerifyEmailBinding> {
    public VerifyEmailFragment() { super(FragmentVerifyEmailBinding::inflate); }
    @Override protected void onScreenCreated(View root, Bundle state) {
        bindErrors(binding.tvEmailError);
        bindFlow(binding.btnSendEmail);
        binding.btnBack.setOnClickListener(view -> back());
        binding.tvDescription.setText(getString(R.string.api_verify_email_description, email()));
        binding.btnSendEmail.setOnClickListener(view -> {
            clearError();
            if (email().isEmpty()) { go(UiScreen.REGISTER); return; }
            authModel().sendRegistrationOtp(email());
        });
    }
}
