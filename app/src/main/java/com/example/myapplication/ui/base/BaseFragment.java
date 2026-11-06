package com.example.myapplication.ui.base;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
    private final java.util.List<retrofit2.Call<?>> requests = new java.util.ArrayList<>();

    protected BaseFragment(BindingInflater<B> bindingInflater) { this.bindingInflater = bindingInflater; }

    @Override public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup parent, Bundle state) {
        binding = bindingInflater.inflate(inflater, parent, false);
        return binding.getRoot();
    }

    protected <T> void request(retrofit2.Call<T> call, java.util.function.Consumer<T> success) {
        request(call, success, error -> Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show());
    }

    protected void cancelRequest(retrofit2.Call<?> call) {
        call.cancel();
        requests.remove(call);
    }

    protected <T> void request(retrofit2.Call<T> call, java.util.function.Consumer<T> success,
            java.util.function.Consumer<com.example.myapplication.data.remote.network.ApiError> failure) {
        requests.add(call);
        com.example.myapplication.data.remote.network.ApiRequests.enqueue(call,
                new com.example.myapplication.data.remote.network.ApiCallback<T>() {
                    @Override public void onSuccess(T result) {
                        requests.remove(call);
                        if (binding != null && isAdded()) success.accept(result);
                    }
                    @Override public void onError(com.example.myapplication.data.remote.network.ApiError error) {
                        requests.remove(call);
                        if (binding != null && isAdded()) failure.accept(error);
                    }
                });
    }

    @Override public void onDestroyView() {
        for (retrofit2.Call<?> call : requests) call.cancel();
        requests.clear();
        binding = null;
        super.onDestroyView();
    }

    @Override public final void onViewCreated(@NonNull View root, @Nullable Bundle state) {
        super.onViewCreated(root, state);
        for (int id : new int[]{R.id.header, R.id.ivPic, R.id.tvHeaderTitle, R.id.tvScreenTitle, R.id.tvTitle, R.id.imgCrown}) {
            View heading = root.findViewById(id);
            if (heading != null) heading.setOnLongClickListener(v -> {
                ((MainActivity) requireActivity()).showScreens();
                return true;
            });
        }
        on(root, R.id.btnBack, this::back);
        on(root, R.id.ivBack, this::back);
        hide(root, R.id.progressBar);
        toggle(root, R.id.ivTogglePassword, R.id.edtPassword);
        toggle(root, R.id.ivToggleConfirmPassword, R.id.edtConfirmPassword);
        genderSpinner(root, R.id.spGender);
        genderSpinner(root, R.id.spinnerGender);
        date(root, R.id.edtDate);
        date(root, R.id.etDateOfBirth);
        on(root, R.id.ivCalendar, () -> pickDate(root, R.id.edtDate));
        on(root, R.id.btnPickDate, () -> pickDate(root, R.id.etDateOfBirth));
        onScreenCreated(root, state);
    }

    protected abstract void onScreenCreated(View root, Bundle state);

    protected void genderSpinner(View root, int id) {
        View view = root.findViewById(id);
        if (!(view instanceof Spinner)) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(root.getContext(), R.layout.item_spinner_gender,
                Arrays.asList("Nam", getString(R.string.ui_dialog_edit_personal_info_text_5), getString(R.string.ui_dialog_edit_personal_info_text_6)));
        adapter.setDropDownViewResource(R.layout.item_spinner_gender_dropdown);
        ((Spinner) view).setAdapter(adapter);
    }

    public void go(UiScreen next) { ((MainActivity) requireActivity()).showScreen(next); }
    public void go(UiScreen next, Bundle arguments) {
        ((MainActivity) requireActivity()).showScreen(next, arguments);
    }

    public void dialog(int layout) { UiDialogs.show((MainActivity) requireActivity(), layout); }
    public void previewNotice() {
        Toast.makeText(requireContext(), getString(R.string.ui_studyscreenfragment_text_22), Toast.LENGTH_SHORT).show();
    }
    private void back() { requireActivity().getOnBackPressedDispatcher().onBackPressed(); }

    public static void text(View root, int id, String text) {
        View view = root.findViewById(id);
        if (view instanceof TextView) ((TextView) view).setText(text);
    }
    public static String value(View root, int id) {
        View view = root.findViewById(id);
        return view instanceof TextView ? ((TextView) view).getText().toString() : "";
    }
    public static void on(View root, int id, Runnable action) {
        View view = root.findViewById(id);
        if (view != null) view.setOnClickListener(v -> action.run());
    }
    public static void hide(View root, int id) {
        View view = root.findViewById(id);
        if (view != null) view.setVisibility(View.GONE);
    }
    public static void spinner(View root, int id, String... values) {
        View view = root.findViewById(id);
        if (!(view instanceof Spinner)) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(root.getContext(), R.layout.item_spinner_selected, Arrays.asList(values));
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        ((Spinner) view).setAdapter(adapter);
    }
    public static void list(View root, int id, BasePreviewAdapter adapter) {
        RecyclerView list = root.findViewById(id);
        if (list == null) return;
        LinearLayoutManager manager = new LinearLayoutManager(root.getContext());
        if (id == R.id.rvChatMessages) manager.setStackFromEnd(true);
        list.setLayoutManager(manager);
        list.setAdapter(adapter);
        list.setNestedScrollingEnabled(id != R.id.rvRecentNotifications && id != R.id.rvEarlierNotifications);
    }
    protected void filter(View root, int id, BasePreviewAdapter adapter) {
        EditText input = root.findViewById(id);
        if (input == null) return;
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { adapter.filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) { }
        });
    }
    private void date(View root, int id) { on(root, id, () -> pickDate(root, id)); }
    protected void pickDate(View root, int id) {
        LocalDate now = LocalDate.now();
        new DatePickerDialog(requireContext(), (view, year, month, day) -> text(root, id,
                String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year)),
                now.getYear(), now.getMonthValue() - 1, now.getDayOfMonth()).show();
    }
    protected void toggle(View root, int toggleId, int inputId) {
        View view = root.findViewById(inputId);
        if (!(view instanceof EditText)) return;
        EditText input = (EditText) view;
        on(root, toggleId, () -> {
            boolean hidden = input.getTransformationMethod() instanceof PasswordTransformationMethod;
            input.setTransformationMethod(hidden ? null : PasswordTransformationMethod.getInstance());
            input.setSelection(input.length());
        });
    }
}
