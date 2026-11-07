package com.example.myapplication.ui.profile;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.FragmentActivity;
import com.example.myapplication.R;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

final class ProfileUi {
    static final String UPDATED = "profile_updated";
    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    static String value(String value) { return value == null ? "" : value; }
    static void inputs(android.view.View root, boolean enabled) {
        if (root instanceof EditText || root instanceof android.widget.RadioButton || root instanceof android.widget.Spinner)
            root.setEnabled(enabled);
        if (root instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) inputs(group.getChildAt(i), enabled);
        }
    }
    static String value(EditText input) { return input.getText().toString().trim(); }
    static LocalDate date(String value) {
        if (value == null || value.isEmpty()) return null;
        try { return LocalDate.parse(value.split("T")[0]); }
        catch (RuntimeException ignored) {
            try { return LocalDate.parse(value, DISPLAY); }
            catch (RuntimeException invalid) { return null; }
        }
    }
    static String displayDate(String value) {
        LocalDate date = date(value);
        return date == null ? "" : date.format(DISPLAY);
    }
    static boolean required(EditText input) {
        if (!value(input).isEmpty()) { input.setError(null); return true; }
        input.setError(input.getContext().getString(R.string.api_required));
        input.requestFocus();
        return false;
    }
    static String birthDate(EditText input) {
        String date = ProfileViewModel.validBirthDate(value(input));
        if (date == null) {
            input.setError(input.getContext().getString(R.string.api_invalid_date));
            input.requestFocus();
            return null;
        }
        input.setError(null);
        return date;
    }
    static int genderIndex(String gender) { return "MALE".equals(gender) ? 0 : "FEMALE".equals(gender) ? 1 : 2; }
    static String gender(int index) { return index == 0 ? "MALE" : index == 1 ? "FEMALE" : "OTHER"; }
    static void updated(FragmentActivity activity) {
        activity.getSupportFragmentManager().setFragmentResult(UPDATED, new Bundle());
    }
    static void saved(FragmentActivity activity) {
        updated(activity);
        Toast.makeText(activity, R.string.profile_saved, Toast.LENGTH_SHORT).show();
    }
    private ProfileUi() { }
}
