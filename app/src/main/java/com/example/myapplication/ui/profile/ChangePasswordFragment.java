package com.example.myapplication.ui.profile;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.main.UiScreen;

public class ChangePasswordFragment extends BaseFragment {
    public ChangePasswordFragment() { super(R.layout.fragment_change_password); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        on(root, R.id.tvForgotPassword, () -> go(UiScreen.INPUT_EMAIL));
        on(root, R.id.btnOk, this::previewNotice);
    }
}
