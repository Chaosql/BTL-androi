package com.example.myapplication.ui.profile;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;

public class EditProfileFragment extends BaseProfileFragment {
    public EditProfileFragment() { super(R.layout.fragment_edit_profile); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        bindProfile(root, true);
    }
}
