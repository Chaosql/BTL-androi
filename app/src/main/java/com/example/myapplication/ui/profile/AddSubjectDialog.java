package com.example.myapplication.ui.profile;

import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.R;
import com.example.myapplication.data.remote.dto.request.AddSubjectRequest;
import com.example.myapplication.databinding.DialogAddSubjectBinding;
import com.example.myapplication.ui.main.MainActivity;

public class AddSubjectDialog extends BaseProfileDialog<DialogAddSubjectBinding> {
    private final Runnable onAdded;
    public AddSubjectDialog(MainActivity activity) { this(activity, () -> { }); }
    AddSubjectDialog(MainActivity activity, Runnable onAdded) {
        super(activity, DialogAddSubjectBinding::inflate, null);
        this.onAdded = onAdded;
    }

    @Override protected void bind(View content, AlertDialog dialog) {
        binding.btnCancelAdd.setOnClickListener(view -> dialog.dismiss());
        binding.btnConfirmAdd.setEnabled(false);
        if (!userId.isEmpty()) request(model.getSubjects(userId, null), response -> {
            if (response.data == null) { error(activity.getString(R.string.profile_empty_response)); return; }
            binding.btnConfirmAdd.setEnabled(true);
        }, failure -> error(failure.message));
        binding.btnConfirmAdd.setOnClickListener(view -> {
            if (!ProfileUi.required(binding.etSubjectName)) return;
            String name = ProfileUi.value(binding.etSubjectName);
            if (model.hasSubject(name)) {
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
            request(model.addSubject(userId, new AddSubjectRequest(name, type)), response -> {
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
