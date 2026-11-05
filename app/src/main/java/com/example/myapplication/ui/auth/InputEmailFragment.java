package com.example.myapplication.ui.auth;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.main.UiScreen;

public class InputEmailFragment extends BaseFragment {
    public InputEmailFragment() { super(R.layout.fragment_input_email); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        on(root, R.id.btnSendEmail, () -> {
            Bundle arguments = new Bundle();
            arguments.putBoolean(VerifyOtpFragment.ARG_RESET_PASSWORD, true);
            go(UiScreen.VERIFY_OTP, arguments);
        });
    }
}
