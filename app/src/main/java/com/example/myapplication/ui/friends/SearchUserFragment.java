package com.example.myapplication.ui.friends;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myapplication.R;
import com.example.myapplication.data.remote.dto.response.UserSearchDto;
import com.example.myapplication.databinding.FragmentSearchUserBinding;
import com.example.myapplication.ui.friends.adapter.UserSearchAdapter;
import java.util.ArrayList;
import java.util.List;

public class SearchUserFragment extends BaseSocialFragment<FragmentSearchUserBinding> {
    private UserSearchAdapter adapter;
    public SearchUserFragment() { super(FragmentSearchUserBinding::inflate); }
    @Override protected void onScreenCreated(View root, Bundle state) {
        enableScreenCatalog(binding.tvHeaderTitle);
        binding.btnBack.setOnClickListener(view -> back());
        binding.progressBar.setVisibility(View.GONE);
        adapter = new UserSearchAdapter(item -> user(item.id, item.friendshipStatus), this::add);
        binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSearchResults.setAdapter(adapter);
        binding.btnSearch.setOnClickListener(view -> load());
        binding.edtKeyword.setOnEditorActionListener((view, action, event) -> {
            if (action == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) { load(); return true; }
            return false;
        });
        search(binding.edtKeyword, this::load);
        changed(this::load);
        binding.tvEmptyState.setOnClickListener(view -> load());
    }

    private void load() {
        cancelSearchDelay();
        String keyword = binding.edtKeyword.getText().toString().trim();
        invalidateQuery();
        if (keyword.isEmpty() || !signedIn()) {
            adapter.submit(new ArrayList<>());
            binding.progressBar.setVisibility(View.GONE);
            binding.tvEmptyState.setVisibility(View.GONE);
            return;
        }
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvEmptyState.setVisibility(View.GONE);
        query(friends().searchUsers(keyword), response -> {
            binding.progressBar.setVisibility(View.GONE);
            if (response.data == null) { failure(getString(R.string.social_empty_response)); return; }
            List<UserSearchDto> users = response.data;
            adapter.submit(users);
            binding.tvEmptyState.setText(R.string.social_no_results);
            binding.tvEmptyState.setVisibility(users.isEmpty() ? View.VISIBLE : View.GONE);
        }, failure -> failure(failure.message));
    }

    private void failure(String message) {
        binding.progressBar.setVisibility(View.GONE);
        binding.tvEmptyState.setText(R.string.social_retry);
        binding.tvEmptyState.setVisibility(View.VISIBLE);
        error(message);
    }

    private void add(UserSearchDto item) {
        if (SocialUi.friend(item.friendshipStatus)) { openChat(item.id, SocialUi.name(item.firstName, item.lastName)); return; }
        if (adapter.isBusy(item.id) || friends().sent(item.id)) return;
        adapter.busy(item.id, true);
        request(friends().sendFriendRequest(item.id), response -> {
            item.friendshipStatus = "PENDING";
            friends().rememberSent(item.id);
            adapter.busy(item.id, false);
            load();
            Toast.makeText(requireContext(), R.string.social_request_sent, Toast.LENGTH_SHORT).show();
        }, failure -> { adapter.busy(item.id, false); error(failure.message); });
    }
}
