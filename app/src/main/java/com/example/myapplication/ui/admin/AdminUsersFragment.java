package com.example.myapplication.ui.admin;

import android.os.Bundle;
import android.view.*;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.*;
import com.example.myapplication.R;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.response.AdminUserDto;
import com.example.myapplication.databinding.*;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.friends.SocialUi;
import com.example.myapplication.ui.main.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.*;

public class AdminUsersFragment extends BaseFragment<FragmentAdminUsersBinding> {
    private final List<AdminUserDto> users = new ArrayList<>();
    private final UsersAdapter adapter = new UsersAdapter();
    private PreferenceManager preferences;
    private boolean busy;
    private com.example.myapplication.ui.base.PagedList<AdminUserDto> pages;
    private AlertDialog dialog;
    public AdminUsersFragment() { super(FragmentAdminUsersBinding::inflate); }
    private AdminViewModel api() { return viewModel(AdminViewModel.class); }
    @Override protected void onScreenCreated(View root, Bundle state) {
        enableScreenCatalog(binding.tvScreenTitle);
        com.example.myapplication.ui.auth.SessionViewModel session = viewModel(com.example.myapplication.ui.auth.SessionViewModel.class);
        session.signedOut().observe(getViewLifecycleOwner(), event -> {
            if (event.consume() != null) ((com.example.myapplication.ui.main.MainActivity) requireActivity()).showRootScreen(UiScreen.LOGIN);
        });
        preferences = new PreferenceManager(requireContext()); users.clear(); busy = false; pages = api().users();
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(requireContext())); binding.rvUsers.setAdapter(adapter);
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
    private int status(AdminUserDto user) {
        switch (SocialUi.value(user.status).toUpperCase(Locale.ROOT)) {
            case "ACTIVE": return R.string.admin_active;
            case "LOCKED": return R.string.admin_locked;
            case "UNVERIFIED": return R.string.admin_unverified;
            default: return R.string.admin_unknown;
        }
    }
    private void detail(String id) {
        if (busy || !allowed()) return;
        busy(true); binding.tvStatus.setText(R.string.feature_loading);
        request(api().getAdminUser(id), response -> {
            busy(false); binding.tvStatus.setText(""); AdminUserDto user = response.data;
            if (user == null || !id.equals(user.id)) { error(getString(R.string.feature_invalid)); return; }
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(SocialUi.name(user.firstName, user.lastName))
                    .setMessage(getString(R.string.admin_detail, SocialUi.value(user.email), SocialUi.value(user.phone),
                            SocialUi.value(user.university), SocialUi.value(user.majorName), SocialUi.value(user.role), getString(status(user))))
                    .setNegativeButton(R.string.btn_close, null);
            boolean locked = "LOCKED".equals(user.status);
            if (!id.equals(preferences.getUserId()) && (locked || "ACTIVE".equals(user.status)))
                builder.setPositiveButton(locked ? R.string.admin_unlock : R.string.admin_lock, (d, which) -> confirm(user, locked));
            dialog = builder.show();
        }, failure -> { busy(false); error(failure.message); });
    }
    private void confirm(AdminUserDto user, boolean unlock) {
        if (busy || !allowed() || user.id.equals(preferences.getUserId())) return;
        dialog = new MaterialAlertDialogBuilder(requireContext()).setMessage(getString(R.string.admin_confirm,
                getString(unlock ? R.string.admin_unlock : R.string.admin_lock), SocialUi.name(user.firstName, user.lastName)))
                .setNegativeButton(android.R.string.cancel, null).setPositiveButton(android.R.string.ok, (d, which) -> {
                    if (busy || !allowed()) return;
                    busy(true); binding.tvStatus.setText(R.string.feature_loading);
                    request(unlock ? api().unlockAdminUser(user.id) : api().lockAdminUser(user.id), response -> {
                        busy(false);
                        if (response.data == null || !user.id.equals(response.data.id)
                                || !(unlock ? "ACTIVE" : "LOCKED").equals(response.data.status)) {
                            error(getString(R.string.feature_invalid)); return;
                        }
                        adapter.notifyDataSetChanged(); binding.tvStatus.setText("");
                    }, failure -> { busy(false); error(failure.message); });
                }).show();
    }
    private void logout() {
        if (busy) return; busy(true);
        viewModel(com.example.myapplication.ui.auth.SessionViewModel.class).signOut();
    }
    @Override public void onDestroyView() { if (dialog != null) { dialog.dismiss(); dialog = null; } super.onDestroyView(); }
    private final class UsersAdapter extends RecyclerView.Adapter<UserHolder> {
        @Override public int getItemCount() { return users.size(); }
        @Override public UserHolder onCreateViewHolder(ViewGroup parent, int type) {
            return new UserHolder(ItemAdminUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
        @Override public void onBindViewHolder(UserHolder holder, int position) {
            AdminUserDto user = users.get(position);
            holder.row.tvName.setText(SocialUi.name(user.firstName, user.lastName)); holder.row.tvEmail.setText(user.email);
            holder.row.tvState.setText(status(user)); holder.row.getRoot().setOnClickListener(view -> detail(user.id));
        }
    }
    private static final class UserHolder extends RecyclerView.ViewHolder {
        final ItemAdminUserBinding row;
        UserHolder(ItemAdminUserBinding row) { super(row.getRoot()); this.row = row; }
    }
}
