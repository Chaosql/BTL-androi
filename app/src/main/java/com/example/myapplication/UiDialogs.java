package com.example.myapplication;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.PopupWindow;
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
    private static String[] titles(MainActivity activity) {
        return new String[] {
            activity.getString(R.string.ui_dialog_add_subject_text_1), activity.getString(R.string.ui_uidialogs_text_2), activity.getString(R.string.ui_uidialogs_text_3), activity.getString(R.string.ui_uidialogs_text_4),
            activity.getString(R.string.confirm), activity.getString(R.string.ui_uidialogs_text_6), activity.getString(R.string.ui_uidialogs_text_7), activity.getString(R.string.ui_dialog_forward_select_text_1),
            activity.getString(R.string.ui_uidialogs_text_9), activity.getString(R.string.ui_uidialogs_text_10), activity.getString(R.string.ui_dialog_subject_management_text_1), activity.getString(R.string.ui_uidialogs_text_12),
            activity.getString(R.string.ui_uidialogs_text_13), activity.getString(R.string.ui_uidialogs_text_14)
        };
    }

    public static void showCatalog(MainActivity activity) {
        new MaterialAlertDialogBuilder(activity).setTitle(activity.getString(R.string.ui_mainactivity_text_1))
                .setItems(titles(activity), (dialog, which) -> show(activity, LAYOUTS[which]))
                .setNegativeButton(activity.getString(R.string.btn_close), null).show();
    }

    public static void show(MainActivity activity, int layout) {
        View content = LayoutInflater.from(activity).inflate(layout, null, false);
        View container = content;
        if (!(content instanceof ScrollView)) {
            ScrollView scroll = new ScrollView(activity);
            scroll.setFillViewport(false);
            scroll.addView(content, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            container = scroll;
        }
        AlertDialog dialog = new AlertDialog.Builder(activity).setView(container).create();
        for (int id : new int[]{R.id.btnCancel, R.id.btnCancelAdd, R.id.ivClose, R.id.btnClose})
            StudyScreenFragment.on(content, id, dialog::dismiss);
        for (int id : new int[]{R.id.btnSave, R.id.btnConfirm, R.id.btnConfirmAdd, R.id.btnConfirmCancel,
                R.id.btnRate, R.id.btnAccept, R.id.btnReject, R.id.btnComplete, R.id.btnAddFriend,
                R.id.tvOptionPin, R.id.tvOptionBlock, R.id.tvOptionDelete, R.id.tvRecall, R.id.tvDelete}) {
            StudyScreenFragment.on(content, id, () -> {
                Toast.makeText(activity, activity.getString(R.string.ui_uidialogs_text_17), Toast.LENGTH_SHORT).show();
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
            StudyScreenFragment.text(content, R.id.tvTitle, activity.getString(R.string.ui_uidialogs_text_18));
            StudyScreenFragment.text(content, R.id.tvSubject, activity.getString(R.string.ui_uidialogs_text_19));
            StudyScreenFragment.text(content, R.id.tvPartnerName, "Minh Anh");
            StudyScreenFragment.text(content, R.id.tvTimeAndDate, activity.getString(R.string.ui_uidialogs_text_20)
                    + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            StudyScreenFragment.text(content, R.id.tvStatus, activity.getString(R.string.ui_uidialogs_text_21));
            StudyScreenFragment.text(content, R.id.tvLocation, activity.getString(R.string.ui_uidialogs_text_22));
            StudyScreenFragment.text(content, R.id.tvNote, activity.getString(R.string.ui_uidialogs_text_23));
            StudyScreenFragment.on(content, R.id.btnCancel, () -> { dialog.dismiss(); show(activity, R.layout.dialog_cancel_reason); });
            View rating = content.findViewById(R.id.layoutRatingSection);
            if (rating != null) rating.setVisibility(View.GONE);
            content.findViewById(R.id.btnComplete).setVisibility(View.VISIBLE);
            content.findViewById(R.id.btnCancel).setVisibility(View.VISIBLE);
        }
        if (layout == R.layout.dialog_appointment_reminder) {
            StudyScreenFragment.text(content, R.id.tvSubject, activity.getString(R.string.ui_uidialogs_text_24));
            StudyScreenFragment.text(content, R.id.tvTime, activity.getString(R.string.ui_uidialogs_text_25));
            StudyScreenFragment.text(content, R.id.tvLocation, activity.getString(R.string.ui_uidialogs_text_22));
        }
        if (layout == R.layout.dialog_user_info) {
            StudyScreenFragment.text(content, R.id.tvUserName, "Minh Anh");
            StudyScreenFragment.text(content, R.id.tvDistance, activity.getString(R.string.ui_uidialogs_text_27));
            StudyScreenFragment.text(content, R.id.tvOnlineStatus, activity.getString(R.string.ui_studyscreenfragment_text_21));
            StudyScreenFragment.text(content, R.id.tvStatusTag, activity.getString(R.string.ui_uidialogs_text_29));
            StudyScreenFragment.text(content, R.id.tvSchool, activity.getString(R.string.ui_uidialogs_text_30));
            StudyScreenFragment.text(content, R.id.tvMajor, activity.getString(R.string.ui_uidialogs_text_31));
        }
        StudyScreenFragment.text(content, R.id.etStatusTag, activity.getString(R.string.ui_studyscreenfragment_text_14));
        if (layout == R.layout.dialog_edit_personal_info) {
            StudyScreenFragment.text(content, R.id.etLastName, activity.getString(R.string.ui_studyscreenfragment_text_1));
            StudyScreenFragment.text(content, R.id.etFirstName, "Minh An");
            StudyScreenFragment.text(content, R.id.etDateOfBirth, "15/08/2005");
            StudyScreenFragment.text(content, R.id.etUniversity, activity.getString(R.string.ui_uidialogs_text_30));
            StudyScreenFragment.text(content, R.id.etMajorName, activity.getString(R.string.ui_uidialogs_text_31));
            StudyScreenFragment.on(content, R.id.etDateOfBirth, () -> new DatePickerDialog(activity,
                    (picker, year, month, day) -> StudyScreenFragment.text(content, R.id.etDateOfBirth,
                            day + "/" + (month + 1) + "/" + year), 2005, 7, 15).show());
        }
        if (layout == R.layout.dialog_subject_management) {
            subject(content, R.id.layoutStrengthSubjects, activity.getString(R.string.ui_uidialogs_text_19));
            subject(content, R.id.layoutStrengthSubjects, activity.getString(R.string.ui_uidialogs_text_24));
            subject(content, R.id.layoutReviewSubjects, activity.getString(R.string.ui_uidialogs_text_38));
        }
        if (layout == R.layout.dialog_login_success) StudyScreenFragment.hide(content, R.id.pbLoading);
        if (layout == R.layout.dialog_forward_select) {
            androidx.fragment.app.Fragment current = activity.getSupportFragmentManager().findFragmentById(R.id.screenHost);
            if (current instanceof StudyScreenFragment) {
                Runnable select = () -> {
                    Toast.makeText(activity, activity.getString(R.string.ui_uidialogs_text_39), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                };
                UiRows.Adapter conversations = new UiRows.Adapter((StudyScreenFragment) current,
                        new UiRows.Row(R.layout.item_friend).with(R.id.tvFriendName, "Minh Anh")
                                .with(R.id.tvLastMessage, activity.getString(R.string.ui_uidialogs_text_40)).click(select),
                        new UiRows.Row(R.layout.item_friend).with(R.id.tvFriendName, activity.getString(R.string.ui_studyscreenfragment_text_2))
                                .with(R.id.tvLastMessage, activity.getString(R.string.ui_uidialogs_text_42)).click(select));
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
                android.util.DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
                int width = Math.min((int) (metrics.widthPixels * 0.9f), (int) (560 * metrics.density));
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                dialog.getWindow().setLayout(width,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });
        dialog.show();
    }

    public static void showPopup(MainActivity activity, View anchor, int layout) {
        View content = LayoutInflater.from(activity).inflate(layout, null, false);
        PopupWindow popup = new PopupWindow(content, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setOutsideTouchable(true);
        popup.setElevation(8 * activity.getResources().getDisplayMetrics().density);
        StudyScreenFragment.on(content, R.id.btnMenuNewChat, () -> {
            popup.dismiss(); activity.showScreen(UiScreen.SEARCH_USER);
        });
        StudyScreenFragment.on(content, R.id.btnMenuPendingRequests, () -> {
            popup.dismiss(); activity.showScreen(UiScreen.PENDING_REQUESTS);
        });
        for (int id : new int[]{R.id.tvEmojiLike, R.id.tvEmojiHeart, R.id.tvEmojiLaugh, R.id.tvEmojiWow,
                R.id.tvEmojiSad, R.id.tvEmojiPray, R.id.tvReply, R.id.tvRecall, R.id.tvDelete})
            StudyScreenFragment.on(content, id, popup::dismiss);
        StudyScreenFragment.on(content, R.id.tvForward, () -> {
            popup.dismiss(); show(activity, R.layout.dialog_forward_select);
        });
        android.graphics.Rect bounds = new android.graphics.Rect();
        anchor.getWindowVisibleDisplayFrame(bounds);
        int maxWidth = Math.min(bounds.width(), (int) (360 * activity.getResources().getDisplayMetrics().density));
        content.measure(View.MeasureSpec.makeMeasureSpec(maxWidth, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(bounds.height(), View.MeasureSpec.AT_MOST));
        popup.setWidth(Math.min(maxWidth, content.getMeasuredWidth()));
        popup.setHeight(Math.min(bounds.height(), content.getMeasuredHeight()));
        if (layout == R.layout.layout_friends_menu_popup) {
            popup.showAsDropDown(anchor, 0, 0, Gravity.END);
        } else {
            int[] point = new int[2];
            anchor.getLocationOnScreen(point);
            int x = Math.max(bounds.left, Math.min(point[0], bounds.right - popup.getWidth()));
            int y = point[1] - popup.getHeight();
            if (y < bounds.top) y = Math.min(point[1] + anchor.getHeight(), bounds.bottom - popup.getHeight());
            popup.showAtLocation(anchor, Gravity.TOP | Gravity.LEFT, x, Math.max(bounds.top, y));
        }
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
