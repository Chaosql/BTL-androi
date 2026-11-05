package com.example.myapplication.ui.profile;

import com.example.myapplication.R;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import android.view.View;
import androidx.appcompat.app.AlertDialog;

public class AddSubjectDialog extends BasePreviewDialog {
    public AddSubjectDialog(MainActivity activity) { super(activity, R.layout.dialog_add_subject); }

    @Override protected void bind(View content, AlertDialog dialog) {

    }
}
