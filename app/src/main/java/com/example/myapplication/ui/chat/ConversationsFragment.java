package com.example.myapplication.ui.chat;

import android.os.Bundle;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myapplication.R;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.response.ConversationResponse;
import com.example.myapplication.ui.chat.ConversationViewModel;
import com.example.myapplication.databinding.FragmentFriendsListBinding;
import com.example.myapplication.ui.chat.adapter.ConversationsAdapter;
import com.example.myapplication.ui.friends.BaseSocialFragment;
import com.example.myapplication.ui.main.UiScreen;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.*;

public class ConversationsFragment extends BaseSocialFragment<FragmentFriendsListBinding> {
    private ConversationsAdapter adapter;
    private com.example.myapplication.ui.base.PagedList<ConversationResponse> pages;
    public ConversationsFragment() { super(FragmentFriendsListBinding::inflate); }
    @Override protected void onScreenCreated(View root, Bundle saved) {
        enableScreenCatalog(binding.tvHeaderTitle);
        binding.btnBack.setOnClickListener(view -> back());
        binding.progressBar.setVisibility(View.GONE);
        pages = viewModel(ConversationViewModel.class).conversations();
        binding.tvHeaderTitle.setText(R.string.chat_conversations);
        adapter = new ConversationsAdapter(new PreferenceManager(requireContext()).getUserId(), item -> {
            Bundle args = ChatUi.args(item.id, ChatUi.conversationName(item, new PreferenceManager(requireContext()).getUserId()), "");
            args.putBoolean("is_disabled", Boolean.TRUE.equals(item.hasLeft) || Integer.valueOf(1).equals(item.memberCount));
            go(UiScreen.CHAT, args);
        });
        binding.rvFriendsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFriendsList.setAdapter(adapter);
        pages.state().observe(getViewLifecycleOwner(), state -> {
            adapter.submit(state.items);
            state(state.loading, state.error != null);
            if (state.error != null) error(state.error.message);
        });

        search(binding.etSearchFriends, () -> load(true));
        pageScroll(binding.rvFriendsList, () -> load(false));
        binding.tvEmptyState.setOnClickListener(view -> load(true));
        getParentFragmentManager().setFragmentResultListener(ChatUi.UPDATED, getViewLifecycleOwner(), (key, value) -> load(true));
        binding.btnHeaderMenu.setOnClickListener(view -> new MaterialAlertDialogBuilder(requireContext())
                .setItems(new CharSequence[]{getString(R.string.title_friends), getString(R.string.ui_fragment_search_user_text_1),
                        getString(R.string.ui_fragment_pending_requests_text_1)}, (dialog, which) ->
                        go(which == 0 ? UiScreen.FRIENDS : which == 1 ? UiScreen.SEARCH_USER : UiScreen.PENDING_REQUESTS)).show());
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
        binding.tvEmptyState.setText(failed ? R.string.social_retry : R.string.chat_empty_conversations);
        binding.tvEmptyState.setVisibility(!loading && (failed || adapter.getItemCount() == 0) ? View.VISIBLE : View.GONE);
    }
}
