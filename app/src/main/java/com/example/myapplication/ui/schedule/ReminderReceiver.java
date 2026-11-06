package com.example.myapplication.ui.schedule;

import android.content.*;

public final class ReminderReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction()))
            BookingReminders.restore(context);
        else BookingReminders.receive(context, intent.getStringExtra(BookingReminders.OWNER), intent.getStringExtra(BookingReminders.BOOKING));
    }
}
