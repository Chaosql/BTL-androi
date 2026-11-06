package com.example.myapplication.ui.map;

import com.example.myapplication.databinding.FragmentMapBinding;
import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.main.UiScreen;

public class MapFragment extends BaseFragment<FragmentMapBinding> {
    public MapFragment() { super(FragmentMapBinding::inflate); }

    @Override public void onResume() {
        super.onResume();
        if (binding == null || !new com.example.myapplication.data.local.PreferenceManager(requireContext()).isLogin()) return;
        request(new com.example.myapplication.data.repository.notification.NotificationRepository(requireContext())
                .getUnreadNotificationCount(), response -> {
            int count = response.data == null ? 0 : Math.max(0, response.data);
            binding.tvNotificationBadge.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
            binding.tvNotificationBadge.setText(count > 99 ? "99+" : String.valueOf(count));
            binding.btnNotification.setContentDescription(count > 0 ? getString(R.string.notification_title_count, count)
                    : getString(R.string.title_notification));
        }, error -> { });
    }

    @Override protected void onScreenCreated(View root, Bundle state) {
        binding.btnProfile.setOnClickListener(bindingView -> go(UiScreen.PROFILE));
        binding.imgAvatar.setOnClickListener(bindingView -> go(UiScreen.PROFILE));
        binding.btnFriend.setOnClickListener(bindingView -> go(UiScreen.CONVERSATIONS));
        binding.btnCalendar.setOnClickListener(bindingView -> go(UiScreen.SCHEDULE));
        binding.btnNotification.setOnClickListener(bindingView -> go(UiScreen.NOTIFICATIONS));
        binding.btnRadar.setOnClickListener(bindingView -> dialog(R.layout.dialog_user_info));
        binding.imgGroupRadar.setOnClickListener(bindingView -> dialog(R.layout.dialog_user_info));
        binding.mapView.setOnClickListener(bindingView -> dialog(R.layout.dialog_user_info));
        binding.imgSearch.setOnClickListener(bindingView -> go(UiScreen.SEARCH_USER));
    }
}
