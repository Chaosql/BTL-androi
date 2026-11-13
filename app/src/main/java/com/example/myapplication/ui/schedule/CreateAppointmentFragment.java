package com.example.myapplication.ui.schedule;

import com.example.myapplication.ui.base.UiOperation;
import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.R;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.request.CreateBookingRequest;
import com.example.myapplication.data.remote.dto.request.DirectChatRequest;
import com.example.myapplication.data.remote.dto.response.FriendDto;
import com.example.myapplication.ui.chat.ConversationViewModel;
import com.example.myapplication.ui.schedule.BookingViewModel;
import com.example.myapplication.ui.friends.FriendViewModel;
import com.example.myapplication.databinding.FragmentCreateAppointmentBinding;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.friends.SocialUi;
import com.example.myapplication.ui.main.MainActivity;
import com.example.myapplication.ui.main.UiScreen;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CreateAppointmentFragment extends BaseFragment<FragmentCreateAppointmentBinding> {
    public static final String CONVERSATION_ID = "conversation_id";
    public static final String TARGET_NAME = "target_user_name";
    private static final int[] DURATIONS = {30, 45, 60, 90, 120, 180, 240};
    private String conversationId;
    private String targetName;
    private boolean busy;
    private AlertDialog chooser;
    private UiOperation<?> partnerCall;
    private LocalDate selectedDate;

    public CreateAppointmentFragment() { super(FragmentCreateAppointmentBinding::inflate); }

    private void setupTimePicker(NumberPicker picker, int max, int value) {
        picker.setMinValue(0); picker.setMaxValue(max);
        picker.setFormatter(number -> String.format(Locale.US, "%02d", number));
        picker.setValue(value);
        int color = requireContext().getColor(R.color.brown);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) picker.setTextColor(color);
        picker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        for (int i = 0; i < picker.getChildCount(); i++)
            if (picker.getChildAt(i) instanceof EditText) ((EditText) picker.getChildAt(i)).setTextColor(color);
    }

    @Override protected void onScreenCreated(View root, Bundle state) {
        binding.ivBack.setOnClickListener(view -> back());
        Bundle data = state != null ? state : getArguments();
        conversationId = data == null ? "" : data.getString(CONVERSATION_ID, "");
        targetName = data == null ? "" : data.getString(TARGET_NAME, "");
        busy = false;
        title();
        LocalDateTime initial = LocalDateTime.now(BookingUi.ZONE).plusMinutes(31);
        selectedDate = data != null && data.getString(BookingUi.SELECTED_DATE) != null
                ? LocalDate.parse(data.getString(BookingUi.SELECTED_DATE)) : initial.toLocalDate();
        setupTimePicker(binding.npHour, 23, state == null ? initial.getHour() : state.getInt("hour", initial.getHour()));
        setupTimePicker(binding.npMinute, 59, state == null ? initial.getMinute() : state.getInt("minute", initial.getMinute()));
        spinner(binding.spDuration, getString(R.string.ui_studyscreenfragment_text_4), getString(R.string.ui_studyscreenfragment_text_5),
                getString(R.string.ui_studyscreenfragment_text_6), getString(R.string.ui_studyscreenfragment_text_7),
                getString(R.string.ui_studyscreenfragment_text_8), getString(R.string.ui_studyscreenfragment_text_9), getString(R.string.ui_studyscreenfragment_text_10));
        if (state != null) {
            binding.spDuration.setSelection(state.getInt("duration", 0));
            binding.cbIsRecurring.setChecked(state.getBoolean("recurring", false));
            binding.etNote.setText(state.getString("note", ""));
        }
        dateText();
        binding.layoutDateField.setOnClickListener(view -> {
            DatePickerDialog picker = new DatePickerDialog(requireContext(), (calendar, year, month, day) -> {
                selectedDate = LocalDate.of(year, month + 1, day); dateText();
            }, selectedDate.getYear(), selectedDate.getMonthValue() - 1, selectedDate.getDayOfMonth());
            picker.show();
        });
        binding.btnCreate.setOnClickListener(view -> {
            if (busy || !validate()) return;
            if (!new PreferenceManager(requireContext()).isLogin()) { error(getString(R.string.api_invalid_login)); return; }
            setBusy(true);
            if (conversationId.isEmpty()) choosePartner();
            else create();
        });
    }

    private void title() {
        binding.tvScreenTitle.setText(targetName == null || targetName.isEmpty() ? getString(R.string.title_create_appointment) : getString(R.string.booking_with, targetName));
    }
    private void dateText() { binding.tvSelectedDate.setText(selectedDate.format(BookingUi.DATE)); }
    private boolean validate() {
        binding.tvErrorMessage.setVisibility(View.GONE);
        if (binding.etLocationName.getText().toString().trim().isEmpty()) {
            binding.etLocationName.setError(getString(R.string.booking_invalid_location));
            binding.etLocationName.requestFocus(); return false;
        }
        LocalDateTime selected = LocalDateTime.of(selectedDate, LocalTime.of(binding.npHour.getValue(), binding.npMinute.getValue()));
        if (!BookingViewModel.validTime(selected)) {
            error(getString(R.string.booking_invalid_time)); return false;
        }
        return true;
    }
    private void setBusy(boolean value) {
        busy = value;
        binding.btnCreate.setEnabled(!value);
        binding.layoutDateField.setEnabled(!value);
        binding.npHour.setEnabled(!value); binding.npMinute.setEnabled(!value);
        binding.spDuration.setEnabled(!value); binding.cbIsRecurring.setEnabled(!value);
        binding.etLocationName.setEnabled(!value); binding.etStreet.setEnabled(!value);
        binding.etDistrict.setEnabled(!value); binding.etProvince.setEnabled(!value);
        binding.etNote.setEnabled(!value);
    }
    private void error(String message) {
        binding.tvErrorMessage.setText(message); binding.tvErrorMessage.setVisibility(View.VISIBLE);
    }

    private void choosePartner() {
        List<FriendDto> partners = new ArrayList<>();
        ArrayAdapter<String> names = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, new ArrayList<>());
        boolean[] selected = {false};
        int[] page = {0};
        chooser = new MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.booking_choose_partner)
                .setAdapter(names, (dialog, position) -> {
                    selected[0] = true;
                    FriendDto friend = partners.get(position);
                    targetName = SocialUi.name(friend.firstName, friend.lastName);
                    title();
                    request(viewModel(com.example.myapplication.ui.chat.ConversationViewModel.class).createOrGetDirectConversation(new DirectChatRequest(friend.userId)),
                            response -> {
                                if (response.data == null || response.data.id == null || response.data.id.isEmpty()) {
                                    setBusy(false); error(getString(R.string.booking_invalid_response)); return;
                                }
                                conversationId = response.data.id;
                                if (!validate()) { setBusy(false); return; }
                                create();
                            }, failure -> { setBusy(false); error(failure.message); });
                }).setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.booking_more_partners, null).create();
        chooser.setOnDismissListener(dialog -> {
            if (partnerCall != null) { cancelRequest(partnerCall); partnerCall = null; }
            if (!selected[0] && binding != null) setBusy(false);
        });
        chooser.setOnShowListener(dialog -> {
            chooser.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(view -> loadPartners(partners, names, page));
            loadPartners(partners, names, page);
        });
        chooser.show();
    }

    private void loadPartners(List<FriendDto> partners, ArrayAdapter<String> names, int[] page) {
        if (chooser == null || !chooser.isShowing() || partnerCall != null) return;
        chooser.setTitle(R.string.booking_loading);
        chooser.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(false);
        UiOperation<com.example.myapplication.data.remote.dto.response.BaseResponse<com.example.myapplication.data.remote.dto.response.PageResponse<FriendDto>>> call =
                viewModel(com.example.myapplication.ui.friends.FriendViewModel.class).getFriends(page[0], 20, null);
        partnerCall = call;
        request(call, response -> {
            partnerCall = null;
            if (chooser == null || !chooser.isShowing()) return;
            chooser.setTitle(R.string.booking_choose_partner);
            if (response.data == null || response.data.content == null) {
                chooser.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(true);
                Toast.makeText(requireContext(), R.string.booking_invalid_response, Toast.LENGTH_LONG).show();
                error(getString(R.string.booking_invalid_response)); return;
            }
            for (FriendDto item : response.data.content) {
                if (item == null || item.userId == null || item.userId.isEmpty()) continue;
                boolean exists = partners.stream().anyMatch(old -> item.userId.equals(old.userId));
                if (!exists) { partners.add(item); names.add(SocialUi.name(item.firstName, item.lastName)); }
            }
            page[0]++;
            boolean last = response.data.totalPages != null && response.data.totalPages > 0
                    ? page[0] >= response.data.totalPages : Boolean.TRUE.equals(response.data.last);
            chooser.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(!last);
            if (partners.isEmpty() && last) { chooser.dismiss(); error(getString(R.string.booking_no_friends)); }
        }, failure -> {
            partnerCall = null;
            if (chooser != null && chooser.isShowing()) chooser.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(true);
            Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show();
            error(failure.message);
        });
    }

    private void create() {
        LocalDateTime time = LocalDateTime.of(selectedDate, LocalTime.of(binding.npHour.getValue(), binding.npMinute.getValue()));
        int duration = DURATIONS[binding.spDuration.getSelectedItemPosition()];
        CreateBookingRequest change = new CreateBookingRequest(binding.tvScreenTitle.getText().toString(),
                time.atZone(BookingUi.ZONE).toInstant().toString(), duration,
                binding.etLocationName.getText().toString().trim(), binding.etStreet.getText().toString().trim(),
                binding.etDistrict.getText().toString().trim(), binding.etProvince.getText().toString().trim(),
                binding.etNote.getText().toString().trim(), binding.cbIsRecurring.isChecked(), 15);
        request(viewModel(com.example.myapplication.ui.schedule.BookingViewModel.class).createBookingInConversation(conversationId, change), response -> {
            MainActivity activity = (MainActivity) requireActivity();
            Bundle result = new Bundle();
            result.putString(BookingUi.SELECTED_DATE, selectedDate.toString());
            if (response.data != null) result.putString(BookingUi.BOOKING_ID, response.data.id);
            Toast.makeText(activity, R.string.booking_created, Toast.LENGTH_SHORT).show();
            androidx.fragment.app.FragmentManager manager = activity.getSupportFragmentManager();
            manager.popBackStackImmediate();
            if (manager.findFragmentById(R.id.screenHost) instanceof ScheduleFragment)
                manager.setFragmentResult(BookingUi.UPDATED, result);
            else activity.showScreen(UiScreen.SCHEDULE, result);
        }, failure -> { setBusy(false); error(failure.message); });
    }

    @Override public void onSaveInstanceState(@NonNull Bundle state) {
        if (binding != null) {
            state.putString(CONVERSATION_ID, conversationId); state.putString(TARGET_NAME, targetName);
            state.putString(BookingUi.SELECTED_DATE, selectedDate.toString());
            state.putInt("hour", binding.npHour.getValue()); state.putInt("minute", binding.npMinute.getValue());
            state.putInt("duration", binding.spDuration.getSelectedItemPosition());
            state.putBoolean("recurring", binding.cbIsRecurring.isChecked());
            state.putString("note", binding.etNote.getText().toString());
        }
        super.onSaveInstanceState(state);
    }
    @Override public void onDestroyView() {
        if (chooser != null) { chooser.dismiss(); chooser = null; }
        super.onDestroyView();
    }
}
