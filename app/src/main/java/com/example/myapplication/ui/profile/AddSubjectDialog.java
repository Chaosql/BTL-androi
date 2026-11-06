package com.example.myapplication.ui.profile;

import com.example.myapplication.databinding.DialogAddSubjectBinding;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import android.view.View;
import androidx.appcompat.app.AlertDialog;

public class AddSubjectDialog extends BasePreviewDialog<DialogAddSubjectBinding> {
    public AddSubjectDialog(MainActivity activity) { super(activity, DialogAddSubjectBinding::inflate); }

    @Override protected void bind(View content, AlertDialog dialog) {

    }
}
