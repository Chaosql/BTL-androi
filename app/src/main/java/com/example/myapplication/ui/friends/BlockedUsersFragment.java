package com.example.myapplication.ui.friends;

import com.example.myapplication.databinding.FragmentBlockedUsersBinding;
import com.example.myapplication.ui.friends.adapter.BlockedUsersAdapter;
import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;

public class BlockedUsersFragment extends BaseFragment<FragmentBlockedUsersBinding> {
    public BlockedUsersFragment() { super(FragmentBlockedUsersBinding::inflate); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        binding.tvEmptyState.setVisibility(android.view.View.GONE);
        list(root, R.id.rvBlockedUsers, new BlockedUsersAdapter(this));
    }
}
