package com.example.myapplication.ui.notification;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplication.data.local.NotificationPreferences;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.repository.schedule.BookingRepository;
import com.example.myapplication.ui.base.BaseViewModel;
import com.example.myapplication.ui.base.UiEvent;
import com.example.myapplication.ui.schedule.BookingReminders;

public final class NotificationSettingsViewModel extends BaseViewModel {
    private final NotificationPreferences preferences;
    private final MutableLiveData<UiEvent<String>> errors = new MutableLiveData<>();
    public NotificationSettingsViewModel(Application application) { super(application); preferences = new NotificationPreferences(application); }
    public boolean systemEnabled() { return preferences.systemEnabled(); }
    public boolean appointmentEnabled() { return preferences.appointmentEnabled(); }
    public boolean scheduleEnabled() { return preferences.scheduleEnabled(); }
    public boolean remindersEnabled() { return preferences.remindersEnabled(); }
    public LiveData<UiEvent<String>> errors() { return errors; }
    public void setSystemEnabled(boolean value) { preferences.setSystemEnabled(value); }
    public void setScheduleEnabled(boolean value) { preferences.setScheduleEnabled(value); }
    public void setAppointmentEnabled(boolean value) {
        preferences.setAppointmentEnabled(value); if (value) restoreReminders(); else BookingReminders.clear(getApplication());
    }
    public void setRemindersEnabled(boolean value) {
        preferences.setRemindersEnabled(value); if (value) restoreReminders(); else BookingReminders.clear(getApplication());
    }
    private void restoreReminders() {
        BookingReminders.restore(getApplication());
        if (!new PreferenceManager(getApplication()).isLogin()) return;
        watch(execute(new BookingRepository((android.content.Context) getApplication()).getMyBookings()), response -> {
            if (response.data != null) for (com.example.myapplication.data.remote.dto.response.BookingDto booking : response.data)
                BookingReminders.sync(getApplication(), booking);
        }, error -> errors.setValue(new UiEvent<>(error.message)));
    }
}
