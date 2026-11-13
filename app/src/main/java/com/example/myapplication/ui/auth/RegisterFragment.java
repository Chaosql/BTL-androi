package com.example.myapplication.ui.auth;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentRegisterBinding;
import com.example.myapplication.ui.main.UiScreen;
import java.time.LocalDate;
import com.example.myapplication.data.remote.dto.request.RegisterRequest;

public class RegisterFragment extends BaseAuthFragment<FragmentRegisterBinding> {
    public RegisterFragment() { super(FragmentRegisterBinding::inflate); }
    @Override protected void onScreenCreated(View root, Bundle state) {
        bindErrors(binding.tvPasswordError, binding.edtEmail, binding.edtLastName, binding.edtFirstName, binding.edtDate, binding.edtPassword, binding.edtConfirmPassword);
        bindFlow(binding.tvRegister);
        Runnable artwork = passwordArtwork(binding.ivPic, binding.edtPassword, binding.edtConfirmPassword);
        toggle(binding.ivTogglePassword, binding.edtPassword, artwork);
        toggle(binding.ivToggleConfirmPassword, binding.edtConfirmPassword, artwork);
        binding.btnBack.setOnClickListener(view -> back());
        genderSpinner(binding.spGender);
        binding.edtDate.setOnClickListener(view -> pickDate(binding.edtDate));
        binding.ivCalendar.setOnClickListener(view -> pickDate(binding.edtDate));
        binding.tvLogin.setOnClickListener(view -> go(UiScreen.LOGIN));
        binding.tvRegister.setOnClickListener(view -> {
            clearError();
            if (!validEmail(binding.edtEmail) || !required(binding.edtLastName) || !required(binding.edtFirstName)
                    || !passwordPair(binding.edtPassword, binding.edtConfirmPassword)) return;
            LocalDate birth;
            try {
                birth = AuthViewModel.birthDate(value(binding.edtDate));
            } catch (Exception error) { fieldError(binding.edtDate, R.string.api_invalid_date); return; }
            String email = value(binding.edtEmail);
            String gender = new String[]{"MALE", "FEMALE", "OTHER"}[binding.spGender.getSelectedItemPosition()];
            RegisterRequest request = new RegisterRequest(email, binding.edtPassword.getText().toString(),
                    binding.edtConfirmPassword.getText().toString(), value(binding.edtFirstName),
                    value(binding.edtLastName), gender, birth.toString());
            authModel().registerAccount(request);
        });
    }
}
