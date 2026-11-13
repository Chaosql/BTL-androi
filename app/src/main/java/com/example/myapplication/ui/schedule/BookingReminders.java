package com.example.myapplication.ui.schedule;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.myapplication.R;
import com.example.myapplication.data.local.*;
import com.example.myapplication.data.remote.dto.response.BookingDto;
import com.example.myapplication.ui.main.MainActivity;
import com.google.gson.Gson;

public final class BookingReminders {
    public static final String CHANNEL = "studydate_appointments";
    public static final String BOOKING = "notification_booking", OWNER = "notification_owner";
    private static SharedPreferences store(Context context) { return context.getSharedPreferences("studydate_reminders", 0); }
    private static String key(String owner, String id) { return owner + "|" + id; }
    private static PendingIntent alarm(Context context, String owner, String id) {
        Intent intent = new Intent(context, ReminderReceiver.class).setData(new Uri.Builder().scheme("studydate")
                .authority("reminder").appendPath(owner).appendPath(id).build()).putExtra(BOOKING, id).putExtra(OWNER, owner);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
    public static void sync(Context context, BookingDto booking) {
        PreferenceManager session = new PreferenceManager(context);
        if (booking == null || booking.id == null || booking.id.isEmpty() || !session.isLogin()) return;
        String owner = session.getUserId();
        boolean member = owner.equals(booking.creatorId) || owner.equals(booking.partnerId);
        if (booking.participants != null) for (com.example.myapplication.data.remote.dto.response.BookingParticipantDto p : booking.participants)
            if (p != null && owner.equals(p.userId)) member = true;
        java.time.ZonedDateTime time = BookingUi.time(booking.scheduledAt);
        NotificationPreferences settings = new NotificationPreferences(context);
        if (!member || !"CONFIRMED".equals(booking.status) || time == null || !settings.appointmentEnabled() || !settings.remindersEnabled()) {
            cancel(context, booking.id); return;
        }
        long startsAt = time.toInstant().toEpochMilli();
        long trigger = startsAt - 30 * 60 * 1000L;
        if (startsAt <= System.currentTimeMillis()) { unschedule(context, owner, booking.id); return; }
        store(context).edit().putString(key(owner, booking.id), new Gson().toJson(booking)).apply();
        if (trigger <= System.currentTimeMillis()) {
            cancelAlarm(context, owner, booking.id);
            SharedPreferences delivered = context.getSharedPreferences("studydate_reminder_deliveries", 0);
            if (delivered.getLong(key(owner, booking.id), 0) != startsAt && show(context, owner, booking.id,
                    context.getString(R.string.reminder_channel), context.getString(R.string.reminder_body,
                    BookingUi.value(booking.locationName), time.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))))) {
                delivered.edit().putLong(key(owner, booking.id), startsAt).apply();
            }
            return;
        }
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) return;
        PendingIntent intent = alarm(context, owner, booking.id);
        try {
            if (Build.VERSION.SDK_INT < 31 || manager.canScheduleExactAlarms()) manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, intent);
            else manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, intent);
        } catch (SecurityException error) { manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, intent); }
    }
    public static void cancel(Context context, String id) {
        String owner = new PreferenceManager(context).getUserId();
        unschedule(context, owner, id);
        NotificationManagerCompat.from(context).cancel(key(owner, id), 0);
    }
    private static void cancelAlarm(Context context, String owner, String id) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) manager.cancel(alarm(context, owner, id));
    }
    private static void unschedule(Context context, String owner, String id) {
        cancelAlarm(context, owner, id);
        store(context).edit().remove(key(owner, id)).apply();
    }
    public static void clear(Context context) {
        String owner = new PreferenceManager(context).getUserId();
        for (String record : store(context).getAll().keySet()) if (record.startsWith(owner + "|")) cancel(context, record.substring(owner.length() + 1));
    }
    public static void restore(Context context) {
        String owner = new PreferenceManager(context).getUserId();
        for (java.util.Map.Entry<String, ?> record : store(context).getAll().entrySet()) {
            if (!record.getKey().startsWith(owner + "|") || !(record.getValue() instanceof String)) continue;
            try { sync(context, new Gson().fromJson((String) record.getValue(), BookingDto.class)); }
            catch (RuntimeException error) { store(context).edit().remove(record.getKey()).apply(); }
        }
    }
    public static void channels(Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) manager.createNotificationChannel(new NotificationChannel(CHANNEL,
                context.getString(R.string.reminder_channel), NotificationManager.IMPORTANCE_DEFAULT));
    }
    @android.annotation.SuppressLint("MissingPermission")
    public static boolean show(Context context, String owner, String booking, String title, String body) {
        PreferenceManager session = new PreferenceManager(context);
        NotificationPreferences settings = new NotificationPreferences(context);
        if (!session.isLogin() || !owner.equals(session.getUserId()) || !NotificationManagerCompat.from(context).areNotificationsEnabled()) return false;
        if (Build.VERSION.SDK_INT >= 33 && androidx.core.content.ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) return false;
        if (booking != null && !settings.appointmentEnabled()) return false;
        channels(context);
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        NotificationChannel channel = manager == null ? null : manager.getNotificationChannel(CHANNEL);
        if (channel != null && channel.getImportance() == NotificationManager.IMPORTANCE_NONE) return false;
        Intent open = new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(OWNER, owner).setData(new Uri.Builder().scheme("studydate").authority("notification").appendPath(owner)
                        .appendPath(booking == null ? "inbox" : booking).build());
        if (booking != null) open.putExtra(BOOKING, booking);
        PendingIntent tap = PendingIntent.getActivity(context, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        try {
            NotificationManagerCompat.from(context).notify(booking == null ? "inbox" : key(owner, booking), 0,
                    new NotificationCompat.Builder(context, CHANNEL).setSmallIcon(R.drawable.ic_notification)
                            .setContentTitle(title).setContentText(body).setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                            .setOnlyAlertOnce(true).setAutoCancel(true).setContentIntent(tap).build());
            return true;
        } catch (SecurityException denied) { return false; }
    }
    static void receive(Context context, String owner, String id) {
        if (id == null || owner == null || !owner.equals(new PreferenceManager(context).getUserId())) return;
        String json = store(context).getString(key(owner, id), "");
        if (json.isEmpty()) return;
        BookingDto booking;
        try { booking = new Gson().fromJson(json, BookingDto.class); } catch (RuntimeException error) { cancel(context, id); return; }
        sync(context, booking);
    }
    private BookingReminders() { }
}
