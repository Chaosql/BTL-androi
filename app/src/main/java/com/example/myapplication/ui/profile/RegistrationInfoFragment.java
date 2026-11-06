package com.example.myapplication.ui.profile;

import com.example.myapplication.databinding.FragmentRegistrationInfoBinding;
import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;

public class RegistrationInfoFragment extends BaseFragment<FragmentRegistrationInfoBinding> {
    public RegistrationInfoFragment() { super(FragmentRegistrationInfoBinding::inflate); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        binding.etEmail.setText("minhan@example.com");
        binding.etId.setText("STUDY-001");
        binding.etLastName.setText(getString(R.string.ui_studyscreenfragment_text_1));
        binding.etFirstName.setText("Minh An");
        binding.etDateOfBirth.setText("15/08/2005");
        binding.btnSave.setOnClickListener(bindingView -> this.previewNotice());
    }
}
