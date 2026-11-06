package com.example.myapplication.ui.friends;

import com.example.myapplication.databinding.FragmentPendingRequestsBinding;
import com.example.myapplication.ui.friends.adapter.PendingRequestsAdapter;
import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;

public class PendingRequestsFragment extends BaseFragment<FragmentPendingRequestsBinding> {
    public PendingRequestsFragment() { super(FragmentPendingRequestsBinding::inflate); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        binding.tvEmptyState.setVisibility(android.view.View.GONE);
        list(root, R.id.rvPendingRequests, new PendingRequestsAdapter(this));
    }
}
