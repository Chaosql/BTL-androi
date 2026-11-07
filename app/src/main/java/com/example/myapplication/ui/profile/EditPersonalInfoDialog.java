package com.example.myapplication.ui.profile;

import android.app.DatePickerDialog;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.R;
import com.example.myapplication.data.remote.dto.request.UpdateAcademicInfoRequest;
import com.example.myapplication.data.remote.dto.request.UpdatePersonalInfoRequest;
import com.example.myapplication.data.remote.dto.response.UserDto;
import com.example.myapplication.databinding.DialogEditPersonalInfoBinding;
import com.example.myapplication.ui.main.MainActivity;
import java.time.LocalDate;

public class EditPersonalInfoDialog extends BaseProfileDialog<DialogEditPersonalInfoBinding> {
    private UserDto user;
    public EditPersonalInfoDialog(MainActivity activity) { this(activity, null); }
    public EditPersonalInfoDialog(MainActivity activity, UserDto user) { super(activity, DialogEditPersonalInfoBinding::inflate, user); }

    @Override protected void bind(View content, AlertDialog dialog) {
        binding.btnCancel.setOnClickListener(view -> dialog.dismiss());
        observe(model.personalSave(), state -> {
            boolean saving = state.status == com.example.myapplication.ui.base.UiState.Status.LOADING;
            ProfileUi.inputs(binding.getRoot(), !saving);
            binding.btnSave.setEnabled(!saving); binding.btnCancel.setEnabled(!saving); dialog.setCancelable(!saving);
            if (state.status == com.example.myapplication.ui.base.UiState.Status.SUCCESS) { ProfileUi.saved(activity); dialog.dismiss(); }
            else if (state.status == com.example.myapplication.ui.base.UiState.Status.ERROR) { ProfileUi.updated(activity); error(state.error.message); }
        });
        loadUser(binding.btnSave, data -> {
            user = data;
            binding.etLastName.setText(ProfileUi.value(data.lastName));
            binding.etFirstName.setText(ProfileUi.value(data.firstName));
            binding.etDateOfBirth.setText(ProfileUi.displayDate(data.dateOfBirth));
            binding.etUniversity.setText(ProfileUi.value(data.university));
            binding.etMajorName.setText(ProfileUi.value(data.majorName));
            binding.etPhone.setText(ProfileUi.value(data.phone));
            binding.etLocation.setText(ProfileUi.value(data.location));
            binding.rgGender.check("MALE".equals(data.gender) ? binding.rbMale.getId() :
                    "FEMALE".equals(data.gender) ? binding.rbFemale.getId() : binding.rbOther.getId());
        });
        binding.etDateOfBirth.setOnClickListener(view -> {
            LocalDate date = ProfileUi.date(ProfileUi.value(binding.etDateOfBirth));
            if (date == null) date = LocalDate.now().minusYears(18);
            DatePickerDialog picker = new DatePickerDialog(activity, (selected, year, month, day) ->
                    binding.etDateOfBirth.setText(ProfileUi.displayDate(LocalDate.of(year, month + 1, day).toString())),
                    date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
            picker.getDatePicker().setMaxDate(System.currentTimeMillis());
            picker.show();
        });
        binding.btnSave.setOnClickListener(view -> save(dialog));
    }

    private void save(AlertDialog dialog) {
        if (user == null || !ProfileUi.required(binding.etLastName) || !ProfileUi.required(binding.etFirstName)) return;
        String date = ProfileUi.birthDate(binding.etDateOfBirth);
        if (date == null) return;
        String phone = ProfileUi.value(binding.etPhone);
        if (!ProfileViewModel.validPhone(phone)) {
            binding.etPhone.setError(activity.getString(R.string.profile_invalid_phone)); return;
        }
        String gender = binding.rbMale.isChecked() ? "MALE" : binding.rbFemale.isChecked() ? "FEMALE" : "OTHER";
        UpdatePersonalInfoRequest personal = new UpdatePersonalInfoRequest(ProfileUi.value(binding.etFirstName),
                ProfileUi.value(binding.etLastName), gender, date, phone, user.email);
        UpdateAcademicInfoRequest academic = new UpdateAcademicInfoRequest(ProfileUi.value(binding.etUniversity), ProfileUi.value(binding.etMajorName));
        String location = ProfileUi.value(binding.etLocation);
        model.savePersonal(user, personal, academic, location);
    }
}
