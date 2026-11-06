package com.example.myapplication.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.myapplication.data.remote.dto.response.NotificationDto;
import java.util.Locale;

public final class NotificationPreferences {
    private final SharedPreferences preferences;
    public NotificationPreferences(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences("notification_prefs", Context.MODE_PRIVATE);
    }
    public boolean systemEnabled() { return preferences.getBoolean("system_notif", true); }
    public boolean remindersEnabled() { return preferences.getBoolean("local_reminders", true); }
    public void setRemindersEnabled(boolean value) { preferences.edit().putBoolean("local_reminders", value).apply(); }
    public boolean appointmentEnabled() { return preferences.getBoolean("appointment_notif", true); }
    public boolean scheduleEnabled() { return preferences.getBoolean("schedule_notif", true); }
    public void setSystemEnabled(boolean value) { preferences.edit().putBoolean("system_notif", value).apply(); }
    public void setAppointmentEnabled(boolean value) { preferences.edit().putBoolean("appointment_notif", value).apply(); }
    public void setScheduleEnabled(boolean value) { preferences.edit().putBoolean("schedule_notif", value).apply(); }
    public boolean hasFilter() { return !systemEnabled() || !appointmentEnabled() || !scheduleEnabled(); }
    public boolean allows(NotificationDto item) {
        if (item == null) return false;
        String type = item.targetType == null ? "" : item.targetType.trim();
        if (type.isEmpty()) type = item.type == null ? "" : item.type.trim();
        type = type.toUpperCase(Locale.ROOT);
        if (type.startsWith("BOOKING_") || type.startsWith("APPOINTMENT_")) return appointmentEnabled();
        switch (type) {
            case "BOOKING": case "APPOINTMENT": return appointmentEnabled();
            case "SCHEDULE": case "TIMETABLE": return scheduleEnabled();
            default: return systemEnabled();
        }
    }
}
