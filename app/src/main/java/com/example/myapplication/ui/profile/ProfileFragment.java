package com.example.myapplication.ui.profile;

import com.example.myapplication.databinding.FragmentProfileBinding;
import android.os.Bundle;
import android.view.View;

public class ProfileFragment extends BaseProfileFragment<FragmentProfileBinding> {
    public ProfileFragment() { super(FragmentProfileBinding::inflate); }

    @Override protected ProfileViews profileViews() { return new ProfileViews(binding); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        binding.ivBack.setOnClickListener(view -> back());
        bindProfile(root, false);
    }
}
