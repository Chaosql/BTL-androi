package com.example.myapplication.ui.chat;

import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.response.ConversationResponse;
import com.example.myapplication.ui.chat.ConversationViewModel;
import com.example.myapplication.databinding.FragmentFriendsListBinding;
import com.example.myapplication.ui.chat.adapter.ConversationsAdapter;
import com.example.myapplication.ui.friends.BaseSocialFragment;
import com.example.myapplication.ui.main.UiScreen;
import java.util.*;

public class ConversationsFragment extends BaseSocialFragment<FragmentFriendsListBinding> {
    private android.widget.PopupWindow menuPopup;
    private ConversationsAdapter adapter;
    private com.example.myapplication.ui.base.PagedList<ConversationResponse> pages;
    public ConversationsFragment() { super(FragmentFriendsListBinding::inflate); }
    @Override protected void onScreenCreated(View root, Bundle saved) {
        binding.btnBack.setOnClickListener(view -> back());
        binding.progressBar.setVisibility(View.GONE);
        pages = viewModel(ConversationViewModel.class).conversations();
        binding.tvHeaderTitle.setText(R.string.chat_conversations);
        adapter = new ConversationsAdapter(new PreferenceManager(requireContext()).getUserId(), item -> {
            Bundle args = ChatUi.args(item.id, ChatUi.conversationName(item, new PreferenceManager(requireContext()).getUserId()), "");
            args.putBoolean("is_disabled", Boolean.TRUE.equals(item.hasLeft) || Integer.valueOf(1).equals(item.memberCount));
            go(UiScreen.CHAT, args);
        });

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
        binding.btnHeaderMenu.setOnClickListener(view -> {
            if (menuPopup != null && menuPopup.isShowing()) { menuPopup.dismiss(); return; }
            com.example.myapplication.databinding.LayoutConversationsMenuPopupBinding menu =
                    com.example.myapplication.databinding.LayoutConversationsMenuPopupBinding.inflate(getLayoutInflater());
            menu.actionConversationsFriends.setOnClickListener(item -> selectMenu(UiScreen.FRIENDS));
            menu.actionConversationsSearch.setOnClickListener(item -> selectMenu(UiScreen.SEARCH_USER));
            menu.actionConversationsRequests.setOnClickListener(item -> selectMenu(UiScreen.PENDING_REQUESTS));
            menuPopup = com.example.myapplication.ui.main.UiDialogs.showAnchoredPopup(host(), view, menu.getRoot());
        });
        if (signedIn()) pages.initialize(binding.etSearchFriends.getText().toString().trim());
        else state(false, false);
    }
    private void selectMenu(UiScreen screen) {
        if (menuPopup != null) menuPopup.dismiss();
        go(screen);
    }
    @Override public void onDestroyView() {
        if (menuPopup != null) { menuPopup.dismiss(); menuPopup = null; }
        super.onDestroyView();
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
