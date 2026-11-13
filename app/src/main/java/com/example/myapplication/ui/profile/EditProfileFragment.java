package com.example.myapplication.ui.profile;

import com.example.myapplication.databinding.FragmentEditProfileBinding;
import android.os.Bundle;
import android.view.View;

public class EditProfileFragment extends BaseProfileFragment<FragmentEditProfileBinding> {
    public EditProfileFragment() { super(FragmentEditProfileBinding::inflate); }

    @Override protected ProfileViews profileViews() { return new ProfileViews(binding); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        binding.ivBack.setOnClickListener(view -> back());
        bindProfile(root, true);
    }
}
