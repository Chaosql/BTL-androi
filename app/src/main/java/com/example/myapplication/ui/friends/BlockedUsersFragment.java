package com.example.myapplication.ui.friends;

import android.os.Bundle;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentBlockedUsersBinding;
import com.example.myapplication.ui.friends.adapter.BlockedUsersAdapter;

public class BlockedUsersFragment extends BaseSocialFragment<FragmentBlockedUsersBinding> {
    private BlockedUsersAdapter adapter;
    public BlockedUsersFragment() { super(FragmentBlockedUsersBinding::inflate); }
    @Override protected void onScreenCreated(View root, Bundle state) {
        adapter = new BlockedUsersAdapter(item -> new ConfirmDeleteDialog(host(),
                getString(R.string.social_unblock_confirm, SocialUi.name(item.firstName, item.lastName)),
                () -> blocks().unblockUser(item.id)).show());
        binding.rvBlockedUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBlockedUsers.setAdapter(adapter);
        binding.tvEmptyState.setOnClickListener(view -> load());
        changed(this::load);
        load();
    }
    private void load() {
        if (!signedIn()) { state(false, false); return; }
        state(true, false);
        query(blocks().getBlockedUsers(), response -> {
            if (response.data == null) { state(false, true); return; }
            adapter.submit(response.data);
            state(false, false);
        }, failure -> { state(false, true); error(failure.message); });
    }
    private void state(boolean loading, boolean failed) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.tvEmptyState.setText(failed ? R.string.social_retry : R.string.ui_fragment_blocked_users_text_1);
        binding.tvEmptyState.setVisibility(!loading && (failed || adapter.getItemCount() == 0) ? View.VISIBLE : View.GONE);
    }
}
