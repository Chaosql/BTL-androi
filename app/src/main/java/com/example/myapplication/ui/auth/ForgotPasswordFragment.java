package com.example.myapplication.ui.auth;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.main.UiScreen;

public class ForgotPasswordFragment extends BaseFragment {
    public ForgotPasswordFragment() { super(R.layout.fragment_forgot_password); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        toggle(root, R.id.ivTogglePassword, R.id.edtNewPassword);
        on(root, R.id.tvConfirm, () -> go(UiScreen.LOGIN));
    }
}
