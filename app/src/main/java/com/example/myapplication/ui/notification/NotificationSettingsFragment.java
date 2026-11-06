package com.example.myapplication.ui.notification;

import com.example.myapplication.databinding.FragmentNotificationSettingsBinding;
import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.data.local.NotificationPreferences;

public class NotificationSettingsFragment extends BaseFragment<FragmentNotificationSettingsBinding> {
    public NotificationSettingsFragment() { super(FragmentNotificationSettingsBinding::inflate); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        NotificationPreferences preferences = new NotificationPreferences(requireContext());
        binding.btnEnableNotifications.setOnClickListener(view -> ((com.example.myapplication.ui.main.MainActivity) requireActivity()).enableNotifications());
        binding.switchSystemNotification.setChecked(preferences.systemEnabled());
        binding.switchAppointmentNotification.setChecked(preferences.appointmentEnabled());
        binding.switchScheduleNotification.setChecked(preferences.scheduleEnabled());
        binding.switchReminders.setChecked(preferences.remindersEnabled());
        binding.switchReminders.setOnCheckedChangeListener((button, checked) -> {
            preferences.setRemindersEnabled(checked);
            if (checked) {
                ((com.example.myapplication.ui.main.MainActivity) requireActivity()).enableNotifications();
                restoreReminders();
            } else com.example.myapplication.ui.schedule.BookingReminders.clear(requireContext());
        });
        binding.switchSystemNotification.setOnCheckedChangeListener((button, checked) -> preferences.setSystemEnabled(checked));
        binding.switchAppointmentNotification.setOnCheckedChangeListener((button, checked) -> {
            preferences.setAppointmentEnabled(checked);
            if (!checked) com.example.myapplication.ui.schedule.BookingReminders.clear(requireContext());
            else restoreReminders();
        });
        binding.switchScheduleNotification.setOnCheckedChangeListener((button, checked) -> preferences.setScheduleEnabled(checked));
    }
    private void restoreReminders() {
        com.example.myapplication.ui.schedule.BookingReminders.restore(requireContext());
        if (!new com.example.myapplication.data.local.PreferenceManager(requireContext()).isLogin()) return;
        request(new com.example.myapplication.data.repository.schedule.BookingRepository(requireContext()).getMyBookings(), response -> {
            if (response.data != null) for (com.example.myapplication.data.remote.dto.response.BookingDto booking : response.data)
                com.example.myapplication.ui.schedule.BookingReminders.sync(requireContext(), booking);
        });
    }
}
