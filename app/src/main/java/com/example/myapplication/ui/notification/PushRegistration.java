package com.example.myapplication.ui.notification;

import android.content.Context;
import androidx.core.app.NotificationManagerCompat;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.example.myapplication.data.local.*;
import com.example.myapplication.data.remote.network.*;
import com.example.myapplication.data.remote.dto.request.RegisterDeviceRequest;
import com.example.myapplication.data.remote.dto.response.*;
import com.example.myapplication.data.repository.notification.NotificationRepository;
import com.example.myapplication.ui.schedule.BookingReminders;

public final class PushRegistration {
    private static boolean registering;
    private static boolean endingSession;
    public static void register(Context context) {
        Context app = context.getApplicationContext();
        PreferenceManager session = new PreferenceManager(app);
        if (android.os.Build.VERSION.SDK_INT >= 33 && androidx.core.content.ContextCompat.checkSelfPermission(app,
                android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) return;
        if (!session.isLogin() || !NotificationManagerCompat.from(app).areNotificationsEnabled() || registering || endingSession) return;
        if (FirebaseApp.getApps(app).isEmpty() && FirebaseApp.initializeApp(app) == null) return;
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> token(app, token));
    }
    public static void token(Context context, String token) {
        Context app = context.getApplicationContext(); PreferenceManager session = new PreferenceManager(app);
        if (!session.isLogin() || token == null || token.isEmpty() || registering || endingSession) return;
        String owner = session.getUserId();
        android.content.SharedPreferences prefs = app.getSharedPreferences("studydate_push", 0);
        if (token.equals(prefs.getString("token", "")) && owner.equals(prefs.getString("owner", ""))) return;
        registering = true;
        ApiRequests.enqueue(new NotificationRepository(app).registerDevice(new RegisterDeviceRequest(token, "ANDROID", android.os.Build.MODEL)),
                new ApiCallback<BaseResponse<ActionStatusDto>>() {
                    @Override public void onSuccess(BaseResponse<ActionStatusDto> result) {
                        registering = false;
                        if (owner.equals(new PreferenceManager(app).getUserId())) prefs.edit().putString("token", token).putString("owner", owner).apply();
                    }
                    @Override public void onError(ApiError error) { registering = false; }
                });
    }
    public static void logout(Context context, Runnable complete) {
        Context app = context.getApplicationContext();
        BookingReminders.clear(app); NotificationManagerCompat.from(app).cancelAll();
        android.content.SharedPreferences prefs = app.getSharedPreferences("studydate_push", 0);
        String token = prefs.getString("token", ""); prefs.edit().clear().apply();
        if (!FirebaseApp.getApps(app).isEmpty()) {
            FirebaseMessaging.getInstance().setAutoInitEnabled(false); FirebaseMessaging.getInstance().deleteToken();
        }
        if (token.isEmpty()) { complete.run(); return; }
        ApiRequests.enqueue(new NotificationRepository(app).unregisterDevice(token), new ApiCallback<BaseResponse<ActionStatusDto>>() {
            @Override public void onSuccess(BaseResponse<ActionStatusDto> result) { complete.run(); }
            @Override public void onError(ApiError error) { complete.run(); }
        });
    }
    public static void endSession(Context context, Runnable after) {
        Context app = context.getApplicationContext(); PreferenceManager session = new PreferenceManager(app);
        String owner = session.getUserId(), refresh = session.getRefreshToken();
        endingSession = true;
        Runnable clear = () -> { endingSession = false; if (owner.equals(session.getUserId())) { session.logout(); after.run(); } };
        logout(app, () -> {
            if (!owner.equals(session.getUserId())) { endingSession = false; return; }
            if (refresh.isEmpty()) { clear.run(); return; }
            ApiRequests.enqueue(new com.example.myapplication.data.repository.auth.AuthRepository(app).logout(
                    new com.example.myapplication.data.remote.dto.request.LogoutRequest(refresh)), new ApiCallback<BaseResponse<Object>>() {
                @Override public void onSuccess(BaseResponse<Object> response) { clear.run(); }
                @Override public void onError(ApiError error) { clear.run(); }
            });
        });
    }
    private PushRegistration() { }
}
