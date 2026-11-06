package com.example.myapplication.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.example.myapplication.R;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.request.UpdatePersonalInfoRequest;
import com.example.myapplication.data.remote.dto.response.UserDto;
import com.example.myapplication.data.repository.profile.ProfileRepository;
import com.example.myapplication.databinding.FragmentRegistrationInfoBinding;
import com.example.myapplication.ui.base.BaseFragment;

public class RegistrationInfoFragment extends BaseFragment<FragmentRegistrationInfoBinding> {
    private UserDto user;
    public RegistrationInfoFragment() { super(FragmentRegistrationInfoBinding::inflate); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        user = null;
        binding.etEmail.setKeyListener(null);
        binding.etEmail.setFocusable(false);
        binding.etId.setKeyListener(null);
        binding.etId.setFocusable(false);
        binding.btnSave.setOnClickListener(view -> {
            if (user == null) { load(); return; }
            if (!ProfileUi.required(binding.etLastName) || !ProfileUi.required(binding.etFirstName)) return;
            String birth = ProfileUi.birthDate(binding.etDateOfBirth);
            if (birth == null) return;
            binding.btnSave.setEnabled(false);
            ProfileUi.inputs(binding.getRoot(), false);
            UpdatePersonalInfoRequest change = new UpdatePersonalInfoRequest(ProfileUi.value(binding.etFirstName),
                    ProfileUi.value(binding.etLastName), ProfileUi.gender(binding.spinnerGender.getSelectedItemPosition()),
                    birth, user.phone, user.email);
            request(new ProfileRepository(requireContext()).updatePersonalInfo(new PreferenceManager(requireContext()).getUserId(), change),
                    response -> {
                        binding.btnSave.setEnabled(true);
                        ProfileUi.inputs(binding.getRoot(), true);
                        ProfileUi.saved(requireActivity());
                    }, error -> {
                        binding.btnSave.setEnabled(true);
                        ProfileUi.inputs(binding.getRoot(), true);
                        Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show();
                    });
        });
        load();
    }

    private void load() {
        binding.btnSave.setEnabled(false);
        ProfileUi.inputs(binding.getRoot(), false);
        if (!new PreferenceManager(requireContext()).isLogin()) return;
        request(new ProfileRepository(requireContext()).getCurrentUser(), response -> {
            binding.btnSave.setEnabled(true);
            if (response.data == null) {
                Toast.makeText(requireContext(), R.string.profile_empty_response, Toast.LENGTH_LONG).show(); return;
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
            Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show();
        });
    }

    @Override public void onDestroyView() { user = null; super.onDestroyView(); }
}
