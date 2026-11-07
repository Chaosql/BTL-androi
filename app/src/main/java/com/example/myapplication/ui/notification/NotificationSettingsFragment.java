package com.example.myapplication.ui.notification;

import com.example.myapplication.databinding.FragmentNotificationSettingsBinding;
import android.os.Bundle;
import android.view.View;
import com.example.myapplication.ui.base.BaseFragment;

public class NotificationSettingsFragment extends BaseFragment<FragmentNotificationSettingsBinding> {
    public NotificationSettingsFragment() { super(FragmentNotificationSettingsBinding::inflate); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        enableScreenCatalog(binding.tvHeaderTitle);
        binding.btnBack.setOnClickListener(view -> back());
        NotificationSettingsViewModel preferences = viewModel(NotificationSettingsViewModel.class);
        preferences.errors().observe(getViewLifecycleOwner(), event -> {
            String message = event.consume(); if (message != null) android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show();
        });
        binding.btnEnableNotifications.setOnClickListener(view -> ((com.example.myapplication.ui.main.MainActivity) requireActivity()).enableNotifications());
        binding.switchSystemNotification.setChecked(preferences.systemEnabled());
        binding.switchAppointmentNotification.setChecked(preferences.appointmentEnabled());
        binding.switchScheduleNotification.setChecked(preferences.scheduleEnabled());
        binding.switchReminders.setChecked(preferences.remindersEnabled());
        binding.switchReminders.setOnCheckedChangeListener((button, checked) -> {
            preferences.setRemindersEnabled(checked);
            if (checked) {
                ((com.example.myapplication.ui.main.MainActivity) requireActivity()).enableNotifications();
            }
        });
        binding.switchSystemNotification.setOnCheckedChangeListener((button, checked) -> preferences.setSystemEnabled(checked));
        binding.switchAppointmentNotification.setOnCheckedChangeListener((button, checked) -> {
            preferences.setAppointmentEnabled(checked);
        });
        binding.switchScheduleNotification.setOnCheckedChangeListener((button, checked) -> preferences.setScheduleEnabled(checked));
    }
}
