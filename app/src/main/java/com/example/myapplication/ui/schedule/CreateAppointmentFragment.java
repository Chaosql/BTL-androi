package com.example.myapplication.ui.schedule;

import com.example.myapplication.databinding.FragmentCreateAppointmentBinding;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CreateAppointmentFragment extends BaseFragment<FragmentCreateAppointmentBinding> {
    private void setupTimePicker(NumberPicker picker, int max, int value) {
        picker.setMinValue(0);
        picker.setMaxValue(max);
        picker.setFormatter(number -> String.format(Locale.getDefault(), "%02d", number));
        picker.setValue(value);
        int color = requireContext().getColor(R.color.brown);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) picker.setTextColor(color);
        picker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        for (int i = 0; i < picker.getChildCount(); i++) {
            if (picker.getChildAt(i) instanceof EditText) ((EditText) picker.getChildAt(i)).setTextColor(color);
        }
    }

    public CreateAppointmentFragment() { super(FragmentCreateAppointmentBinding::inflate); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        binding.tvScreenTitle.setText(getString(R.string.title_create_appointment));
        NumberPicker hours = binding.npHour;
        NumberPicker minutes = binding.npMinute;
        LocalTime startTime = LocalTime.now().plusMinutes(30);
        setupTimePicker(hours, 23, startTime.getHour());
        setupTimePicker(minutes, 59, startTime.getMinute());
        spinner(root, R.id.spDuration, getString(R.string.ui_studyscreenfragment_text_4), getString(R.string.ui_studyscreenfragment_text_5), getString(R.string.ui_studyscreenfragment_text_6),
                getString(R.string.ui_studyscreenfragment_text_7), getString(R.string.ui_studyscreenfragment_text_8), getString(R.string.ui_studyscreenfragment_text_9), getString(R.string.ui_studyscreenfragment_text_10));
        binding.tvSelectedDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        binding.layoutDateField.setOnClickListener(bindingView -> pickDate(root, R.id.tvSelectedDate));
        binding.btnCreate.setOnClickListener(bindingView -> dialog(R.layout.dialog_booking_detail));
    }
}
