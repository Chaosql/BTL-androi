package com.example.myapplication.ui.auth;

import com.example.myapplication.databinding.DialogLoginSuccessBinding;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.ui.base.BaseFragment;

public class LoginSuccessDialog extends BasePreviewDialog<DialogLoginSuccessBinding> {
    public LoginSuccessDialog(MainActivity activity) { super(activity, DialogLoginSuccessBinding::inflate); }

    @Override protected void bind(View content, AlertDialog dialog) {
        binding.pbLoading.setVisibility(android.view.View.GONE);
    }
}
