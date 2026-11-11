package com.example.myapplication.ui.friends;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.data.remote.dto.response.FriendDto;
import com.example.myapplication.databinding.FragmentFriendsListBinding;
import com.example.myapplication.ui.friends.adapter.FriendsAdapter;
import com.example.myapplication.ui.main.UiDialogs;

public class FriendsListFragment extends BaseSocialFragment<FragmentFriendsListBinding> {
    private FriendsAdapter adapter;
    private com.example.myapplication.ui.base.PagedList<FriendDto> pages;
    public FriendsListFragment() { super(FragmentFriendsListBinding::inflate); }

    @Override protected void onScreenCreated(View root, Bundle saved) {
        enableScreenCatalog(binding.tvHeaderTitle);
        binding.btnBack.setOnClickListener(view -> back());
        binding.progressBar.setVisibility(View.GONE);
        pages = friends().friendsPage();
        adapter = new FriendsAdapter(item -> user(item.userId, "FRIEND"),
                item -> new FriendOptionsDialog(host(), item).show());

        binding.rvFriendsList.setAdapter(adapter);
        pages.state().observe(getViewLifecycleOwner(), state -> {
            adapter.submit(state.items);
            state(state.loading, state.error != null);
            if (state.error != null) error(state.error.message);
        });

        search(binding.etSearchFriends, () -> load(true));
        pageScroll(binding.rvFriendsList, () -> load(false));
        changed(() -> load(true));
        binding.tvEmptyState.setOnClickListener(view -> load(true));
        binding.btnHeaderMenu.setOnClickListener(view -> UiDialogs.showPopup(host(), binding.btnHeaderMenu, R.layout.layout_friends_menu_popup));
        if (signedIn()) pages.initialize(binding.etSearchFriends.getText().toString().trim());
        else state(false, false);
    }

    private void load(boolean reset) {
        if (!signedIn()) { state(false, false); return; }
        pages.load(binding.etSearchFriends.getText().toString().trim(), reset);
    }
    @Override protected void onQueryInvalidated() { if (pages != null) pages.cancel(); }

    private void state(boolean loading, boolean failed) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.tvEmptyState.setText(failed ? R.string.social_retry :
                binding.etSearchFriends.getText().toString().trim().isEmpty() ? R.string.social_empty : R.string.social_no_results);
        binding.tvEmptyState.setVisibility(!loading && (failed || adapter.getItemCount() == 0) ? View.VISIBLE : View.GONE);
    }
}
