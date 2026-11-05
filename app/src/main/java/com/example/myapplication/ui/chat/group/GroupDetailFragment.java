package com.example.myapplication.ui.chat.group;

import com.example.myapplication.ui.friends.adapter.FriendsAdapter;
import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.main.MainActivity;
import com.example.myapplication.ui.main.UiDialogs;

public class GroupDetailFragment extends BaseFragment {
    public GroupDetailFragment() { super(R.layout.fragment_friends_list); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        FriendsAdapter friends = new FriendsAdapter(this, true);
        list(root, R.id.rvFriendsList, friends);
        filter(root, R.id.etSearchFriends, friends);
        on(root, R.id.btnHeaderMenu, () -> UiDialogs.showPopup((MainActivity) requireActivity(),
                root.findViewById(R.id.btnHeaderMenu), R.layout.layout_friends_menu_popup));
        text(root, R.id.tvHeaderTitle, getString(R.string.ui_studyscreenfragment_text_2));
    }
}
