package com.example.myapplication.ui.friends;

import android.os.Bundle;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myapplication.R;
import com.example.myapplication.data.remote.dto.response.FriendDto;
import com.example.myapplication.databinding.FragmentFriendsListBinding;
import com.example.myapplication.ui.friends.adapter.FriendsAdapter;
import com.example.myapplication.ui.main.UiDialogs;
import java.util.ArrayList;
import java.util.List;

public class FriendsListFragment extends BaseSocialFragment<FragmentFriendsListBinding> {
    private FriendsAdapter adapter;
    private final List<FriendDto> data = new ArrayList<>();
    private int page;
    private boolean last;
    private boolean loading;
    public FriendsListFragment() { super(FragmentFriendsListBinding::inflate); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        data.clear(); page = -1; last = false; loading = false;
        adapter = new FriendsAdapter(item -> user(item.userId, "FRIEND"),
                item -> new FriendOptionsDialog(host(), item).show());
        binding.rvFriendsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFriendsList.setAdapter(adapter);
        search(binding.etSearchFriends, () -> load(true));
        pageScroll(binding.rvFriendsList, () -> load(false));
        changed(() -> load(true));
        binding.tvEmptyState.setOnClickListener(view -> load(true));
        binding.btnHeaderMenu.setOnClickListener(view -> UiDialogs.showPopup(host(), binding.btnHeaderMenu, R.layout.layout_friends_menu_popup));
        load(true);
    }

    private void load(boolean reset) {
        if (!signedIn()) { state(false, false); return; }
        if (!reset && (loading || last)) return;
        int next = reset ? 0 : page + 1;
        String keyword = binding.etSearchFriends.getText().toString().trim();
        loading = true; state(true, false);
        query(keyword.isEmpty() ? friends().getFriends(next, 20, null) : friends().searchFriends(keyword, next, 20, null),
                response -> {
                    loading = false;
                    if (response.data == null || response.data.content == null) { state(false, true); return; }
                    if (reset) data.clear();
                    for (FriendDto item : response.data.content) {
                        if (item == null || item.userId == null) continue;
                        data.removeIf(existing -> item.userId.equals(existing.userId));
                        data.add(item);
                    }
                    page = next;
                    last = response.data.totalPages != null && response.data.totalPages > 0
                            ? next + 1 >= response.data.totalPages : Boolean.TRUE.equals(response.data.last);
                    adapter.submit(data);
                    state(false, false);
                }, failure -> { loading = false; state(false, true); error(failure.message); });
    }

    private void state(boolean loading, boolean failed) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.tvEmptyState.setText(failed ? R.string.social_retry :
                binding.etSearchFriends.getText().toString().trim().isEmpty() ? R.string.social_empty : R.string.social_no_results);
        binding.tvEmptyState.setVisibility(!loading && (failed || adapter.getItemCount() == 0) ? View.VISIBLE : View.GONE);
    }
}
