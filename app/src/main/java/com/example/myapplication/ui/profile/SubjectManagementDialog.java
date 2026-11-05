package com.example.myapplication.ui.profile;

import com.example.myapplication.R;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import android.widget.LinearLayout;
import android.view.LayoutInflater;
import com.example.myapplication.ui.base.BaseFragment;

public class SubjectManagementDialog extends BasePreviewDialog {
    public SubjectManagementDialog(MainActivity activity) { super(activity, R.layout.dialog_subject_management); }

    @Override protected void bind(View content, AlertDialog dialog) {

        subject(content, R.id.layoutStrengthSubjects, activity.getString(R.string.ui_uidialogs_text_19));
        subject(content, R.id.layoutStrengthSubjects, activity.getString(R.string.ui_uidialogs_text_24));
        subject(content, R.id.layoutReviewSubjects, activity.getString(R.string.ui_uidialogs_text_38));
    }

    private static void subject(View root, int containerId, String name) {
        LinearLayout container = root.findViewById(containerId);
        View row = LayoutInflater.from(root.getContext()).inflate(R.layout.item_dialog_subject, container, false);
        BaseFragment.text(row, R.id.tvSubjectName, name);
        BaseFragment.on(row, R.id.btnDeleteSubject, () -> container.removeView(row));
        container.addView(row);
    }
}
