package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import com.example.myapplication.ui.main.MainActivity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class NotificationPermissionTest {
    private ActivityController<MainActivity> controller;
    private MainActivity activity;

    @Before public void setup() {
        RuntimeEnvironment.getApplication().getSharedPreferences("permission_requests", Context.MODE_PRIVATE).edit().clear().commit();
        Shadows.shadowOf(RuntimeEnvironment.getApplication()).denyPermissions(Manifest.permission.POST_NOTIFICATIONS);
        controller = Robolectric.buildActivity(MainActivity.class).setup();
        activity = controller.get();
    }
    @After public void cleanup() { controller.close(); }

    @Test public void firstAttemptRequestsNotificationPermission() {
        activity.enableNotifications();
        ShadowActivity.PermissionsRequest request = Shadows.shadowOf(activity).getLastRequestedPermission();
        assertNotNull(request);
        assertArrayEquals(new String[]{Manifest.permission.POST_NOTIFICATIONS}, request.requestedPermissions);
    }

    @Test public void alreadyGrantedPermissionShowsFeedbackWithoutAnotherPrompt() {
        Shadows.shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.POST_NOTIFICATIONS);
        activity.enableNotifications();
        assertNull(Shadows.shadowOf(activity).getLastRequestedPermission());
        assertEquals(activity.getString(R.string.notification_permission_granted),
                org.robolectric.shadows.ShadowToast.getTextOfLatestToast());
    }

    @Test public void permanentlyDeniedPermissionOpensSystemNotificationSettings() {
        activity.getSharedPreferences("permission_requests", Context.MODE_PRIVATE).edit().putBoolean("notifications", true).commit();
        activity.enableNotifications();
        Intent intent = Shadows.shadowOf(activity).getNextStartedActivity();
        assertNotNull(intent);
        assertEquals(Settings.ACTION_APP_NOTIFICATION_SETTINGS, intent.getAction());
        assertEquals(activity.getPackageName(), intent.getStringExtra(Settings.EXTRA_APP_PACKAGE));
    }
}
