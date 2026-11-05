package com.example.myapplication.ui.auth;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.main.MainActivity;
import com.example.myapplication.ui.main.UiDialogs;
import com.example.myapplication.ui.main.UiScreen;

public class VerifyOtpFragment extends BaseFragment {
    public static final String ARG_RESET_PASSWORD = "reset_password";

    public VerifyOtpFragment() { super(R.layout.fragment_verify_otp); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        boolean resetPassword = getArguments() != null && getArguments().getBoolean(ARG_RESET_PASSWORD);
        on(root, R.id.btnVerify, () -> {
            if (resetPassword) go(UiScreen.FORGOT_PASSWORD);
            else UiDialogs.show((MainActivity) requireActivity(), R.layout.dialog_login_success);
        });
        on(root, R.id.btnResend, this::previewNotice);
    }
}
