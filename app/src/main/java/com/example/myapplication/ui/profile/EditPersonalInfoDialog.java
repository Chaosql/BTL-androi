package com.example.myapplication.ui.profile;

import com.example.myapplication.databinding.DialogEditPersonalInfoBinding;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import android.app.DatePickerDialog;
import com.example.myapplication.ui.base.BaseFragment;

public class EditPersonalInfoDialog extends BasePreviewDialog<DialogEditPersonalInfoBinding> {
    public EditPersonalInfoDialog(MainActivity activity) { super(activity, DialogEditPersonalInfoBinding::inflate); }

    @Override protected void bind(View content, AlertDialog dialog) {

        binding.etLastName.setText(activity.getString(R.string.ui_studyscreenfragment_text_1));
        binding.etFirstName.setText("Minh An");
        binding.etDateOfBirth.setText("15/08/2005");
        binding.etUniversity.setText(activity.getString(R.string.ui_uidialogs_text_30));
        binding.etMajorName.setText(activity.getString(R.string.ui_uidialogs_text_31));
        binding.etDateOfBirth.setOnClickListener(bindingView -> new DatePickerDialog(activity,
                (picker, year, month, day) -> binding.etDateOfBirth.setText(day + "/" + (month + 1) + "/" + year), 2005, 7, 15).show());
    }
}
