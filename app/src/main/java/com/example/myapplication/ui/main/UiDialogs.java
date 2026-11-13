package com.example.myapplication.ui.main;

import com.example.myapplication.R;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.widget.PopupWindow;

public final class UiDialogs {
    public static void showPopup(MainActivity activity, View anchor, int layout) {
        if (layout != R.layout.layout_friends_menu_popup) throw new IllegalArgumentException("Unknown popup layout: " + layout);
        com.example.myapplication.databinding.LayoutFriendsMenuPopupBinding menu =
                com.example.myapplication.databinding.LayoutFriendsMenuPopupBinding.inflate(LayoutInflater.from(activity));
        PopupWindow popup = createPopup(activity, menu.getRoot());
        menu.btnMenuNewChat.setOnClickListener(view -> {
            popup.dismiss(); activity.showScreen(UiScreen.SEARCH_USER);
        });
        menu.btnMenuPendingRequests.setOnClickListener(view -> {
            popup.dismiss(); activity.showScreen(UiScreen.PENDING_REQUESTS);
        });
        measurePopup(activity, anchor, popup);
        popup.showAsDropDown(anchor, 0, 0, Gravity.END);
    }


    public static PopupWindow showAnchoredPopup(MainActivity activity, View anchor, View content) {
        PopupWindow popup = createPopup(activity, content);
        measurePopup(activity, anchor, popup);
        popup.showAsDropDown(anchor, 0, 0, Gravity.END);
        return popup;
    }
    private static PopupWindow createPopup(MainActivity activity, View content) {
        PopupWindow popup = new PopupWindow(content, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setOutsideTouchable(true);
        popup.setElevation(8 * activity.getResources().getDisplayMetrics().density);
        return popup;
    }
    private static android.graphics.Rect measurePopup(MainActivity activity, View anchor, PopupWindow popup) {
        View content = popup.getContentView();
        android.graphics.Rect bounds = new android.graphics.Rect();
        anchor.getWindowVisibleDisplayFrame(bounds);
        int maxWidth = Math.min(bounds.width(), (int) (360 * activity.getResources().getDisplayMetrics().density));
        content.measure(View.MeasureSpec.makeMeasureSpec(maxWidth, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(bounds.height(), View.MeasureSpec.AT_MOST));
        popup.setWidth(Math.min(maxWidth, content.getMeasuredWidth()));
        popup.setHeight(Math.min(bounds.height(), content.getMeasuredHeight()));
        return bounds;
    }

    private UiDialogs() { }
}
