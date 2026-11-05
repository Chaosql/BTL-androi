package com.example.myapplication;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;

public class StudyScreenFragment extends Fragment {
    private UiScreen screen;
    private boolean resetPassword;

    public static StudyScreenFragment create(UiScreen screen) {
        StudyScreenFragment fragment = new StudyScreenFragment();
        Bundle args = new Bundle();
        args.putString("screen", screen.name());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle state) {
        screen = UiScreen.valueOf(requireArguments().getString("screen", "LOGIN"));
        resetPassword = requireArguments().getBoolean("reset_password", false);
        return inflater.inflate(screen.layout, container, false);
    }

    @Override public void onViewCreated(@NonNull View root, @Nullable Bundle state) {
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
        spinner(root, R.id.spGender, "Nam", "Nữ", "Khác");
        spinner(root, R.id.spinnerGender, "Nam", "Nữ", "Khác");
        date(root, R.id.edtDate);
        date(root, R.id.etDateOfBirth);
        on(root, R.id.ivCalendar, () -> pickDate(root, R.id.edtDate));
        on(root, R.id.btnPickDate, () -> pickDate(root, R.id.etDateOfBirth));
        switch (screen) {
            case LOGIN:
                on(root, R.id.tvLogin, () -> go(UiScreen.MAP));
                on(root, R.id.btnGoogleLogin, () -> go(UiScreen.MAP));
                on(root, R.id.tvRegister, () -> go(UiScreen.REGISTER));
                on(root, R.id.tvForgot, () -> go(UiScreen.INPUT_EMAIL));
                break;
            case REGISTER:
                on(root, R.id.tvLogin, () -> go(UiScreen.LOGIN));
                on(root, R.id.tvRegister, () -> go(UiScreen.VERIFY_EMAIL));
                break;
            case VERIFY_EMAIL:
            case INPUT_EMAIL:
                on(root, R.id.btnSendEmail, () -> {
                    StudyScreenFragment otp = create(UiScreen.VERIFY_OTP);
                    otp.requireArguments().putBoolean("reset_password", screen == UiScreen.INPUT_EMAIL);
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.screenHost, otp).addToBackStack("OTP").commit();
                });
                break;
            case VERIFY_OTP:
                on(root, R.id.btnVerify, () -> {
                    if (resetPassword) go(UiScreen.FORGOT_PASSWORD);
                    else UiDialogs.show((MainActivity) requireActivity(), R.layout.dialog_login_success);
                });
                on(root, R.id.btnResend, this::previewNotice);
                break;
            case FORGOT_PASSWORD:
                toggle(root, R.id.ivTogglePassword, R.id.edtNewPassword);
                on(root, R.id.tvConfirm, () -> go(UiScreen.LOGIN));
                break;
            case MAP:
                on(root, R.id.btnProfile, () -> go(UiScreen.PROFILE));
                on(root, R.id.imgAvatar, () -> go(UiScreen.PROFILE));
                on(root, R.id.btnFriend, () -> go(UiScreen.FRIENDS));
                on(root, R.id.btnCalendar, () -> go(UiScreen.SCHEDULE));
                on(root, R.id.btnNotification, () -> go(UiScreen.NOTIFICATIONS));
                on(root, R.id.btnRadar, () -> dialog(R.layout.dialog_user_info));
                on(root, R.id.imgGroupRadar, () -> dialog(R.layout.dialog_user_info));
                on(root, R.id.mapView, () -> dialog(R.layout.dialog_user_info));
                on(root, R.id.imgSearch, () -> go(UiScreen.SEARCH_USER));
                break;
            case PROFILE:
            case EDIT_PROFILE:
                profile(root);
                break;
            case ACCOUNT_SETTINGS:
                on(root, R.id.cardPersonalInfo, () -> go(UiScreen.REGISTRATION_INFO));
                on(root, R.id.cardChangePassword, () -> go(UiScreen.CHANGE_PASSWORD));
                on(root, R.id.cardNotificationSettings, () -> go(UiScreen.NOTIFICATION_SETTINGS));
                on(root, R.id.cardBlockedUsers, () -> go(UiScreen.BLOCKED_USERS));
                on(root, R.id.cardLogout, () -> go(UiScreen.LOGIN));
                on(root, R.id.cardHelp, this::previewNotice);
                break;
            case REGISTRATION_INFO:
                text(root, R.id.etEmail, "minhan@example.com");
                text(root, R.id.etId, "STUDY-001");
                text(root, R.id.etLastName, "Nguyễn");
                text(root, R.id.etFirstName, "Minh An");
                text(root, R.id.etDateOfBirth, "15/08/2005");
                on(root, R.id.btnSave, this::previewNotice);
                break;
            case CHANGE_PASSWORD:
                on(root, R.id.tvForgotPassword, () -> go(UiScreen.INPUT_EMAIL));
                on(root, R.id.btnOk, this::previewNotice);
                break;
            case FRIENDS:
            case GROUP_DETAIL:
                UiRows.Adapter friends = UiRows.friends(this, screen == UiScreen.GROUP_DETAIL);
                list(root, R.id.rvFriendsList, friends);
                filter(root, R.id.etSearchFriends, friends);
                on(root, R.id.btnHeaderMenu, () -> dialog(R.layout.layout_friends_menu_popup));
                if (screen == UiScreen.GROUP_DETAIL) text(root, R.id.tvHeaderTitle, "Nhóm học Android");
                break;
            case SEARCH_USER:
                UiRows.Adapter results = UiRows.users(this, false);
                list(root, R.id.rvSearchResults, results);
                on(root, R.id.btnSearch, () -> results.filter(value(root, R.id.edtKeyword)));
                filter(root, R.id.edtKeyword, results);
                break;
            case PENDING_REQUESTS:
                hide(root, R.id.tvEmptyState);
                list(root, R.id.rvPendingRequests, UiRows.users(this, true));
                break;
            case BLOCKED_USERS:
                hide(root, R.id.tvEmptyState);
                list(root, R.id.rvBlockedUsers, UiRows.blocked(this));
                break;
            case CHAT:
                chat(root);
                break;
            case NOTIFICATIONS:
                hide(root, R.id.layoutEmptyState);
                root.findViewById(R.id.scrollViewContent).setVisibility(View.VISIBLE);
                list(root, R.id.rvRecentNotifications, UiRows.notifications(this, true));
                list(root, R.id.rvEarlierNotifications, UiRows.notifications(this, false));
                on(root, R.id.tvMarkAllRead, this::previewNotice);
                break;
            case SCHEDULE:
                text(root, R.id.tvMonthYear, LocalDate.now().format(DateTimeFormatter.ofPattern("MM / yyyy")));
                text(root, R.id.tvDateTag, LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM")));
                list(root, R.id.rvScheduleList, UiRows.schedule(this));
                for (int id : new int[]{R.id.tabMon, R.id.tabTue, R.id.tabWed, R.id.tabThu, R.id.tabFri, R.id.tabSat, R.id.tabSun}) {
                    on(root, id, () -> {
                        for (int tab : new int[]{R.id.tabMon, R.id.tabTue, R.id.tabWed, R.id.tabThu, R.id.tabFri, R.id.tabSat, R.id.tabSun})
                            root.findViewById(tab).setAlpha(tab == id ? 1f : 0.5f);
                    });
                }
                on(root, R.id.btnWeekPrev, this::previewNotice);
                on(root, R.id.btnWeekNext, this::previewNotice);
                on(root, R.id.imgMascot, () -> go(UiScreen.CREATE_APPOINTMENT));
                break;
            case CREATE_APPOINTMENT:
                NumberPicker hours = root.findViewById(R.id.npHour);
                hours.setMinValue(0); hours.setMaxValue(23); hours.setValue(14);
                NumberPicker minutes = root.findViewById(R.id.npMinute);
                minutes.setMinValue(0); minutes.setMaxValue(59); minutes.setValue(30);
                spinner(root, R.id.spDuration, "30 phút", "60 phút", "90 phút", "120 phút");
                text(root, R.id.tvSelectedDate, LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                on(root, R.id.layoutDateField, () -> pickDate(root, R.id.tvSelectedDate));
                on(root, R.id.btnCreate, () -> dialog(R.layout.dialog_booking_detail));
                break;
            default:
                break;
        }
    }

    private void profile(View root) {
        text(root, R.id.tvUsername, "Minh An");
        text(root, R.id.tvBuddyStatus, "Trạng thái quét: Bật");
        text(root, R.id.tvIntroduction, "Cùng nhau học tốt hơn mỗi ngày.");
        text(root, R.id.tvAge, "Tuổi: 21");
        text(root, R.id.tvGender, "Giới tính: Nam");
        text(root, R.id.tvSchool, "Trường học: Đại học Công nghiệp Hà Nội");
        text(root, R.id.tvMajor, "Ngành học: Công nghệ thông tin");
        text(root, R.id.tvLocation, "Địa điểm: Hà Nội");
        text(root, R.id.tvFriendsCount, "Điểm tin cậy: 100.0");
        text(root, R.id.tvFavoriteRating, "100.0");
        text(root, R.id.tvAppointmentCount, "8");
        text(root, R.id.tvSubjectCount, "3");
        on(root, R.id.btnUpdateProfile, () -> {
            if (screen == UiScreen.PROFILE) go(UiScreen.EDIT_PROFILE);
            else previewNotice();
        });
        on(root, R.id.cvSettings, () -> go(UiScreen.ACCOUNT_SETTINGS));
        on(root, R.id.cvAppointment, () -> go(UiScreen.SCHEDULE));
        on(root, R.id.cvHonor, () -> dialog(R.layout.dialog_subject_management));
        on(root, R.id.tvAddInfo, () -> dialog(R.layout.dialog_edit_personal_info));
        on(root, R.id.tvIntroduction, () -> dialog(R.layout.dialog_edit_introduction));
        on(root, R.id.imgEditAvatar, this::previewNotice);
        on(root, R.id.cvInfo, () -> dialog(R.layout.dialog_edit_personal_info));
    }

    private void chat(View root) {
        text(root, R.id.tvChatTitle, "Minh Anh");
        text(root, R.id.tvOnlineStatus, "Đang hoạt động");
        UiRows.Adapter messages = UiRows.messages(this);
        list(root, R.id.rvChatMessages, messages);
        on(root, R.id.btnSend, () -> {
            String message = value(root, R.id.etMessage).trim();
            if (message.isEmpty()) return;
            messages.addMessage(message);
            text(root, R.id.etMessage, "");
            ((RecyclerView) root.findViewById(R.id.rvChatMessages)).scrollToPosition(messages.getItemCount() - 1);
        });
        on(root, R.id.btnCreateSchedule, () -> go(UiScreen.CREATE_APPOINTMENT));
        on(root, R.id.tvChatTitle, () -> go(UiScreen.GROUP_DETAIL));
        on(root, R.id.btnSticker, () -> dialog(R.layout.layout_reaction_popup));
        on(root, R.id.btnGallery, this::previewNotice);
        on(root, R.id.btnFolder, this::previewNotice);
        on(root, R.id.btnMic, this::previewNotice);
        on(root, R.id.btnCancelReply, () -> hide(root, R.id.layoutReplyPreview));
    }

    public void go(UiScreen next) { ((MainActivity) requireActivity()).showScreen(next); }
    public void dialog(int layout) { UiDialogs.show((MainActivity) requireActivity(), layout); }
    public void previewNotice() {
        Toast.makeText(requireContext(), "Bản giao diện minh họa, chưa kết nối dữ liệu.", Toast.LENGTH_SHORT).show();
    }
    private void back() { requireActivity().getOnBackPressedDispatcher().onBackPressed(); }

    static void text(View root, int id, String text) {
        View view = root.findViewById(id);
        if (view instanceof TextView) ((TextView) view).setText(text);
    }
    static String value(View root, int id) {
        View view = root.findViewById(id);
        return view instanceof TextView ? ((TextView) view).getText().toString() : "";
    }
    static void on(View root, int id, Runnable action) {
        View view = root.findViewById(id);
        if (view != null) view.setOnClickListener(v -> action.run());
    }
    static void hide(View root, int id) {
        View view = root.findViewById(id);
        if (view != null) view.setVisibility(View.GONE);
    }
    static void spinner(View root, int id, String... values) {
        View view = root.findViewById(id);
        if (!(view instanceof Spinner)) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(root.getContext(), R.layout.item_spinner_selected, Arrays.asList(values));
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        ((Spinner) view).setAdapter(adapter);
    }
    static void list(View root, int id, UiRows.Adapter adapter) {
        RecyclerView list = root.findViewById(id);
        if (list == null) return;
        list.setLayoutManager(new LinearLayoutManager(root.getContext()));
        list.setAdapter(adapter);
        list.setNestedScrollingEnabled(false);
    }
    private void filter(View root, int id, UiRows.Adapter adapter) {
        EditText input = root.findViewById(id);
        if (input == null) return;
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { adapter.filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) { }
        });
    }
    private void date(View root, int id) { on(root, id, () -> pickDate(root, id)); }
    private void pickDate(View root, int id) {
        LocalDate now = LocalDate.now();
        new DatePickerDialog(requireContext(), (view, year, month, day) -> text(root, id,
                String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year)),
                now.getYear(), now.getMonthValue() - 1, now.getDayOfMonth()).show();
    }
    private void toggle(View root, int toggleId, int inputId) {
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
