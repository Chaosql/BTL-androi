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
            com.example.myapplication.data.local.PreferenceManager preferences =
                    new com.example.myapplication.data.local.PreferenceManager(requireContext());
            String token = preferences.getRefreshToken();
            binding.cardLogout.setEnabled(false);
            Runnable clear = () -> {
                preferences.logout();
                ((com.example.myapplication.ui.main.MainActivity) requireActivity()).showRootScreen(UiScreen.LOGIN);
            };
            if (token.isEmpty()) { clear.run(); return; }
            request(new com.example.myapplication.data.repository.auth.AuthRepository(requireContext())
                    .logout(new com.example.myapplication.data.remote.dto.request.LogoutRequest(token)),
                    response -> clear.run(), error -> clear.run());
        });
        binding.cardHelp.setOnClickListener(bindingView -> this.previewNotice());
    }
}
