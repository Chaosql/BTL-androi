package com.example.myapplication.ui.map;

import com.example.myapplication.databinding.FragmentMapBinding;
import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.main.UiScreen;

public class MapFragment extends BaseFragment<FragmentMapBinding> {
    private StudyMap studyMap;
    public MapFragment() { super(FragmentMapBinding::inflate); }

    @Override public void onResume() {
        super.onResume();
        if (studyMap != null) studyMap.resume();
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
        studyMap = new StudyMap(binding.mapView, message -> {
            if (binding == null) return;
            binding.tvMapStatus.setText(message);
            binding.tvMapStatus.setVisibility(message.isEmpty() ? View.GONE : View.VISIBLE);
        });
        studyMap.create(state == null ? null : state.getBundle("map_state"));
        binding.tvMapStatus.setOnClickListener(view -> studyMap.create(null));
        binding.btnProfile.setOnClickListener(bindingView -> go(UiScreen.PROFILE));
        binding.imgAvatar.setOnClickListener(bindingView -> go(UiScreen.PROFILE));
        binding.btnFriend.setOnClickListener(bindingView -> go(UiScreen.CONVERSATIONS));
        binding.btnCalendar.setOnClickListener(bindingView -> go(UiScreen.SCHEDULE));
        binding.btnNotification.setOnClickListener(bindingView -> go(UiScreen.NOTIFICATIONS));
        binding.btnRadar.setContentDescription(getString(R.string.map_description));
        binding.imgGroupRadar.setContentDescription(getString(R.string.map_description));
        binding.btnRadar.setOnClickListener(view -> android.widget.Toast.makeText(requireContext(), R.string.map_pending_scan, android.widget.Toast.LENGTH_SHORT).show());
        binding.imgGroupRadar.setOnClickListener(view -> android.widget.Toast.makeText(requireContext(), R.string.map_pending_scan, android.widget.Toast.LENGTH_SHORT).show());
        binding.imgSearch.setOnClickListener(bindingView -> go(UiScreen.SEARCH_USER));
    }
    @Override public void onStart() { super.onStart(); if (studyMap != null) studyMap.start(); }
    @Override public void onPause() { if (studyMap != null) studyMap.pause(); super.onPause(); }
    @Override public void onStop() { if (studyMap != null) studyMap.stop(); super.onStop(); }
    @Override public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        if (studyMap != null) { Bundle mapState = new Bundle(); studyMap.save(mapState); state.putBundle("map_state", mapState); }
    }
    @Override public void onLowMemory() { super.onLowMemory(); if (studyMap != null) studyMap.lowMemory(); }
    @Override public void onDestroyView() {
        if (studyMap != null) { studyMap.destroy(); studyMap = null; }
        super.onDestroyView();
    }
}
