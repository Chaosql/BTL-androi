package com.example.myapplication;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            int statusBarTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), statusBarTop, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
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
        for (int i = 0; i < screens.length; i++) labels[i] = screens[i].title;
        labels[screens.length] = "Hộp thoại và menu";
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Các màn hình Study Date")
                .setItems(labels, (dialog, which) -> {
                    if (which == screens.length) UiDialogs.showCatalog(this);
                    else showScreen(screens[which]);
                }).setNegativeButton("Đóng", null).show();
    }
}
