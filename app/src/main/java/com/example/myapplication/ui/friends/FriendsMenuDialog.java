package com.example.myapplication.ui.friends;

import com.example.myapplication.databinding.LayoutFriendsMenuPopupBinding;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import android.view.View;
import androidx.appcompat.app.AlertDialog;

public class FriendsMenuDialog extends BasePreviewDialog<LayoutFriendsMenuPopupBinding> {
    public FriendsMenuDialog(MainActivity activity) { super(activity, LayoutFriendsMenuPopupBinding::inflate); }

    @Override protected void bind(View content, AlertDialog dialog) {
        binding.btnMenuNewChat.setOnClickListener(view -> { dialog.dismiss(); activity.showScreen(com.example.myapplication.ui.main.UiScreen.SEARCH_USER); });
        binding.btnMenuPendingRequests.setOnClickListener(view -> { dialog.dismiss(); activity.showScreen(com.example.myapplication.ui.main.UiScreen.PENDING_REQUESTS); });

    }
}
