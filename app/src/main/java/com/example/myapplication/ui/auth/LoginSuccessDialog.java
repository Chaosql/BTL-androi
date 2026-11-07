package com.example.myapplication.ui.auth;

import com.example.myapplication.databinding.DialogLoginSuccessBinding;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import android.view.View;
import androidx.appcompat.app.AlertDialog;

public class LoginSuccessDialog extends BasePreviewDialog<DialogLoginSuccessBinding> {
    public LoginSuccessDialog(MainActivity activity) { super(activity, DialogLoginSuccessBinding::inflate); }

    @Override protected void bind(View content, AlertDialog dialog) {
        binding.btnGetStarted.setOnClickListener(view -> { dialog.dismiss(); activity.showScreen(com.example.myapplication.ui.main.UiScreen.MAP); });
        binding.pbLoading.setVisibility(android.view.View.GONE);
    }
}
