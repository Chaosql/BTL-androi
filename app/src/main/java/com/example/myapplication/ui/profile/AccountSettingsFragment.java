package com.example.myapplication.ui.profile;

import com.example.myapplication.databinding.FragmentAccountSettingsBinding;
import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.main.UiScreen;

public class AccountSettingsFragment extends BaseFragment<FragmentAccountSettingsBinding> {
    public AccountSettingsFragment() { super(FragmentAccountSettingsBinding::inflate); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        binding.cardPersonalInfo.setOnClickListener(bindingView -> go(UiScreen.REGISTRATION_INFO));
        binding.cardChangePassword.setOnClickListener(bindingView -> go(UiScreen.CHANGE_PASSWORD));
        binding.cardNotificationSettings.setOnClickListener(bindingView -> go(UiScreen.NOTIFICATION_SETTINGS));
        binding.cardBlockedUsers.setOnClickListener(bindingView -> go(UiScreen.BLOCKED_USERS));
        binding.cardLogout.setOnClickListener(bindingView -> {
            binding.cardLogout.setEnabled(false);
            com.example.myapplication.ui.main.MainActivity host = (com.example.myapplication.ui.main.MainActivity) requireActivity();
            com.example.myapplication.ui.notification.PushRegistration.endSession(requireContext(), () -> {
                if (!host.isDestroyed()) host.showRootScreen(UiScreen.LOGIN);
            });
        });
        binding.cardHelp.setOnClickListener(bindingView -> this.previewNotice());
    }
}
