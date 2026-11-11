package com.example.myapplication.ui.auth;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.databinding.FragmentLoginBinding;
import com.example.myapplication.ui.main.UiScreen;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.request.LoginRequest;
import com.example.myapplication.ui.main.MainActivity;

public class LoginFragment extends BaseAuthFragment<FragmentLoginBinding> {
    public LoginFragment() { super(FragmentLoginBinding::inflate); }
    @Override protected void onScreenCreated(View root, Bundle state) {
        bindErrors(binding.tvPasswordError, binding.edtEmail, binding.edtPassword);
        enableScreenCatalog(binding.header, binding.ivPic);
        Runnable artwork = passwordArtwork(binding.ivPic, binding.edtPassword);
        toggle(binding.ivTogglePassword, binding.edtPassword, artwork);
        AuthViewModel model = viewModel(AuthViewModel.class);
        model.signInState().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            binding.tvLogin.setEnabled(result.status != com.example.myapplication.ui.base.UiState.Status.LOADING);
            if (result.status == com.example.myapplication.ui.base.UiState.Status.ERROR) {
                showError(result.error.message);
            } else if (result.status == com.example.myapplication.ui.base.UiState.Status.SUCCESS) {
                model.acknowledgeSignIn();
                ((MainActivity) requireActivity()).showRootScreen(new PreferenceManager(requireContext()).homeScreen());
            }
        });
        binding.tvLogin.setOnClickListener(view -> {
            clearError();
            if (!validEmail(binding.edtEmail) || !required(binding.edtPassword)) return;
            model.authenticate(new LoginRequest(value(binding.edtEmail), binding.edtPassword.getText().toString()));
        });

        binding.tvRegister.setOnClickListener(view -> go(UiScreen.REGISTER));
        binding.tvForgot.setOnClickListener(view -> go(UiScreen.INPUT_EMAIL));
    }
}
