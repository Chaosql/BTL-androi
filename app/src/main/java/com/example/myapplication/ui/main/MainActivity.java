package com.example.myapplication.ui.main;

import com.example.myapplication.R;
import android.os.Bundle;


public class MainActivity extends com.example.myapplication.ui.base.BaseActivity {
    private com.example.myapplication.databinding.ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = com.example.myapplication.databinding.ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applyWindowInsets(binding.getRoot());
        if (savedInstanceState == null) showScreen(new com.example.myapplication.data.local.PreferenceManager(this).isLogin()
                ? UiScreen.MAP : UiScreen.LOGIN);
    }

    public void showScreen(UiScreen screen) { showScreen(screen, null); }

    public void showRootScreen(UiScreen screen) {
        getSupportFragmentManager().popBackStackImmediate(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager().beginTransaction().replace(R.id.screenHost, ScreenFactory.create(screen)).commit();
    }

    public void showScreen(UiScreen screen, Bundle arguments) {
        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                .replace(R.id.screenHost, ScreenFactory.create(screen, arguments));
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

    @Override protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}
