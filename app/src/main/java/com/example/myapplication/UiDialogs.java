package com.example.myapplication;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import java.time.LocalDate;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public final class UiDialogs {
    public static final int[] LAYOUTS = {
            R.layout.dialog_add_subject, R.layout.dialog_appointment_reminder,
            R.layout.dialog_booking_detail, R.layout.dialog_cancel_reason,
            R.layout.dialog_confirm_delete, R.layout.dialog_edit_introduction,
            R.layout.dialog_edit_personal_info, R.layout.dialog_forward_select,
            R.layout.dialog_friend_options, R.layout.dialog_login_success,
            R.layout.dialog_subject_management, R.layout.dialog_user_info,
            R.layout.layout_friends_menu_popup, R.layout.layout_reaction_popup
    };
    private static final String[] TITLES = {
            "Thêm môn học", "Nhắc lịch hẹn", "Chi tiết lịch hẹn", "Lý do hủy lịch",
            "Xác nhận", "Sửa giới thiệu", "Sửa thông tin cá nhân", "Chuyển tiếp tin nhắn",
            "Tùy chọn bạn bè", "Đăng nhập thành công", "Quản lý môn học", "Thông tin người dùng",
            "Menu bạn bè", "Cảm xúc và thao tác tin nhắn"
    };

    public static void showCatalog(MainActivity activity) {
        new MaterialAlertDialogBuilder(activity).setTitle("Hộp thoại và menu")
                .setItems(TITLES, (dialog, which) -> show(activity, LAYOUTS[which]))
                .setNegativeButton("Đóng", null).show();
    }

    public static void show(MainActivity activity, int layout) {
        View content = LayoutInflater.from(activity).inflate(layout, null, false);
        AlertDialog dialog = new MaterialAlertDialogBuilder(activity).setView(content).create();
        for (int id : new int[]{R.id.btnCancel, R.id.btnCancelAdd, R.id.ivClose, R.id.btnClose})
            StudyScreenFragment.on(content, id, dialog::dismiss);
        for (int id : new int[]{R.id.btnSave, R.id.btnConfirm, R.id.btnConfirmAdd, R.id.btnConfirmCancel,
                R.id.btnRate, R.id.btnAccept, R.id.btnReject, R.id.btnComplete, R.id.btnAddFriend,
                R.id.tvOptionPin, R.id.tvOptionBlock, R.id.tvOptionDelete, R.id.tvRecall, R.id.tvDelete}) {
            StudyScreenFragment.on(content, id, () -> {
                Toast.makeText(activity, "Thao tác minh họa giao diện, chưa lưu dữ liệu.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }
        StudyScreenFragment.on(content, R.id.btnGetStarted, () -> { dialog.dismiss(); activity.showScreen(UiScreen.MAP); });
        StudyScreenFragment.on(content, R.id.btnViewSchedule, () -> { dialog.dismiss(); activity.showScreen(UiScreen.SCHEDULE); });
        StudyScreenFragment.on(content, R.id.btnViewProfile, () -> { dialog.dismiss(); activity.showScreen(UiScreen.PROFILE); });
        StudyScreenFragment.on(content, R.id.tvOptionViewProfile, () -> { dialog.dismiss(); activity.showScreen(UiScreen.PROFILE); });
        StudyScreenFragment.on(content, R.id.tvOptionCreateGroup, () -> { dialog.dismiss(); activity.showScreen(UiScreen.GROUP_DETAIL); });
        StudyScreenFragment.on(content, R.id.btnMenuNewChat, () -> { dialog.dismiss(); activity.showScreen(UiScreen.SEARCH_USER); });
        StudyScreenFragment.on(content, R.id.btnMenuPendingRequests, () -> { dialog.dismiss(); activity.showScreen(UiScreen.PENDING_REQUESTS); });
        StudyScreenFragment.on(content, R.id.btnAddSubject, () -> { dialog.dismiss(); show(activity, R.layout.dialog_add_subject); });
        StudyScreenFragment.on(content, R.id.btnCancel, dialog::dismiss);
        if (layout == R.layout.dialog_booking_detail) {
            StudyScreenFragment.text(content, R.id.tvTitle, "Học Java cùng Minh Anh");
            StudyScreenFragment.text(content, R.id.tvSubject, "Lập trình Java");
            StudyScreenFragment.text(content, R.id.tvPartnerName, "Minh Anh");
            StudyScreenFragment.text(content, R.id.tvTimeAndDate, "14:30 – 15:30 · " + LocalDate.now());
            StudyScreenFragment.text(content, R.id.tvStatus, "Đã xác nhận");
            StudyScreenFragment.text(content, R.id.tvLocation, "Thư viện trường");
            StudyScreenFragment.text(content, R.id.tvNote, "Mang theo máy tính và tài liệu ôn tập.");
            StudyScreenFragment.on(content, R.id.btnCancel, () -> { dialog.dismiss(); show(activity, R.layout.dialog_cancel_reason); });
            View rating = content.findViewById(R.id.layoutRatingSection);
            if (rating != null) rating.setVisibility(View.VISIBLE);
        }
        if (layout == R.layout.dialog_appointment_reminder) {
            StudyScreenFragment.text(content, R.id.tvSubject, "Lập trình Android");
            StudyScreenFragment.text(content, R.id.tvTime, "14:30 · Hôm nay");
            StudyScreenFragment.text(content, R.id.tvLocation, "Thư viện trường");
        }
        if (layout == R.layout.dialog_user_info) {
            StudyScreenFragment.text(content, R.id.tvUserName, "Minh Anh");
            StudyScreenFragment.text(content, R.id.tvDistance, "Cách bạn 1,2 km");
            StudyScreenFragment.text(content, R.id.tvOnlineStatus, "Đang hoạt động");
            StudyScreenFragment.text(content, R.id.tvStatusTag, "Tìm bạn học Java");
            StudyScreenFragment.text(content, R.id.tvSchool, "Đại học Công nghiệp Hà Nội");
            StudyScreenFragment.text(content, R.id.tvMajor, "Công nghệ thông tin");
        }
        StudyScreenFragment.text(content, R.id.etStatusTag, "Cùng nhau học tốt hơn mỗi ngày.");
        if (layout == R.layout.dialog_edit_personal_info) {
            StudyScreenFragment.text(content, R.id.etLastName, "Nguyễn");
            StudyScreenFragment.text(content, R.id.etFirstName, "Minh An");
            StudyScreenFragment.text(content, R.id.etDateOfBirth, "15/08/2005");
            StudyScreenFragment.text(content, R.id.etUniversity, "Đại học Công nghiệp Hà Nội");
            StudyScreenFragment.text(content, R.id.etMajorName, "Công nghệ thông tin");
            StudyScreenFragment.on(content, R.id.etDateOfBirth, () -> new DatePickerDialog(activity,
                    (picker, year, month, day) -> StudyScreenFragment.text(content, R.id.etDateOfBirth,
                            day + "/" + (month + 1) + "/" + year), 2005, 7, 15).show());
        }
        if (layout == R.layout.dialog_subject_management) {
            subject(content, R.id.layoutStrengthSubjects, "Lập trình Java");
            subject(content, R.id.layoutStrengthSubjects, "Lập trình Android");
            subject(content, R.id.layoutReviewSubjects, "Cơ sở dữ liệu");
        }
        if (layout == R.layout.dialog_login_success) StudyScreenFragment.hide(content, R.id.pbLoading);
        if (layout == R.layout.dialog_forward_select) {
            androidx.fragment.app.Fragment current = activity.getSupportFragmentManager().findFragmentById(R.id.screenHost);
            if (current instanceof StudyScreenFragment) {
                Runnable select = () -> {
                    Toast.makeText(activity, "Đã chọn hội thoại minh họa, chưa gửi tin nhắn.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                };
                UiRows.Adapter conversations = new UiRows.Adapter((StudyScreenFragment) current,
                        new UiRows.Row(R.layout.item_friend).with(R.id.tvFriendName, "Minh Anh")
                                .with(R.id.tvLastMessage, "Hội thoại cá nhân").click(select),
                        new UiRows.Row(R.layout.item_friend).with(R.id.tvFriendName, "Nhóm học Android")
                                .with(R.id.tvLastMessage, "Hội thoại nhóm").click(select));
                StudyScreenFragment.list(content, R.id.rvConversations, conversations);
            }
        }
        for (int id : new int[]{R.id.tvEmojiLike, R.id.tvEmojiHeart, R.id.tvEmojiLaugh, R.id.tvEmojiWow,
                R.id.tvEmojiSad, R.id.tvEmojiPray, R.id.tvReply})
            StudyScreenFragment.on(content, id, dialog::dismiss);
        StudyScreenFragment.on(content, R.id.tvForward, () -> { dialog.dismiss(); show(activity, R.layout.dialog_forward_select); });
        dialog.setOnShowListener(ignored -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setLayout((int) (activity.getResources().getDisplayMetrics().widthPixels * 0.92),
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });
        dialog.show();
    }

    private static void subject(View root, int containerId, String name) {
        LinearLayout container = root.findViewById(containerId);
        View row = LayoutInflater.from(root.getContext()).inflate(R.layout.item_dialog_subject, container, false);
        StudyScreenFragment.text(row, R.id.tvSubjectName, name);
        StudyScreenFragment.on(row, R.id.btnDeleteSubject, () -> container.removeView(row));
        container.addView(row);
    }
    private UiDialogs() { }
}
