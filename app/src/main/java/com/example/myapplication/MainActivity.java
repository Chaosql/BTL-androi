package com.example.myapplication;

import android.os.Bundle;
import android.graphics.Color;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this, SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int keyboard = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            v.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, keyboard));
            return WindowInsetsCompat.CONSUMED;
        });
        if (savedInstanceState == null) showScreen(UiScreen.LOGIN);
    }

    public void showScreen(UiScreen screen) {
        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                .replace(R.id.screenHost, StudyScreenFragment.create(screen));
        if (getSupportFragmentManager().findFragmentById(R.id.screenHost) != null)
            transaction.addToBackStack(screen.name());
        transaction.commit();
    }

    public void showScreens() {
        UiScreen[] screens = UiScreen.values();
        String[] labels = new String[screens.length + 1];
        for (int i = 0; i < screens.length; i++) labels[i] = getString(screens[i].title);
        labels[screens.length] = getString(R.string.ui_mainactivity_text_1);
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.ui_mainactivity_text_2))
                .setItems(labels, (dialog, which) -> {
                    if (which == screens.length) UiDialogs.showCatalog(this);
                    else showScreen(screens[which]);
                }).setNegativeButton(getString(R.string.btn_close), null).show();
    }
}
