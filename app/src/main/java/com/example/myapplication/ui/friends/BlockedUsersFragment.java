package com.example.myapplication.ui.friends;

import com.example.myapplication.ui.friends.adapter.BlockedUsersAdapter;
import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;

public class BlockedUsersFragment extends BaseFragment {
    public BlockedUsersFragment() { super(R.layout.fragment_blocked_users); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        hide(root, R.id.tvEmptyState);
        list(root, R.id.rvBlockedUsers, new BlockedUsersAdapter(this));
    }
}
