package com.example.myapplication.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.R;
import com.example.myapplication.data.remote.dto.response.SubjectDto;
import com.example.myapplication.databinding.DialogSubjectManagementBinding;
import com.example.myapplication.databinding.ItemDialogSubjectBinding;
import com.example.myapplication.ui.main.MainActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;

public class SubjectManagementDialog extends BaseProfileDialog<DialogSubjectManagementBinding> {
    private boolean loading;
    public SubjectManagementDialog(MainActivity activity) { super(activity, DialogSubjectManagementBinding::inflate, null); }

    @Override protected void bind(View content, AlertDialog dialog) {
        binding.btnCancel.setOnClickListener(view -> dialog.dismiss());
        binding.btnAddSubject.setEnabled(!userId.isEmpty());
        binding.btnAddSubject.setOnClickListener(view -> new AddSubjectDialog(activity, this::load).show());
        load();
    }

    private void load() {
        if (loading || userId.isEmpty() || binding == null) return;
        loading = true;
        binding.btnAddSubject.setEnabled(false);
        request(model.getSubjects(userId, null), response -> {
            loading = false;
            binding.btnAddSubject.setEnabled(true);
            if (response.data == null) { error(activity.getString(R.string.profile_empty_response)); return; }
            render(response.data);
        }, failure -> {
            loading = false;
            binding.btnAddSubject.setEnabled(true);
            error(failure.message);
        });
    }

    private void render(List<SubjectDto> subjects) {
        binding.layoutStrengthSubjects.removeAllViews();
        binding.layoutReviewSubjects.removeAllViews();
        for (SubjectDto subject : subjects) {
            LinearLayout parent = "STRENGTH".equals(subject.type) ? binding.layoutStrengthSubjects : binding.layoutReviewSubjects;
            ItemDialogSubjectBinding row = ItemDialogSubjectBinding.inflate(LayoutInflater.from(activity), parent, false);
            row.tvSubjectName.setText(ProfileUi.value(subject.name));
            row.btnDeleteSubject.setEnabled(subject.id != null && !subject.id.isEmpty());
            row.btnDeleteSubject.setOnClickListener(view -> new MaterialAlertDialogBuilder(activity)
                    .setMessage(activity.getString(R.string.profile_remove_subject, ProfileUi.value(subject.name)))
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.profile_delete, (confirm, which) -> {
                        if (binding == null) return;
                        row.btnDeleteSubject.setEnabled(false);
                        request(model.deleteSubject(userId, subject.id), response -> {
                            parent.removeView(row.getRoot());
                            empty(parent);
                            ProfileUi.saved(activity);
                            load();
                        }, failure -> {
                            row.btnDeleteSubject.setEnabled(true);
                            error(failure.message);
                        });
                    }).show());
            parent.addView(row.getRoot());
        }
        empty(binding.layoutStrengthSubjects);
        empty(binding.layoutReviewSubjects);
    }

    private void empty(LinearLayout container) {
        if (container.getChildCount() == 0) {
            TextView empty = new TextView(activity);
            empty.setText(R.string.profile_empty_subjects);
            empty.setTextColor(activity.getColor(R.color.brown));
            container.addView(empty);
        }
    }
}
