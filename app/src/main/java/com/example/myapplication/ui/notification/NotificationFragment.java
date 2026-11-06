package com.example.myapplication.ui.notification;

import com.example.myapplication.databinding.FragmentNotificationBinding;
import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;

public class NotificationFragment extends BaseFragment<FragmentNotificationBinding> {
    public NotificationFragment() { super(FragmentNotificationBinding::inflate); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        binding.layoutEmptyState.setVisibility(android.view.View.GONE);
        binding.scrollViewContent.setVisibility(View.VISIBLE);
        list(root, R.id.rvRecentNotifications, new NotificationAdapter(this, true));
        list(root, R.id.rvEarlierNotifications, new NotificationAdapter(this, false));
        binding.tvMarkAllRead.setOnClickListener(bindingView -> this.previewNotice());
    }
}
