package com.example.myapplication.ui.profile;

import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.data.remote.dto.request.UpdateStatusTagRequest;
import com.example.myapplication.data.remote.dto.response.UserDto;
import com.example.myapplication.databinding.DialogEditIntroductionBinding;
import com.example.myapplication.ui.main.MainActivity;

public class EditIntroductionDialog extends BaseProfileDialog<DialogEditIntroductionBinding> {
    public EditIntroductionDialog(MainActivity activity) { this(activity, null); }
    public EditIntroductionDialog(MainActivity activity, UserDto user) { super(activity, DialogEditIntroductionBinding::inflate, user); }

    @Override protected void bind(View content, AlertDialog dialog) {
        loadUser(binding.btnSave, user -> binding.etStatusTag.setText(ProfileUi.value(user.statusTag)));
        binding.btnSave.setOnClickListener(view -> submit(repository.updateStatusTag(userId,
                new UpdateStatusTagRequest(ProfileUi.value(binding.etStatusTag))), binding.btnSave, dialog));
    }
}
