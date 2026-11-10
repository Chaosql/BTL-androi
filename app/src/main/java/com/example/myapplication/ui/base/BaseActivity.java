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
        android.graphics.drawable.LayerDrawable background = new android.graphics.drawable.LayerDrawable(
                new android.graphics.drawable.Drawable[]{
                        new android.graphics.drawable.ColorDrawable(getColor(com.example.myapplication.R.color.bg_2)),
                        new android.graphics.drawable.ColorDrawable(getColor(com.example.myapplication.R.color.bg))});
        background.setLayerGravity(1, android.view.Gravity.TOP);
        background.setLayerHeight(1, 0);
        root.setBackground(background);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int keyboard = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            background.setLayerHeight(1, bars.top);
            view.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, keyboard));
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
