package com.example.myapplication.ui.chat.group;

import com.example.myapplication.databinding.FragmentFriendsListBinding;
import com.example.myapplication.ui.friends.adapter.FriendsAdapter;
import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.main.MainActivity;
import com.example.myapplication.ui.main.UiDialogs;

public class GroupDetailFragment extends BaseFragment<FragmentFriendsListBinding> {
    public GroupDetailFragment() { super(FragmentFriendsListBinding::inflate); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        FriendsAdapter friends = new FriendsAdapter(this, true);
        list(root, R.id.rvFriendsList, friends);
        filter(root, R.id.etSearchFriends, friends);
        binding.btnHeaderMenu.setOnClickListener(bindingView -> UiDialogs.showPopup((MainActivity) requireActivity(),
                binding.btnHeaderMenu, R.layout.layout_friends_menu_popup));
        binding.tvHeaderTitle.setText(getString(R.string.ui_studyscreenfragment_text_2));
    }
}
