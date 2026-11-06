package com.example.myapplication.ui.chat;

import android.os.Bundle;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myapplication.R;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.response.ConversationResponse;
import com.example.myapplication.data.repository.chat.ConversationRepository;
import com.example.myapplication.databinding.FragmentFriendsListBinding;
import com.example.myapplication.ui.chat.adapter.ConversationsAdapter;
import com.example.myapplication.ui.friends.BaseSocialFragment;
import com.example.myapplication.ui.main.UiScreen;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.*;

public class ConversationsFragment extends BaseSocialFragment<FragmentFriendsListBinding> {
    private ConversationsAdapter adapter;
    private final List<ConversationResponse> data = new ArrayList<>();
    private int page;
    private boolean last;
    private boolean loading;
    public ConversationsFragment() { super(FragmentFriendsListBinding::inflate); }
    @Override protected void onScreenCreated(View root, Bundle state) {
        data.clear(); page = -1; last = false; loading = false;
        binding.tvHeaderTitle.setText(R.string.chat_conversations);
        adapter = new ConversationsAdapter(new PreferenceManager(requireContext()).getUserId(), item -> {
            Bundle args = ChatUi.args(item.id, ChatUi.conversationName(item, new PreferenceManager(requireContext()).getUserId()), "");
            args.putBoolean("is_disabled", Boolean.TRUE.equals(item.hasLeft) || Integer.valueOf(1).equals(item.memberCount));
            go(UiScreen.CHAT, args);
        });
        binding.rvFriendsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFriendsList.setAdapter(adapter);
        search(binding.etSearchFriends, () -> load(true));
        pageScroll(binding.rvFriendsList, () -> load(false));
        binding.tvEmptyState.setOnClickListener(view -> load(true));
        getParentFragmentManager().setFragmentResultListener(ChatUi.UPDATED, getViewLifecycleOwner(), (key, value) -> load(true));
        binding.btnHeaderMenu.setOnClickListener(view -> new MaterialAlertDialogBuilder(requireContext())
                .setItems(new CharSequence[]{getString(R.string.title_friends), getString(R.string.ui_fragment_search_user_text_1),
                        getString(R.string.ui_fragment_pending_requests_text_1)}, (dialog, which) ->
                        go(which == 0 ? UiScreen.FRIENDS : which == 1 ? UiScreen.SEARCH_USER : UiScreen.PENDING_REQUESTS)).show());
        load(true);
    }
    private void load(boolean reset) {
        if (!signedIn()) { state(false, false); return; }
        if (!reset && (loading || last)) return;
        int next = reset ? 0 : page + 1;
        String keyword = binding.etSearchFriends.getText().toString().trim();
        loading = true; state(true, false);
        ConversationRepository repository = new ConversationRepository(requireContext());
        query(keyword.isEmpty() ? repository.getConversations(next, 20, null) : repository.searchConversations(keyword, next, 20, null), response -> {
            loading = false;
            if (response.data == null || response.data.content == null) { state(false, true); return; }
            if (reset) data.clear();
            for (ConversationResponse item : response.data.content) {
                if (item == null || item.id == null || item.id.isEmpty()) continue;
                data.removeIf(old -> item.id.equals(old.id)); data.add(item);
            }
            page = next;
            last = response.data.totalPages != null && response.data.totalPages > 0
                    ? next + 1 >= response.data.totalPages : Boolean.TRUE.equals(response.data.last);
            adapter.submit(data); state(false, false);
        }, failure -> { loading = false; state(false, true); error(failure.message); });
    }
    private void state(boolean loading, boolean failed) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.tvEmptyState.setText(failed ? R.string.social_retry : R.string.chat_empty_conversations);
        binding.tvEmptyState.setVisibility(!loading && (failed || adapter.getItemCount() == 0) ? View.VISIBLE : View.GONE);
    }
}
