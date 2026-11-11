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
    private android.widget.TextView errorView;
    public static final String ARG_EMAIL = "email";
    protected BaseAuthFragment(BindingInflater<B> inflater) { super(inflater); }
    protected AuthViewModel authModel() { return viewModel(com.example.myapplication.ui.auth.AuthViewModel.class); }
    protected String email() { return getArguments() == null ? "" : getArguments().getString(ARG_EMAIL, ""); }
    protected String value(EditText view) { return view.getText().toString().trim(); }

    protected Runnable passwordArtwork(android.widget.ImageView artwork, EditText... passwords) {
        return () -> {
            boolean visible = false;
            for (EditText password : passwords)
                visible |= !(password.getTransformationMethod() instanceof android.text.method.PasswordTransformationMethod);
            artwork.setImageResource(visible ? R.drawable.frame1 : R.drawable.frame3);
        };
    }

    protected void bindErrors(android.widget.TextView view, EditText... inputs) {
        errorView = view;
        view.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        for (EditText input : inputs) input.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence text, int start, int before, int count) {
                if (getViewLifecycleOwner().getLifecycle().getCurrentState().isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) clearError();
            }
            @Override public void afterTextChanged(android.text.Editable text) { }
        });
    }
    protected void showError(String message) {
        errorView.setText(message); errorView.setVisibility(View.VISIBLE);
    }
    protected void clearError() {
        if (errorView == null) return;
        errorView.setText(null); errorView.setVisibility(View.GONE);
        AuthViewModel model = authModel();
        if (model.flowState().getValue() != null && model.flowState().getValue().status == com.example.myapplication.ui.base.UiState.Status.ERROR)
            model.acknowledgeFlow();
        if (model.signInState().getValue() != null && model.signInState().getValue().status == com.example.myapplication.ui.base.UiState.Status.ERROR)
            model.acknowledgeSignIn();
    }
    protected boolean fieldError(EditText view, int message) {
        view.requestFocus(); showError(getString(message)); return false;
    }
    protected boolean validEmail(EditText view) {
        if (value(view).isEmpty()) return fieldError(view, R.string.auth_email_required);
        return AuthViewModel.validEmail(value(view)) || fieldError(view, R.string.api_invalid_email);
    }
    protected boolean required(EditText view) {
        if (!value(view).isEmpty()) return true;
        int message = view.getId() == R.id.edtLastName ? R.string.auth_last_name_required
                : view.getId() == R.id.edtFirstName ? R.string.auth_first_name_required
                : R.string.auth_password_required;
        return fieldError(view, message);
    }
    protected boolean passwordPair(EditText password, EditText confirm) {
        if (!required(password)) return false;
        if (!AuthViewModel.validPassword(password.getText().toString())) return fieldError(password, R.string.password_policy);
        if (confirm.length() == 0) return fieldError(confirm, R.string.auth_confirm_required);
        return password.getText().toString().equals(confirm.getText().toString())
                || fieldError(confirm, R.string.api_password_mismatch);
    }
    @Override public void onDestroyView() {
        errorView = null;
        super.onDestroyView();
    }

    protected void bindFlow(View... buttons) {
        AuthViewModel model = authModel();
        model.flowState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            boolean loading = state.status == com.example.myapplication.ui.base.UiState.Status.LOADING;
            for (View button : buttons) button.setEnabled(!loading);
            if (loading) { errorView.setText(null); errorView.setVisibility(View.GONE); }
            if (state.status == com.example.myapplication.ui.base.UiState.Status.ERROR) {
                showError(state.error.message);
            } else if (state.status == com.example.myapplication.ui.base.UiState.Status.SUCCESS) {
                AuthViewModel.FlowResult result = state.data; model.acknowledgeFlow();
                if (result.notice != 0) Toast.makeText(requireContext(), result.notice, Toast.LENGTH_LONG).show();
                if (result.next != null) {
                    Bundle args = flow(result.email, result.reset);
                    if (result.next == com.example.myapplication.ui.main.UiScreen.VERIFY_OTP)
                        args.putBoolean(VerifyOtpFragment.ARG_OTP_SENT, true);
                    go(result.next, args);
                }
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
