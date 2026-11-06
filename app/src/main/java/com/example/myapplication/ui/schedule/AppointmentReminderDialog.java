package com.example.myapplication.ui.schedule;

import com.example.myapplication.databinding.DialogAppointmentReminderBinding;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.ui.base.BaseFragment;

public class AppointmentReminderDialog extends BasePreviewDialog<DialogAppointmentReminderBinding> {
    public AppointmentReminderDialog(MainActivity activity) { super(activity, DialogAppointmentReminderBinding::inflate); }

    @Override protected void bind(View content, AlertDialog dialog) {

        binding.tvSubject.setText(activity.getString(R.string.ui_uidialogs_text_24));
        binding.tvTime.setText(activity.getString(R.string.ui_uidialogs_text_25));
        binding.tvLocation.setText(activity.getString(R.string.ui_uidialogs_text_22));
    }
}
