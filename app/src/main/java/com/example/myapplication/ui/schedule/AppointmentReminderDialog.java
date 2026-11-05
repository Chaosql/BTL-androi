package com.example.myapplication.ui.schedule;

import com.example.myapplication.R;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.ui.base.BaseFragment;

public class AppointmentReminderDialog extends BasePreviewDialog {
    public AppointmentReminderDialog(MainActivity activity) { super(activity, R.layout.dialog_appointment_reminder); }

    @Override protected void bind(View content, AlertDialog dialog) {

        BaseFragment.text(content, R.id.tvSubject, activity.getString(R.string.ui_uidialogs_text_24));
        BaseFragment.text(content, R.id.tvTime, activity.getString(R.string.ui_uidialogs_text_25));
        BaseFragment.text(content, R.id.tvLocation, activity.getString(R.string.ui_uidialogs_text_22));
    }
}
