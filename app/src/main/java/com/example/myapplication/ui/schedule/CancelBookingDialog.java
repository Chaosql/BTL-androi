package com.example.myapplication.ui.schedule;

import com.example.myapplication.R;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import android.view.View;
import androidx.appcompat.app.AlertDialog;

public class CancelBookingDialog extends BasePreviewDialog {
    public CancelBookingDialog(MainActivity activity) { super(activity, R.layout.dialog_cancel_reason); }

    @Override protected void bind(View content, AlertDialog dialog) {

    }
}
