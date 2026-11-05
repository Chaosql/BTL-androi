package com.example.myapplication.ui.auth;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.main.UiScreen;

public class LoginFragment extends BaseFragment {
    public LoginFragment() { super(R.layout.fragment_login); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        on(root, R.id.tvLogin, () -> go(UiScreen.MAP));
        on(root, R.id.btnGoogleLogin, () -> go(UiScreen.MAP));
        on(root, R.id.tvRegister, () -> go(UiScreen.REGISTER));
        on(root, R.id.tvForgot, () -> go(UiScreen.INPUT_EMAIL));
    }
}
