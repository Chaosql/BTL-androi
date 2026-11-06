package com.example.myapplication.ui.profile;

import com.example.myapplication.databinding.FragmentProfileBinding;
import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;

public class ProfileFragment extends BaseProfileFragment<FragmentProfileBinding> {
    public ProfileFragment() { super(FragmentProfileBinding::inflate); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        bindProfile(root, false);
    }
}
