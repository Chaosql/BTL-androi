package com.example.myapplication;

import android.app.AlarmManager;
import android.content.Context;
import com.example.myapplication.data.local.*;
import com.example.myapplication.data.remote.dto.response.*;
import com.example.myapplication.ui.schedule.BookingReminders;
import java.time.Instant;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk = 34)
public class BookingReminderTest {
    private Context context;
    private PreferenceManager session;
    private BookingDto booking;
    @Before public void setup() {
        context = RuntimeEnvironment.getApplication(); session = new PreferenceManager(context); session.logout();
        Shadows.shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS);
        context.getSharedPreferences("studydate_reminder_deliveries", 0).edit().clear().commit();
        context.getSharedPreferences("notification_prefs", 0).edit().clear().commit();
        context.getSharedPreferences("studydate_reminders", 0).edit().clear().commit();
        session.saveLogin(new LoginData("Bearer", "access", "refresh", "self", null), "test@example.com");
        booking = new BookingDto(); booking.id = "b1"; booking.creatorId = "self";
        booking.partnerId = "other"; booking.status = "CONFIRMED";
        booking.scheduledAt = Instant.now().plusSeconds(7200).toString();
    }
    @After public void cleanup() { BookingReminders.clear(context); session.logout(); context.getSharedPreferences("notification_prefs", 0).edit().clear().commit(); }
    private int alarms() { return Shadows.shadowOf((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).getScheduledAlarms().size(); }
    @Test public void confirmedBookingSchedulesOnceAndCancellationRemovesIt() {
        BookingReminders.sync(context, booking); BookingReminders.sync(context, booking); assertEquals(1, alarms());
        booking.status = "CANCELLED"; BookingReminders.sync(context, booking); assertEquals(0, alarms());
        assertTrue(context.getSharedPreferences("studydate_reminders", 0).getAll().isEmpty());
    }
    @Test public void disabledAndUnrelatedBookingsDoNotSchedule() {
        new NotificationPreferences(context).setRemindersEnabled(false); BookingReminders.sync(context, booking); assertEquals(0, alarms());
        new NotificationPreferences(context).setRemindersEnabled(true); booking.creatorId = "unknown"; booking.partnerId = "other";
        BookingReminders.sync(context, booking); assertEquals(0, alarms());
    }
    @Test public void rebootRestorationDoesNotDuplicateAndLogoutClears() {
        BookingReminders.sync(context, booking); BookingReminders.restore(context); assertEquals(1, alarms());
        BookingReminders.clear(context); session.logout(); BookingReminders.restore(context); assertEquals(0, alarms());
    }
    private org.robolectric.shadows.ShadowNotificationManager notifications() {
        return Shadows.shadowOf(context.getSystemService(android.app.NotificationManager.class));
    }
    @Test public void bookingLoadedTwentyEightMinutesBeforeStartNotifiesOnceAndRefreshKeepsIt() {
        booking.scheduledAt = Instant.now().plusSeconds(28 * 60).toString();
        BookingReminders.sync(context, booking);
        assertEquals(1, notifications().size());
        android.app.Notification first = notifications().getAllNotifications().get(0);
        BookingReminders.sync(context, booking); BookingReminders.restore(context);
        assertEquals(1, notifications().size());
        assertSame(first, notifications().getAllNotifications().get(0));
        assertEquals(0, alarms());
    }
    @Test public void deniedNotificationPermissionCanRetryAfterGrantWithoutBeingMarkedDelivered() {
        booking.scheduledAt = Instant.now().plusSeconds(28 * 60).toString();
        Shadows.shadowOf(RuntimeEnvironment.getApplication()).denyPermissions(android.Manifest.permission.POST_NOTIFICATIONS);
        BookingReminders.sync(context, booking); assertEquals(0, notifications().size());
        Shadows.shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS);
        BookingReminders.restore(context); assertEquals(1, notifications().size());
    }
    @Test public void cancellingBookingRemovesAlreadyDisplayedReminder() {
        booking.scheduledAt = Instant.now().plusSeconds(28 * 60).toString();
        BookingReminders.sync(context, booking); assertEquals(1, notifications().size());
        booking.status = "CANCELLED";
        BookingReminders.sync(context, booking); assertEquals(0, notifications().size());
    }
    @Test public void expiredBookingDoesNotPostLateReminder() {
        booking.scheduledAt = Instant.now().minusSeconds(60).toString();
        BookingReminders.sync(context, booking);
        assertEquals(0, alarms()); assertEquals(0, notifications().size());
    }
    @Test public void rescheduledBookingCanNotifyAgainForNewStartTime() {
        booking.scheduledAt = Instant.now().plusSeconds(28 * 60).toString();
        BookingReminders.sync(context, booking);
        android.app.Notification first = notifications().getAllNotifications().get(0);
        booking.scheduledAt = Instant.now().plusSeconds(29 * 60).toString();
        BookingReminders.sync(context, booking);
        assertNotSame(first, notifications().getAllNotifications().get(0));
    }

    @Test public void pendingAndLateBookingsDoNotSchedule() {
        booking.status = "PENDING"; BookingReminders.sync(context, booking); assertEquals(0, alarms());
        booking.status = "CONFIRMED"; booking.scheduledAt = Instant.now().plusSeconds(60).toString();
        BookingReminders.sync(context, booking); assertEquals(0, alarms());
    }
}
