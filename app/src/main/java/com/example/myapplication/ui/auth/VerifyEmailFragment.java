package com.example.myapplication.ui.auth;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.main.UiScreen;

public class VerifyEmailFragment extends BaseFragment {
    public VerifyEmailFragment() { super(R.layout.fragment_verify_email); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        on(root, R.id.btnSendEmail, () -> {
            Bundle arguments = new Bundle();
            arguments.putBoolean(VerifyOtpFragment.ARG_RESET_PASSWORD, false);
            go(UiScreen.VERIFY_OTP, arguments);
        });
    }
}
