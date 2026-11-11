package com.example.myapplication.ui.auth;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.databinding.FragmentInputEmailBinding;

public class InputEmailFragment extends BaseAuthFragment<FragmentInputEmailBinding> {
    public InputEmailFragment() { super(FragmentInputEmailBinding::inflate); }
    @Override protected void onScreenCreated(View root, Bundle state) {
        bindErrors(binding.tvEmailError, binding.edtEmail);
        bindFlow(binding.btnSendEmail);
        enableScreenCatalog(binding.tvTitle);
        binding.btnBack.setOnClickListener(view -> back());
        binding.btnSendEmail.setOnClickListener(view -> {
            clearError();
            if (!validEmail(binding.edtEmail)) return;
            String email = value(binding.edtEmail);
            authModel().sendResetOtp(email);
        });
    }
}
