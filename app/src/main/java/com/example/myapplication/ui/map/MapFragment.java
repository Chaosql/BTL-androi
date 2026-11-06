package com.example.myapplication.ui.map;

import com.example.myapplication.databinding.FragmentMapBinding;
import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.main.UiScreen;

public class MapFragment extends BaseFragment<FragmentMapBinding> {
    public MapFragment() { super(FragmentMapBinding::inflate); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        binding.btnProfile.setOnClickListener(bindingView -> go(UiScreen.PROFILE));
        binding.imgAvatar.setOnClickListener(bindingView -> go(UiScreen.PROFILE));
        binding.btnFriend.setOnClickListener(bindingView -> go(UiScreen.FRIENDS));
        binding.btnCalendar.setOnClickListener(bindingView -> go(UiScreen.SCHEDULE));
        binding.btnNotification.setOnClickListener(bindingView -> go(UiScreen.NOTIFICATIONS));
        binding.btnRadar.setOnClickListener(bindingView -> dialog(R.layout.dialog_user_info));
        binding.imgGroupRadar.setOnClickListener(bindingView -> dialog(R.layout.dialog_user_info));
        binding.mapView.setOnClickListener(bindingView -> dialog(R.layout.dialog_user_info));
        binding.imgSearch.setOnClickListener(bindingView -> go(UiScreen.SEARCH_USER));
    }
}
