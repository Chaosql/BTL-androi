package com.example.myapplication.ui.map;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.main.UiScreen;

public class MapFragment extends BaseFragment {
    public MapFragment() { super(R.layout.fragment_map); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        on(root, R.id.btnProfile, () -> go(UiScreen.PROFILE));
        on(root, R.id.imgAvatar, () -> go(UiScreen.PROFILE));
        on(root, R.id.btnFriend, () -> go(UiScreen.FRIENDS));
        on(root, R.id.btnCalendar, () -> go(UiScreen.SCHEDULE));
        on(root, R.id.btnNotification, () -> go(UiScreen.NOTIFICATIONS));
        on(root, R.id.btnRadar, () -> dialog(R.layout.dialog_user_info));
        on(root, R.id.imgGroupRadar, () -> dialog(R.layout.dialog_user_info));
        on(root, R.id.mapView, () -> dialog(R.layout.dialog_user_info));
        on(root, R.id.imgSearch, () -> go(UiScreen.SEARCH_USER));
    }
}
