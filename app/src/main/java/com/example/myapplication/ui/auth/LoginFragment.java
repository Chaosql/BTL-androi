package com.example.myapplication.ui.auth;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentLoginBinding;
import com.example.myapplication.ui.main.UiScreen;
import android.widget.Toast;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.request.LoginRequest;
import com.example.myapplication.ui.main.MainActivity;

public class LoginFragment extends BaseAuthFragment<FragmentLoginBinding> {
    public LoginFragment() { super(FragmentLoginBinding::inflate); }
    @Override protected void onScreenCreated(View root, Bundle state) {
        binding.tvLogin.setOnClickListener(view -> {
            if (!validEmail(binding.edtEmail) || !required(binding.edtPassword)) return;
            String email = value(binding.edtEmail);
            submit(repository().login(new LoginRequest(email, binding.edtPassword.getText().toString())), binding.tvLogin, response -> {
                try { new PreferenceManager(requireContext()).saveLogin(response.data, email); }
                catch (IllegalArgumentException error) {
                    Toast.makeText(requireContext(), R.string.api_invalid_login, Toast.LENGTH_LONG).show(); return;
                }
                ((MainActivity) requireActivity()).showRootScreen(new PreferenceManager(requireContext()).homeScreen());
            });
        });

        binding.tvRegister.setOnClickListener(view -> go(UiScreen.REGISTER));
        binding.tvForgot.setOnClickListener(view -> go(UiScreen.INPUT_EMAIL));
    }
}
