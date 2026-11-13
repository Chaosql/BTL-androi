package com.example.myapplication.ui.admin;

import android.os.Bundle;
import android.view.*;
import androidx.recyclerview.widget.*;
import com.example.myapplication.R;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.response.AdminUserDto;
import com.example.myapplication.databinding.*;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.friends.SocialUi;
import com.example.myapplication.ui.main.*;
import java.util.*;

public class AdminUsersFragment extends BaseFragment<FragmentAdminUsersBinding> {
    private final List<AdminUserDto> users = new ArrayList<>();
    private final UsersAdapter adapter = new UsersAdapter();
    private PreferenceManager preferences;
    private boolean busy;
    private com.example.myapplication.ui.base.PagedList<AdminUserDto> pages;
    public AdminUsersFragment() { super(FragmentAdminUsersBinding::inflate); }
    private AdminViewModel api() { return viewModel(AdminViewModel.class); }
    @Override protected void onScreenCreated(View root, Bundle state) {
        com.example.myapplication.ui.auth.SessionViewModel session = viewModel(com.example.myapplication.ui.auth.SessionViewModel.class);
        session.signedOut().observe(getViewLifecycleOwner(), event -> {
            if (event.consume() != null) ((com.example.myapplication.ui.main.MainActivity) requireActivity()).showRootScreen(UiScreen.LOGIN);
        });
        preferences = new PreferenceManager(requireContext()); users.clear(); busy = false; pages = api().users();

        binding.rvUsers.setAdapter(adapter);
        binding.btnReload.setOnClickListener(view -> { if (!busy && allowed()) pages.load("", true); });
        binding.tvStatus.setOnClickListener(view -> load()); binding.btnMore.setOnClickListener(view -> load());
        binding.btnLogout.setOnClickListener(view -> logout());
        pages.state().observe(getViewLifecycleOwner(), result -> {
            users.clear(); users.addAll(result.items); adapter.notifyDataSetChanged();
            busy(result.loading);
            if (!allowed()) return;
            binding.btnMore.setVisibility(result.last ? View.GONE : View.VISIBLE);
            if (result.error != null) error(result.error.message);
            else binding.tvStatus.setText(result.loading ? getString(R.string.feature_loading) : users.isEmpty() ? getString(R.string.admin_empty) : "");
        });
        if (allowed()) pages.initialize("");
    }
    private boolean allowed() {
        if (preferences.isAdmin()) return true;
        binding.tvStatus.setText(R.string.admin_denied); binding.btnMore.setVisibility(View.GONE); return false;
    }
    private void busy(boolean value) {
        busy = value; binding.btnMore.setEnabled(!value); binding.btnReload.setEnabled(!value); binding.btnLogout.setEnabled(!value);
    }
    private void error(String message) {
        binding.tvStatus.setText(getString(R.string.feature_retry, message == null ? getString(R.string.feature_invalid) : message));
    }
    private void load() { if (!busy && allowed()) pages.load("", false); }
    private void detail(String id) {
        if (busy || !allowed()) return;
        busy(true); binding.tvStatus.setText(R.string.feature_loading);
        request(api().getAdminUser(id), response -> {
            busy(false); binding.tvStatus.setText(""); AdminUserDto user = response.data;
            if (user == null || !id.equals(user.id)) { error(getString(R.string.feature_invalid)); return; }
            new AdminUserDetailDialog((MainActivity) requireActivity(), user, changed -> {
                if (binding != null && allowed()) pages.replace(changed);
            }).show(getViewLifecycleOwner());
        }, failure -> { busy(false); error(failure.message); });
    }
    private void logout() {
        if (busy) return; busy(true);
        viewModel(com.example.myapplication.ui.auth.SessionViewModel.class).signOut();
    }
    private final class UsersAdapter extends RecyclerView.Adapter<UserHolder> {
        @Override public int getItemCount() { return users.size(); }
        @Override public UserHolder onCreateViewHolder(ViewGroup parent, int type) {
            return new UserHolder(ItemAdminUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
        @Override public void onBindViewHolder(UserHolder holder, int position) {
            AdminUserDto user = users.get(position);
            holder.row.tvName.setText(SocialUi.name(user.firstName, user.lastName)); holder.row.tvEmail.setText(user.email);
            holder.row.tvState.setText(AdminUserDetailDialog.statusLabel(user)); holder.row.getRoot().setOnClickListener(view -> detail(user.id));
        }
    }
    private static final class UserHolder extends RecyclerView.ViewHolder {
        final ItemAdminUserBinding row;
        UserHolder(ItemAdminUserBinding row) { super(row.getRoot()); this.row = row; }
    }
}
