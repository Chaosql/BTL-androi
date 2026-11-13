package com.example.myapplication.ui.friends;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.data.remote.dto.response.FriendDto;
import com.example.myapplication.databinding.FragmentPendingRequestsBinding;
import com.example.myapplication.ui.friends.adapter.PendingRequestsAdapter;

public class PendingRequestsFragment extends BaseSocialFragment<FragmentPendingRequestsBinding> {
    private PendingRequestsAdapter adapter;
    private com.example.myapplication.ui.base.PagedList<FriendDto> pages;
    public PendingRequestsFragment() { super(FragmentPendingRequestsBinding::inflate); }
    @Override protected void onScreenCreated(View root, Bundle saved) {
        binding.btnBack.setOnClickListener(view -> back());
        binding.progressBar.setVisibility(View.GONE);
        pages = friends().pendingPage();
        adapter = new PendingRequestsAdapter(item -> user(item.userId, "PENDING"),
                item -> answer(item, true), item -> answer(item, false));

        binding.rvPendingRequests.setAdapter(adapter);
        pages.state().observe(getViewLifecycleOwner(), state -> {
            adapter.submit(state.items);
            state(state.loading, state.error != null);
            if (state.error != null) error(state.error.message);
        });

        pageScroll(binding.rvPendingRequests, () -> load(false));
        binding.tvEmptyState.setOnClickListener(view -> load(true));
        if (signedIn()) pages.initialize("");
        else state(false, false);
    }
    private void load(boolean reset) {
        if (!signedIn()) { state(false, false); return; }
        pages.load("", reset);
    }
    @Override protected void onQueryInvalidated() { if (pages != null) pages.cancel(); }

    private void answer(FriendDto item, boolean accept) {
        if (adapter.isBusy(item.requestId)) return;
        adapter.busy(item.requestId, true);
        request(accept ? friends().acceptFriendRequest(item.requestId) : friends().rejectFriendRequest(item.requestId),
                response -> {
                    pages.remove(item.requestId);
                    adapter.remove(item);
                    adapter.busy(item.requestId, false);
                    state(false, false);
                    SocialUi.updated(requireActivity());
                    load(true);
                }, failure -> { adapter.busy(item.requestId, false); error(failure.message); });
    }
    private void state(boolean loading, boolean failed) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.tvEmptyState.setText(failed ? R.string.social_retry : R.string.ui_fragment_pending_requests_text_2);
        binding.tvEmptyState.setVisibility(!loading && (failed || adapter.getItemCount() == 0) ? View.VISIBLE : View.GONE);
    }
}
