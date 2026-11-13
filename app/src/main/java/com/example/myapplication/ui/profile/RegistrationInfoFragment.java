package com.example.myapplication.ui.profile;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.request.UpdatePersonalInfoRequest;
import com.example.myapplication.data.remote.dto.response.UserDto;
import com.example.myapplication.ui.profile.ProfileViewModel;
import com.example.myapplication.databinding.FragmentRegistrationInfoBinding;
import com.example.myapplication.ui.base.BaseFragment;

public class RegistrationInfoFragment extends BaseFragment<FragmentRegistrationInfoBinding> {
    private UserDto user;
    public RegistrationInfoFragment() { super(FragmentRegistrationInfoBinding::inflate); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        binding.btnBack.setOnClickListener(view -> back());
        genderSpinner(binding.spinnerGender);
        ProfileUi.clearOnEdit(binding.etLastName, binding.tvFormError);
        ProfileUi.clearOnEdit(binding.etFirstName, binding.tvFormError);
        ProfileUi.clearOnEdit(binding.etDateOfBirth, binding.tvFormError);
        binding.etDateOfBirth.setOnClickListener(view -> pickDate(binding.etDateOfBirth));
        binding.btnPickDate.setOnClickListener(view -> pickDate(binding.etDateOfBirth));
        user = null;
        binding.etEmail.setKeyListener(null);
        binding.etEmail.setFocusable(false);
        binding.etId.setKeyListener(null);
        binding.etId.setFocusable(false);
        binding.btnSave.setOnClickListener(view -> {
            ProfileUi.clearErrors(binding.tvFormError);
            if (user == null) { load(); return; }
            if (!ProfileUi.required(binding.etLastName, binding.tvFormError) || !ProfileUi.required(binding.etFirstName, binding.tvFormError)) return;
            String birth = ProfileUi.birthDate(binding.etDateOfBirth, binding.tvFormError);
            if (birth == null) return;
            binding.btnSave.setEnabled(false);
            ProfileUi.inputs(binding.getRoot(), false);
            UpdatePersonalInfoRequest change = new UpdatePersonalInfoRequest(ProfileUi.value(binding.etFirstName),
                    ProfileUi.value(binding.etLastName), ProfileUi.gender(binding.spinnerGender.getSelectedItemPosition()),
                    birth, user.phone, user.email);
            request(viewModel(com.example.myapplication.ui.profile.ProfileViewModel.class).updatePersonalInfo(new PreferenceManager(requireContext()).getUserId(), change),
                    response -> {
                        binding.btnSave.setEnabled(true);
                        ProfileUi.inputs(binding.getRoot(), true);
                        ProfileUi.saved(requireActivity());
                    }, error -> {
                        binding.btnSave.setEnabled(true);
                        ProfileUi.inputs(binding.getRoot(), true);
                        ProfileUi.showError(binding.tvFormError, error.message);
                    });
        });
        load();
    }

    private void load() {
        ProfileUi.clearErrors(binding.tvFormError);
        binding.btnSave.setEnabled(false);
        ProfileUi.inputs(binding.getRoot(), false);
        if (!new PreferenceManager(requireContext()).isLogin()) return;
        request(viewModel(com.example.myapplication.ui.profile.ProfileViewModel.class).getCurrentUser(), response -> {
            binding.btnSave.setEnabled(true);
            if (response.data == null) {
                ProfileUi.showError(binding.tvFormError, getString(R.string.profile_empty_response)); return;
            }
            user = response.data;
            ProfileUi.inputs(binding.getRoot(), true);
            binding.etEmail.setText(ProfileUi.value(user.email));
            binding.etId.setText(ProfileUi.value(user.id));
            binding.etLastName.setText(ProfileUi.value(user.lastName));
            binding.etFirstName.setText(ProfileUi.value(user.firstName));
            binding.etDateOfBirth.setText(ProfileUi.displayDate(user.dateOfBirth));
            binding.spinnerGender.setSelection(ProfileUi.genderIndex(user.gender));
        }, error -> {
            binding.btnSave.setEnabled(true);
            ProfileUi.showError(binding.tvFormError, error.message);
        });
    }

    @Override public void onDestroyView() { user = null; super.onDestroyView(); }
}
