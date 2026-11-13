package com.example.myapplication.ui.profile;

import com.example.myapplication.databinding.FragmentAccountSettingsBinding;
import android.os.Bundle;
import android.view.View;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.main.UiScreen;

public class AccountSettingsFragment extends BaseFragment<FragmentAccountSettingsBinding> {
    public AccountSettingsFragment() { super(FragmentAccountSettingsBinding::inflate); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        binding.btnBack.setOnClickListener(view -> back());
        AccountSettingsViewModel settings = viewModel(AccountSettingsViewModel.class);
        binding.switchRadar.setOnCheckedChangeListener((button, checked) -> settings.setRadarEnabled(checked));
        settings.radar().observe(getViewLifecycleOwner(), radar -> {
            binding.switchRadar.setOnCheckedChangeListener(null);
            binding.switchRadar.setChecked(Boolean.TRUE.equals(settings.radarEnabled()));
            binding.switchRadar.setEnabled(settings.radarEnabled() != null &&
                    radar.status != com.example.myapplication.ui.base.UiState.Status.LOADING);
            binding.switchRadar.setOnCheckedChangeListener((button, checked) -> settings.setRadarEnabled(checked));
            boolean failed = radar.status == com.example.myapplication.ui.base.UiState.Status.ERROR;
            binding.tvRadarError.setVisibility(failed ? View.VISIBLE : View.GONE);
            binding.tvRadarError.setText(failed ? radar.error.message : "");
            if (radar.status == com.example.myapplication.ui.base.UiState.Status.SUCCESS) ProfileUi.updated(requireActivity());
        });
        settings.loadRadar();
        com.example.myapplication.ui.auth.SessionViewModel session = viewModel(com.example.myapplication.ui.auth.SessionViewModel.class);
        session.signedOut().observe(getViewLifecycleOwner(), event -> {
            if (event.consume() != null) ((com.example.myapplication.ui.main.MainActivity) requireActivity()).showRootScreen(UiScreen.LOGIN);
        });
        binding.cardPersonalInfo.setOnClickListener(bindingView -> go(UiScreen.REGISTRATION_INFO));
        binding.cardChangePassword.setOnClickListener(bindingView -> go(UiScreen.CHANGE_PASSWORD));
        binding.cardNotificationSettings.setOnClickListener(bindingView -> go(UiScreen.NOTIFICATION_SETTINGS));
        binding.cardBlockedUsers.setOnClickListener(bindingView -> go(UiScreen.BLOCKED_USERS));
        binding.cardLogout.setOnClickListener(bindingView -> {
            binding.cardLogout.setEnabled(false);
            session.signOut();
        });
    }
}
