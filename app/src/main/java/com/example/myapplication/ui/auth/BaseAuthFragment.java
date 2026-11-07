package com.example.myapplication.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.viewbinding.ViewBinding;
import com.example.myapplication.R;
import com.example.myapplication.ui.auth.AuthViewModel;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.base.BindingInflater;

public abstract class BaseAuthFragment<B extends ViewBinding> extends BaseFragment<B> {
    public static final String ARG_EMAIL = "email";
    protected BaseAuthFragment(BindingInflater<B> inflater) { super(inflater); }
    protected AuthViewModel authModel() { return viewModel(com.example.myapplication.ui.auth.AuthViewModel.class); }
    protected String email() { return getArguments() == null ? "" : getArguments().getString(ARG_EMAIL, ""); }
    protected String value(EditText view) { return view.getText().toString().trim(); }

    protected boolean validEmail(EditText view) {
        if (AuthViewModel.validEmail(value(view))) { view.setError(null); return true; }
        view.setError(getString(R.string.api_invalid_email));
        view.requestFocus();
        return false;
    }

    protected boolean required(EditText view) {
        if (!value(view).isEmpty()) { view.setError(null); return true; }
        view.setError(getString(R.string.api_required));
        view.requestFocus();
        return false;
    }

    protected boolean passwordPair(EditText password, EditText confirm) {
        if (!AuthViewModel.validPassword(password.getText().toString())) {
            password.setError(getString(R.string.password_policy)); return false;
        }
        if (password.getText().toString().isEmpty()) { password.setError(getString(R.string.api_required)); return false; }
        if (!password.getText().toString().equals(confirm.getText().toString())) {
            confirm.setError(getString(R.string.api_password_mismatch)); return false;
        }
        return true;
    }

    protected void bindFlow(View... buttons) {
        AuthViewModel model = authModel();
        model.flowState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            boolean loading = state.status == com.example.myapplication.ui.base.UiState.Status.LOADING;
            for (View button : buttons) button.setEnabled(!loading);
            if (state.status == com.example.myapplication.ui.base.UiState.Status.ERROR) {
                Toast.makeText(requireContext(), state.error.message, Toast.LENGTH_LONG).show(); model.acknowledgeFlow();
            } else if (state.status == com.example.myapplication.ui.base.UiState.Status.SUCCESS) {
                AuthViewModel.FlowResult result = state.data; model.acknowledgeFlow();
                if (result.notice != 0) Toast.makeText(requireContext(), result.notice, Toast.LENGTH_LONG).show();
                if (result.next != null) go(result.next, flow(result.email, result.reset));
            }
        });
    }

    protected Bundle flow(String email, boolean reset) {
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        args.putBoolean(VerifyOtpFragment.ARG_RESET_PASSWORD, reset);
        return args;
    }

}
