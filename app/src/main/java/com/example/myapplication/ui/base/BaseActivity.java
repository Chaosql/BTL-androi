package com.example.myapplication.ui.base;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public abstract class BaseActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        EdgeToEdge.enable(this, SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
    }

    protected final void applyWindowInsets(View root) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int keyboard = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            view.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, keyboard));
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
