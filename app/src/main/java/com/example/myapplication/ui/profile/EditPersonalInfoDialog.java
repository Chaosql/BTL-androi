package com.example.myapplication.ui.profile;

import android.app.DatePickerDialog;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.R;
import com.example.myapplication.data.remote.dto.request.UpdateAcademicInfoRequest;
import com.example.myapplication.data.remote.dto.request.UpdatePersonalInfoRequest;
import com.example.myapplication.data.remote.dto.request.UpdateProfileLocationRequest;
import com.example.myapplication.data.remote.dto.response.BaseResponse;
import com.example.myapplication.data.remote.dto.response.UserDto;
import com.example.myapplication.databinding.DialogEditPersonalInfoBinding;
import com.example.myapplication.ui.main.MainActivity;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import retrofit2.Call;

public class EditPersonalInfoDialog extends BaseProfileDialog<DialogEditPersonalInfoBinding> {
    private UserDto user;
    public EditPersonalInfoDialog(MainActivity activity) { this(activity, null); }
    public EditPersonalInfoDialog(MainActivity activity, UserDto user) { super(activity, DialogEditPersonalInfoBinding::inflate, user); }

    @Override protected void bind(View content, AlertDialog dialog) {
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
        if (!phone.isEmpty() && !phone.matches("[+0-9() .-]{6,20}")) {
            binding.etPhone.setError(activity.getString(R.string.profile_invalid_phone)); return;
        }
        String gender = binding.rbMale.isChecked() ? "MALE" : binding.rbFemale.isChecked() ? "FEMALE" : "OTHER";
        UpdatePersonalInfoRequest personal = new UpdatePersonalInfoRequest(ProfileUi.value(binding.etFirstName),
                ProfileUi.value(binding.etLastName), gender, date, phone, user.email);
        UpdateAcademicInfoRequest academic = new UpdateAcademicInfoRequest(ProfileUi.value(binding.etUniversity), ProfileUi.value(binding.etMajorName));
        String location = ProfileUi.value(binding.etLocation);
        List<Call<BaseResponse<UserDto>>> changes = new ArrayList<>();
        if (!Objects.equals(personal.firstName, ProfileUi.value(user.firstName))
                || !Objects.equals(personal.lastName, ProfileUi.value(user.lastName))
                || !Objects.equals(personal.gender, user.gender)
                || !Objects.equals(ProfileUi.date(personal.dateOfBirth), ProfileUi.date(user.dateOfBirth))
                || !Objects.equals(personal.phone, ProfileUi.value(user.phone)))
            changes.add(repository.updatePersonalInfo(userId, personal));
        if (!Objects.equals(academic.university, ProfileUi.value(user.university)) || !Objects.equals(academic.majorName, ProfileUi.value(user.majorName)))
            changes.add(repository.updateAcademicInfo(userId, academic));
        if (!Objects.equals(location, ProfileUi.value(user.location)))
            changes.add(repository.updateProfileLocation(userId, new UpdateProfileLocationRequest(location)));
        ProfileUi.inputs(binding.getRoot(), false);
        binding.btnSave.setEnabled(false);
        binding.btnCancel.setEnabled(false);
        dialog.setCancelable(false);
        saveNext(changes, 0, dialog);
    }

    private void saveNext(List<Call<BaseResponse<UserDto>>> changes, int index, AlertDialog dialog) {
        if (index == changes.size()) { ProfileUi.saved(activity); dialog.dismiss(); return; }
        request(changes.get(index), response -> saveNext(changes, index + 1, dialog), failure -> {
            ProfileUi.inputs(binding.getRoot(), true);
            binding.btnSave.setEnabled(true);
            binding.btnCancel.setEnabled(true);
            dialog.setCancelable(true);
            if (index > 0) {
                ProfileUi.updated(activity);
                error(activity.getString(R.string.profile_partial_save) + "\n" + failure.message);
            } else {
                error(failure.message);
            }
        });
    }
}
