package com.example.myapplication.ui.main;

import com.example.myapplication.R;
import android.os.Bundle;


public class MainActivity extends com.example.myapplication.ui.base.BaseActivity {
    private com.example.myapplication.databinding.ActivityMainBinding binding;
    private final androidx.activity.result.ActivityResultLauncher<String> notificationPermission = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    com.example.myapplication.ui.notification.PushRegistration.register(this);
                    com.example.myapplication.ui.schedule.BookingReminders.restore(this);
                    android.widget.Toast.makeText(this, R.string.notification_permission_granted, android.widget.Toast.LENGTH_SHORT).show();
                } else android.widget.Toast.makeText(this, R.string.reminder_permission, android.widget.Toast.LENGTH_LONG).show();
            });
    public void enableNotifications() {
        android.content.SharedPreferences permissions = getSharedPreferences("permission_requests", MODE_PRIVATE);
        if (android.os.Build.VERSION.SDK_INT >= 33 && androidx.core.content.ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            if (permissions.getBoolean("notifications", false)
                    && !androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.POST_NOTIFICATIONS)) {
                openNotificationSettings();
                return;
            }
            permissions.edit().putBoolean("notifications", true).apply();
            notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            return;
        }
        if (!androidx.core.app.NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            openNotificationSettings();
            return;
        }
        com.example.myapplication.ui.notification.PushRegistration.register(this);
        com.example.myapplication.ui.schedule.BookingReminders.restore(this);
        android.widget.Toast.makeText(this, R.string.notification_permission_granted, android.widget.Toast.LENGTH_SHORT).show();
    }
    private void openNotificationSettings() {
        android.content.Intent settings = new android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, getPackageName());
        startActivity(settings);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = com.example.myapplication.databinding.ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applyWindowInsets(binding.getRoot());
        if (savedInstanceState == null) showScreen(new com.example.myapplication.data.local.PreferenceManager(this).homeScreen());
        if (savedInstanceState == null) openNotification(getIntent());
    }
    @Override protected void onResume() {
        super.onResume(); com.example.myapplication.ui.schedule.BookingReminders.channels(this);
        com.example.myapplication.ui.notification.PushRegistration.register(this);
        com.example.myapplication.ui.schedule.BookingReminders.restore(this);
    }
    @Override protected void onNewIntent(android.content.Intent intent) { super.onNewIntent(intent); setIntent(intent); openNotification(intent); }
    private void openNotification(android.content.Intent intent) {
        if (intent == null) return;
        com.example.myapplication.data.local.PreferenceManager session = new com.example.myapplication.data.local.PreferenceManager(this);
        if (!session.isLogin() || !session.getUserId().equals(intent.getStringExtra(com.example.myapplication.ui.schedule.BookingReminders.OWNER))) return;
        String id = intent.getStringExtra(com.example.myapplication.ui.schedule.BookingReminders.BOOKING);
        intent.removeExtra(com.example.myapplication.ui.schedule.BookingReminders.OWNER);
        intent.removeExtra(com.example.myapplication.ui.schedule.BookingReminders.BOOKING);
        if (id == null || id.isEmpty()) showScreen(UiScreen.NOTIFICATIONS);
        else { Bundle args = new Bundle(); args.putString(com.example.myapplication.ui.schedule.BookingUi.BOOKING_ID, id); showScreen(UiScreen.SCHEDULE, args); }
    }

    public void showScreen(UiScreen screen) { showScreen(screen, null); }

    public void showRootScreen(UiScreen screen) {
        getSupportFragmentManager().popBackStackImmediate(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager().beginTransaction().replace(R.id.screenHost, ScreenFactory.create(screen)).commit();
        if (screen != UiScreen.LOGIN) {
            com.example.myapplication.ui.notification.PushRegistration.register(this); openNotification(getIntent());
        }
    }

    public void showScreen(UiScreen screen, Bundle arguments) {
        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                .replace(R.id.screenHost, ScreenFactory.create(screen, arguments));
        if (getSupportFragmentManager().findFragmentById(R.id.screenHost) != null)
            transaction.addToBackStack(screen.name());
        transaction.commit();
    }

    @Override protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}
