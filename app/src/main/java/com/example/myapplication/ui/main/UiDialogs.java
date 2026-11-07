package com.example.myapplication.ui.main;

import com.example.myapplication.ui.chat.component.MessageActionsDialog;

import com.example.myapplication.ui.friends.FriendsMenuDialog;

import com.example.myapplication.ui.friends.UserInfoDialog;

import com.example.myapplication.ui.profile.SubjectManagementDialog;

import com.example.myapplication.ui.auth.LoginSuccessDialog;

import com.example.myapplication.ui.friends.FriendOptionsDialog;


import com.example.myapplication.ui.profile.EditPersonalInfoDialog;

import com.example.myapplication.ui.profile.EditIntroductionDialog;

import com.example.myapplication.ui.friends.ConfirmDeleteDialog;

import com.example.myapplication.ui.schedule.CancelBookingDialog;

import com.example.myapplication.ui.schedule.BookingDetailDialog;

import com.example.myapplication.ui.schedule.AppointmentReminderDialog;

import com.example.myapplication.ui.profile.AddSubjectDialog;

import com.example.myapplication.R;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.widget.PopupWindow;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public final class UiDialogs {
    public static final int[] LAYOUTS = {
            R.layout.dialog_add_subject, R.layout.dialog_appointment_reminder,
            R.layout.dialog_booking_detail, R.layout.dialog_cancel_reason,
            R.layout.dialog_confirm_delete, R.layout.dialog_edit_introduction,
            R.layout.dialog_edit_personal_info,
            R.layout.dialog_friend_options, R.layout.dialog_login_success,
            R.layout.dialog_subject_management, R.layout.dialog_user_info,
            R.layout.layout_friends_menu_popup, R.layout.layout_message_actions
    };
    private static String[] titles(MainActivity activity) {
        return new String[] {
            activity.getString(R.string.ui_dialog_add_subject_text_1), activity.getString(R.string.ui_uidialogs_text_2), activity.getString(R.string.ui_uidialogs_text_3), activity.getString(R.string.ui_uidialogs_text_4),
            activity.getString(R.string.confirm), activity.getString(R.string.ui_uidialogs_text_6), activity.getString(R.string.ui_uidialogs_text_7),
            activity.getString(R.string.ui_uidialogs_text_9), activity.getString(R.string.ui_uidialogs_text_10), activity.getString(R.string.ui_dialog_subject_management_text_1), activity.getString(R.string.ui_uidialogs_text_12),
            activity.getString(R.string.ui_uidialogs_text_13), activity.getString(R.string.message_actions_title)
        };
    }

    public static void showCatalog(MainActivity activity) {
        new MaterialAlertDialogBuilder(activity).setTitle(activity.getString(R.string.ui_mainactivity_text_1))
                .setItems(titles(activity), (dialog, which) -> show(activity, LAYOUTS[which]))
                .setNegativeButton(activity.getString(R.string.btn_close), null).show();
    }

    public static void show(MainActivity activity, int layout) {
        if (layout == R.layout.dialog_add_subject) { new AddSubjectDialog(activity).show(); return; }
        if (layout == R.layout.dialog_appointment_reminder) { new AppointmentReminderDialog(activity).show(); return; }
        if (layout == R.layout.dialog_booking_detail) { new BookingDetailDialog(activity).show(); return; }
        if (layout == R.layout.dialog_cancel_reason) { new CancelBookingDialog(activity).show(); return; }
        if (layout == R.layout.dialog_confirm_delete) { new ConfirmDeleteDialog(activity).show(); return; }
        if (layout == R.layout.dialog_edit_introduction) { new EditIntroductionDialog(activity).show(); return; }
        if (layout == R.layout.dialog_edit_personal_info) { new EditPersonalInfoDialog(activity).show(); return; }
        if (layout == R.layout.dialog_friend_options) { new FriendOptionsDialog(activity).show(); return; }
        if (layout == R.layout.dialog_login_success) { new LoginSuccessDialog(activity).show(); return; }
        if (layout == R.layout.dialog_subject_management) { new SubjectManagementDialog(activity).show(); return; }
        if (layout == R.layout.dialog_user_info) { new UserInfoDialog(activity).show(); return; }
        if (layout == R.layout.layout_friends_menu_popup) { new FriendsMenuDialog(activity).show(); return; }
        if (layout == R.layout.layout_message_actions) { new MessageActionsDialog(activity).show(); return; }
        throw new IllegalArgumentException("Unknown dialog layout: " + layout);
    }

    public static void showPopup(MainActivity activity, View anchor, int layout) {
        androidx.viewbinding.ViewBinding binding;
        if (layout == R.layout.layout_friends_menu_popup)
            binding = com.example.myapplication.databinding.LayoutFriendsMenuPopupBinding.inflate(LayoutInflater.from(activity));
        else if (layout == R.layout.layout_message_actions)
            binding = com.example.myapplication.databinding.LayoutMessageActionsBinding.inflate(LayoutInflater.from(activity));
        else throw new IllegalArgumentException("Unknown popup layout: " + layout);
        View content = binding.getRoot();
        PopupWindow popup = new PopupWindow(content, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setOutsideTouchable(true);
        popup.setElevation(8 * activity.getResources().getDisplayMetrics().density);
        if (binding instanceof com.example.myapplication.databinding.LayoutFriendsMenuPopupBinding) {
            com.example.myapplication.databinding.LayoutFriendsMenuPopupBinding menu =
                    (com.example.myapplication.databinding.LayoutFriendsMenuPopupBinding) binding;
            menu.btnMenuNewChat.setOnClickListener(view -> {
                popup.dismiss(); activity.showScreen(UiScreen.SEARCH_USER);
            });
            menu.btnMenuPendingRequests.setOnClickListener(view -> {
                popup.dismiss(); activity.showScreen(UiScreen.PENDING_REQUESTS);
            });
        } else {
            com.example.myapplication.databinding.LayoutMessageActionsBinding actions =
                    (com.example.myapplication.databinding.LayoutMessageActionsBinding) binding;
            actions.tvRecall.setOnClickListener(view -> popup.dismiss());
            actions.tvDelete.setOnClickListener(view -> popup.dismiss());
        }
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


    private UiDialogs() { }
}
