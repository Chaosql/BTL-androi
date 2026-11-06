package com.example.myapplication.ui.profile;

import com.example.myapplication.databinding.DialogSubjectManagementBinding;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import android.widget.LinearLayout;
import android.view.LayoutInflater;
import com.example.myapplication.ui.base.BaseFragment;

public class SubjectManagementDialog extends BasePreviewDialog<DialogSubjectManagementBinding> {
    public SubjectManagementDialog(MainActivity activity) { super(activity, DialogSubjectManagementBinding::inflate); }

    @Override protected void bind(View content, AlertDialog dialog) {

        subject(content, R.id.layoutStrengthSubjects, activity.getString(R.string.ui_uidialogs_text_19));
        subject(content, R.id.layoutStrengthSubjects, activity.getString(R.string.ui_uidialogs_text_24));
        subject(content, R.id.layoutReviewSubjects, activity.getString(R.string.ui_uidialogs_text_38));
    }

    private static void subject(View root, int containerId, String name) {
        LinearLayout container = root.findViewById(containerId);
        com.example.myapplication.databinding.ItemDialogSubjectBinding row =
                com.example.myapplication.databinding.ItemDialogSubjectBinding.inflate(LayoutInflater.from(root.getContext()), container, false);
        row.tvSubjectName.setText(name);
        row.btnDeleteSubject.setOnClickListener(view -> container.removeView(row.getRoot()));
        container.addView(row.getRoot());
    }
}
