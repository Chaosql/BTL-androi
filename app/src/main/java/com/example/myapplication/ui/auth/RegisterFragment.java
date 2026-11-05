package com.example.myapplication.ui.auth;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.main.UiScreen;

public class RegisterFragment extends BaseFragment {
    public RegisterFragment() { super(R.layout.fragment_register); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        on(root, R.id.tvLogin, () -> go(UiScreen.LOGIN));
        on(root, R.id.tvRegister, () -> go(UiScreen.VERIFY_EMAIL));
    }
}
