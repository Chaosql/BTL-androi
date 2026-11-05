package com.example.myapplication.ui.notification;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;

public class NotificationFragment extends BaseFragment {
    public NotificationFragment() { super(R.layout.fragment_notification); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        hide(root, R.id.layoutEmptyState);
        root.findViewById(R.id.scrollViewContent).setVisibility(View.VISIBLE);
        list(root, R.id.rvRecentNotifications, new NotificationAdapter(this, true));
        list(root, R.id.rvEarlierNotifications, new NotificationAdapter(this, false));
        on(root, R.id.tvMarkAllRead, this::previewNotice);
    }
}
