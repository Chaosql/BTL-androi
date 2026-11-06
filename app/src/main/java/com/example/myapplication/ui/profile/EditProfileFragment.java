package com.example.myapplication.ui.profile;

import com.example.myapplication.databinding.FragmentEditProfileBinding;
import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;

public class EditProfileFragment extends BaseProfileFragment<FragmentEditProfileBinding> {
    public EditProfileFragment() { super(FragmentEditProfileBinding::inflate); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        bindProfile(root, true);
    }
}
