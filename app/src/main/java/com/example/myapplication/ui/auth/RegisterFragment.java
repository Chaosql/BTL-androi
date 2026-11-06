package com.example.myapplication.ui.auth;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentRegisterBinding;
import com.example.myapplication.ui.main.UiScreen;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import com.example.myapplication.data.remote.dto.request.RegisterRequest;

public class RegisterFragment extends BaseAuthFragment<FragmentRegisterBinding> {
    public RegisterFragment() { super(FragmentRegisterBinding::inflate); }
    @Override protected void onScreenCreated(View root, Bundle state) {
        binding.tvLogin.setOnClickListener(view -> go(UiScreen.LOGIN));
        binding.tvRegister.setOnClickListener(view -> {
            if (!validEmail(binding.edtEmail) || !required(binding.edtLastName) || !required(binding.edtFirstName)
                    || !passwordPair(binding.edtPassword, binding.edtConfirmPassword)) return;
            LocalDate birth;
            try {
                birth = LocalDate.parse(value(binding.edtDate),
                        DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT));
                if (!birth.isBefore(LocalDate.now())) throw new IllegalArgumentException();
            } catch (Exception error) { binding.edtDate.setError(getString(R.string.api_invalid_date)); return; }
            String email = value(binding.edtEmail);
            String gender = new String[]{"MALE", "FEMALE", "OTHER"}[binding.spGender.getSelectedItemPosition()];
            RegisterRequest request = new RegisterRequest(email, binding.edtPassword.getText().toString(),
                    binding.edtConfirmPassword.getText().toString(), value(binding.edtFirstName),
                    value(binding.edtLastName), gender, birth.toString());
            submit(repository().register(request), binding.tvRegister,
                    response -> go(UiScreen.VERIFY_EMAIL, flow(email, false)));
        });
    }
}
