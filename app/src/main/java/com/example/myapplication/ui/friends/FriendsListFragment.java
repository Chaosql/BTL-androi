package com.example.myapplication.ui.friends;

import com.example.myapplication.databinding.FragmentFriendsListBinding;
import com.example.myapplication.ui.friends.adapter.FriendsAdapter;
import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.main.MainActivity;
import com.example.myapplication.ui.main.UiDialogs;

public class FriendsListFragment extends BaseFragment<FragmentFriendsListBinding> {
    public FriendsListFragment() { super(FragmentFriendsListBinding::inflate); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        FriendsAdapter friends = new FriendsAdapter(this, false);
        list(root, R.id.rvFriendsList, friends);
        filter(root, R.id.etSearchFriends, friends);
        binding.btnHeaderMenu.setOnClickListener(bindingView -> UiDialogs.showPopup((MainActivity) requireActivity(),
                binding.btnHeaderMenu, R.layout.layout_friends_menu_popup));
    }
}
