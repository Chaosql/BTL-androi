package com.example.myapplication.ui.auth;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentInputEmailBinding;
import com.example.myapplication.ui.main.UiScreen;
import com.example.myapplication.data.remote.dto.request.SendOtpRequest;

public class InputEmailFragment extends BaseAuthFragment<FragmentInputEmailBinding> {
    public InputEmailFragment() { super(FragmentInputEmailBinding::inflate); }
    @Override protected void onScreenCreated(View root, Bundle state) {
        binding.btnSendEmail.setOnClickListener(view -> {
            if (!validEmail(binding.edtEmail)) return;
            String email = value(binding.edtEmail);
            submit(repository().forgotPasswordSendOtp(new SendOtpRequest(email)), binding.btnSendEmail,
                    response -> go(UiScreen.VERIFY_OTP, flow(email, true)));
        });
    }
}
