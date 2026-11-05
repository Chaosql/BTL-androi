package com.example.myapplication.ui.profile;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.main.UiScreen;

public class AccountSettingsFragment extends BaseFragment {
    public AccountSettingsFragment() { super(R.layout.fragment_account_settings); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        on(root, R.id.cardPersonalInfo, () -> go(UiScreen.REGISTRATION_INFO));
        on(root, R.id.cardChangePassword, () -> go(UiScreen.CHANGE_PASSWORD));
        on(root, R.id.cardNotificationSettings, () -> go(UiScreen.NOTIFICATION_SETTINGS));
        on(root, R.id.cardBlockedUsers, () -> go(UiScreen.BLOCKED_USERS));
        on(root, R.id.cardLogout, () -> go(UiScreen.LOGIN));
        on(root, R.id.cardHelp, this::previewNotice);
    }
}
