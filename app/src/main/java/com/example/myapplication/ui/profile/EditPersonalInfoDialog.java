package com.example.myapplication.ui.profile;

import com.example.myapplication.R;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import android.app.DatePickerDialog;
import com.example.myapplication.ui.base.BaseFragment;

public class EditPersonalInfoDialog extends BasePreviewDialog {
    public EditPersonalInfoDialog(MainActivity activity) { super(activity, R.layout.dialog_edit_personal_info); }

    @Override protected void bind(View content, AlertDialog dialog) {

        BaseFragment.text(content, R.id.etLastName, activity.getString(R.string.ui_studyscreenfragment_text_1));
        BaseFragment.text(content, R.id.etFirstName, "Minh An");
        BaseFragment.text(content, R.id.etDateOfBirth, "15/08/2005");
        BaseFragment.text(content, R.id.etUniversity, activity.getString(R.string.ui_uidialogs_text_30));
        BaseFragment.text(content, R.id.etMajorName, activity.getString(R.string.ui_uidialogs_text_31));
        BaseFragment.on(content, R.id.etDateOfBirth, () -> new DatePickerDialog(activity,
                (picker, year, month, day) -> BaseFragment.text(content, R.id.etDateOfBirth,
                        day + "/" + (month + 1) + "/" + year), 2005, 7, 15).show());
    }
}
