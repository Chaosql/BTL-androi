package com.example.myapplication.ui.auth;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.viewbinding.ViewBinding;
import com.example.myapplication.R;
import com.example.myapplication.data.remote.network.ApiError;
import com.example.myapplication.data.repository.auth.AuthRepository;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.base.BindingInflater;
import java.util.function.Consumer;
import retrofit2.Call;

public abstract class BaseAuthFragment<B extends ViewBinding> extends BaseFragment<B> {
    public static final String ARG_EMAIL = "email";
    private boolean busy;
    protected BaseAuthFragment(BindingInflater<B> inflater) { super(inflater); }
    protected AuthRepository repository() { return new AuthRepository(requireContext()); }
    protected String email() { return getArguments() == null ? "" : getArguments().getString(ARG_EMAIL, ""); }
    protected String value(EditText view) { return view.getText().toString().trim(); }

    protected boolean validEmail(EditText view) {
        if (Patterns.EMAIL_ADDRESS.matcher(value(view)).matches()) { view.setError(null); return true; }
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
        if (!password.getText().toString().matches("^(?=.*[0-9])(?=.*[a-z])(?=\\S+$).{8,}$")) {
            password.setError(getString(R.string.password_policy)); return false;
        }
        if (password.getText().toString().isEmpty()) { password.setError(getString(R.string.api_required)); return false; }
        if (!password.getText().toString().equals(confirm.getText().toString())) {
            confirm.setError(getString(R.string.api_password_mismatch)); return false;
        }
        return true;
    }

    protected <T> void submit(Call<T> call, View button, Consumer<T> success) {
        if (busy) { call.cancel(); return; }
        busy = true;
        button.setEnabled(false);
        request(call, result -> {
            busy = false;
            button.setEnabled(true);
            success.accept(result);
        }, error -> {
            busy = false;
            button.setEnabled(true);
            Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show();
        });
    }

    protected Bundle flow(String email, boolean reset) {
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        args.putBoolean(VerifyOtpFragment.ARG_RESET_PASSWORD, reset);
        return args;
    }

    @Override public void onDestroyView() { busy = false; super.onDestroyView(); }
}
