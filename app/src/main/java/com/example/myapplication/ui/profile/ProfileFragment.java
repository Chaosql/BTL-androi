package com.example.myapplication.ui.profile;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;

public class ProfileFragment extends BaseProfileFragment {
    public ProfileFragment() { super(R.layout.fragment_profile); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        bindProfile(root, false);
    }
}
