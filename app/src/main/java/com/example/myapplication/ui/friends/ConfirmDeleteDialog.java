package com.example.myapplication.ui.friends;

import com.example.myapplication.R;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import android.view.View;
import androidx.appcompat.app.AlertDialog;

public class ConfirmDeleteDialog extends BasePreviewDialog {
    public ConfirmDeleteDialog(MainActivity activity) { super(activity, R.layout.dialog_confirm_delete); }

    @Override protected void bind(View content, AlertDialog dialog) {

    }
}
