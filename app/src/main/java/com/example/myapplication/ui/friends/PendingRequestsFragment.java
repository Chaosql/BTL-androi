package com.example.myapplication.ui.friends;

import android.os.Bundle;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myapplication.R;
import com.example.myapplication.data.remote.dto.response.FriendDto;
import com.example.myapplication.databinding.FragmentPendingRequestsBinding;
import com.example.myapplication.ui.friends.adapter.PendingRequestsAdapter;
import java.util.ArrayList;
import java.util.List;

public class PendingRequestsFragment extends BaseSocialFragment<FragmentPendingRequestsBinding> {
    private PendingRequestsAdapter adapter;
    private final List<FriendDto> data = new ArrayList<>();
    private int page;
    private boolean last;
    private boolean loading;
    public PendingRequestsFragment() { super(FragmentPendingRequestsBinding::inflate); }
    @Override protected void onScreenCreated(View root, Bundle state) {
        data.clear(); page = -1; last = false; loading = false;
        adapter = new PendingRequestsAdapter(item -> user(item.userId, "PENDING"),
                item -> answer(item, true), item -> answer(item, false));
        binding.rvPendingRequests.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPendingRequests.setAdapter(adapter);
        pageScroll(binding.rvPendingRequests, () -> load(false));
        binding.tvEmptyState.setOnClickListener(view -> load(true));
        load(true);
    }
    private void load(boolean reset) {
        if (!signedIn()) { state(false, false); return; }
        if (!reset && (loading || last)) return;
        int next = reset ? 0 : page + 1;
        loading = true; state(true, false);
        query(friends().getPendingFriendRequests(next, 20, null), response -> {
            loading = false;
            if (response.data == null || response.data.content == null) { state(false, true); return; }
            if (reset) data.clear();
            for (FriendDto item : response.data.content) {
                if (item == null || item.requestId == null) continue;
                data.removeIf(existing -> item.requestId.equals(existing.requestId));
                data.add(item);
            }
            page = next;
            last = response.data.totalPages != null && response.data.totalPages > 0
                    ? next + 1 >= response.data.totalPages : Boolean.TRUE.equals(response.data.last);
            adapter.submit(data); state(false, false);
        }, failure -> { loading = false; state(false, true); error(failure.message); });
    }
    private void answer(FriendDto item, boolean accept) {
        if (adapter.isBusy(item.requestId)) return;
        adapter.busy(item.requestId, true);
        request(accept ? friends().acceptFriendRequest(item.requestId) : friends().rejectFriendRequest(item.requestId),
                response -> {
                    data.remove(item);
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
