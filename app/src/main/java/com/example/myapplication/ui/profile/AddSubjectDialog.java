package com.example.myapplication.ui.profile;

import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.R;
import com.example.myapplication.data.remote.dto.request.AddSubjectRequest;
import com.example.myapplication.databinding.DialogAddSubjectBinding;
import com.example.myapplication.ui.main.MainActivity;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class AddSubjectDialog extends BaseProfileDialog<DialogAddSubjectBinding> {
    private final Set<String> existing = new HashSet<>();
    private final Runnable onAdded;
    public AddSubjectDialog(MainActivity activity) { this(activity, () -> { }); }
    AddSubjectDialog(MainActivity activity, Runnable onAdded) {
        super(activity, DialogAddSubjectBinding::inflate, null);
        this.onAdded = onAdded;
    }

    @Override protected void bind(View content, AlertDialog dialog) {
        binding.btnConfirmAdd.setEnabled(false);
        if (!userId.isEmpty()) request(repository.getSubjects(userId, null), response -> {
            if (response.data == null) { error(activity.getString(R.string.profile_empty_response)); return; }
            for (com.example.myapplication.data.remote.dto.response.SubjectDto subject : response.data)
                existing.add(ProfileUi.value(subject.name).trim().toLowerCase(Locale.ROOT));
            binding.btnConfirmAdd.setEnabled(true);
        }, failure -> error(failure.message));
        binding.btnConfirmAdd.setOnClickListener(view -> {
            if (!ProfileUi.required(binding.etSubjectName)) return;
            String name = ProfileUi.value(binding.etSubjectName);
            if (existing.contains(name.toLowerCase(Locale.ROOT))) {
                binding.etSubjectName.setError(activity.getString(R.string.profile_duplicate_subject)); return;
            }
            if (binding.rgSubjectType.getCheckedRadioButtonId() == -1) {
                error(activity.getString(R.string.profile_choose_type)); return;
            }
            String type = binding.rbStrength.isChecked() ? "STRENGTH" : "NEED_REVIEW";
            binding.btnConfirmAdd.setEnabled(false);
            binding.btnCancelAdd.setEnabled(false);
            ProfileUi.inputs(binding.getRoot(), false);
            dialog.setCancelable(false);
            request(repository.addSubject(userId, new AddSubjectRequest(name, type)), response -> {
                ProfileUi.saved(activity);
                dialog.dismiss();
                onAdded.run();
            }, failure -> {
                binding.btnConfirmAdd.setEnabled(true);
                binding.btnCancelAdd.setEnabled(true);
                ProfileUi.inputs(binding.getRoot(), true);
                dialog.setCancelable(true);
                error(failure.message);
            });
        });
    }
}
