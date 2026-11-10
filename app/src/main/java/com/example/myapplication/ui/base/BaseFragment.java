package com.example.myapplication.ui.base;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.myapplication.R;
import com.example.myapplication.ui.main.MainActivity;
import com.example.myapplication.ui.main.UiDialogs;
import com.example.myapplication.ui.main.UiScreen;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;

public abstract class BaseFragment<B extends androidx.viewbinding.ViewBinding> extends Fragment {
    protected B binding;
    private final BindingInflater<B> bindingInflater;

    protected BaseFragment(BindingInflater<B> bindingInflater) { this.bindingInflater = bindingInflater; }

    @Override public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup parent, Bundle state) {
        binding = bindingInflater.inflate(inflater, parent, false);
        return binding.getRoot();
    }

    protected final <V extends androidx.lifecycle.ViewModel> V viewModel(Class<V> type) {
        return new androidx.lifecycle.ViewModelProvider(this, new androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication())).get(type);
    }

    protected <T> void request(UiOperation<T> operation, java.util.function.Consumer<T> success) {
        request(operation, success, error -> Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show());
    }
    protected void cancelRequest(UiOperation<?> operation) { operation.cancel(); }
    protected <T> void request(UiOperation<T> operation, java.util.function.Consumer<T> success,
            java.util.function.Consumer<com.example.myapplication.data.remote.network.ApiError> failure) {
        operation.observe(getViewLifecycleOwner(), state -> {
            if (binding == null || !isAdded() || !state.isFinished()) return;
            operation.removeObservers(getViewLifecycleOwner());
            if (state.status == UiState.Status.SUCCESS) success.accept(state.data);
            else if (state.status == UiState.Status.ERROR) failure.accept(state.error);
        });
    }

    @Override public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    @Override public final void onViewCreated(@NonNull View root, @Nullable Bundle state) {
        super.onViewCreated(root, state);
        onScreenCreated(root, state);
    }

    protected abstract void onScreenCreated(View root, Bundle state);

    protected void enableScreenCatalog(View... headings) {
        for (View heading : headings) heading.setOnLongClickListener(view -> {
            ((MainActivity) requireActivity()).showScreens();
            return true;
        });
    }

    protected void genderSpinner(Spinner view) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), R.layout.item_spinner_gender,
                Arrays.asList("Nam", getString(R.string.ui_dialog_edit_personal_info_text_5), getString(R.string.ui_dialog_edit_personal_info_text_6)));
        adapter.setDropDownViewResource(R.layout.item_spinner_gender_dropdown);
        view.setAdapter(adapter);
    }

    public void go(UiScreen next) { ((MainActivity) requireActivity()).showScreen(next); }
    public void go(UiScreen next, Bundle arguments) {
        ((MainActivity) requireActivity()).showScreen(next, arguments);
    }

    public void dialog(int layout) { UiDialogs.show((MainActivity) requireActivity(), layout); }
    public void previewNotice() {
        Toast.makeText(requireContext(), getString(R.string.ui_studyscreenfragment_text_22), Toast.LENGTH_SHORT).show();
    }
    protected void back() { requireActivity().getOnBackPressedDispatcher().onBackPressed(); }

    protected static void text(TextView view, String text) {
        if (view != null) view.setText(text);
    }
    protected static void on(View view, Runnable action) {
        if (view != null) view.setOnClickListener(ignored -> action.run());
    }
    protected static void spinner(Spinner view, String... values) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), R.layout.item_spinner_selected, Arrays.asList(values));
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        view.setAdapter(adapter);
    }
    protected void pickDate(EditText input) {
        LocalDate now = LocalDate.now();
        new DatePickerDialog(requireContext(), (view, year, month, day) -> input.setText(
                String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year)),
                now.getYear(), now.getMonthValue() - 1, now.getDayOfMonth()).show();
    }
    protected void toggle(android.widget.ImageView toggle, EditText input) {
        toggle(toggle, input, () -> { });
    }
    protected void toggle(android.widget.ImageView toggle, EditText input, Runnable visibilityChanged) {
        updatePasswordToggle(toggle, input);
        visibilityChanged.run();
        toggle.setOnClickListener(view -> {
            boolean hidden = input.getTransformationMethod() instanceof PasswordTransformationMethod;
            input.setTransformationMethod(hidden ? null : PasswordTransformationMethod.getInstance());
            input.setSelection(input.length());
            updatePasswordToggle(toggle, input);
            visibilityChanged.run();
        });
    }
    private void updatePasswordToggle(android.widget.ImageView toggle, EditText input) {
        boolean visible = !(input.getTransformationMethod() instanceof PasswordTransformationMethod);
        toggle.setSelected(visible);
        toggle.setImageResource(visible ? R.drawable.ic_visibility : R.drawable.ic_visibility_off);
        toggle.setContentDescription(getString(visible ? R.string.password_hide : R.string.password_show));
    }
}
