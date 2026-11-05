package com.example.myapplication.ui.auth;

import com.example.myapplication.R;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.ui.base.BaseFragment;

public class LoginSuccessDialog extends BasePreviewDialog {
    public LoginSuccessDialog(MainActivity activity) { super(activity, R.layout.dialog_login_success); }

    @Override protected void bind(View content, AlertDialog dialog) {
        BaseFragment.hide(content, R.id.pbLoading);
    }
}
