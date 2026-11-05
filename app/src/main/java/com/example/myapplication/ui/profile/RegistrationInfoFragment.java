package com.example.myapplication.ui.profile;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;

public class RegistrationInfoFragment extends BaseFragment {
    public RegistrationInfoFragment() { super(R.layout.fragment_registration_info); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        text(root, R.id.etEmail, "minhan@example.com");
        text(root, R.id.etId, "STUDY-001");
        text(root, R.id.etLastName, getString(R.string.ui_studyscreenfragment_text_1));
        text(root, R.id.etFirstName, "Minh An");
        text(root, R.id.etDateOfBirth, "15/08/2005");
        on(root, R.id.btnSave, this::previewNotice);
    }
}
