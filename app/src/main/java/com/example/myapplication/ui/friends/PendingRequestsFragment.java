package com.example.myapplication.ui.friends;

import com.example.myapplication.ui.friends.adapter.PendingRequestsAdapter;
import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;

public class PendingRequestsFragment extends BaseFragment {
    public PendingRequestsFragment() { super(R.layout.fragment_pending_requests); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        hide(root, R.id.tvEmptyState);
        list(root, R.id.rvPendingRequests, new PendingRequestsAdapter(this));
    }
}
