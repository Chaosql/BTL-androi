package com.example.myapplication.ui.admin;

import android.os.Bundle;
import android.view.*;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.*;
import com.example.myapplication.R;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.api.ApiService;
import com.example.myapplication.data.remote.dto.response.AdminUserDto;
import com.example.myapplication.data.remote.network.RetrofitClient;
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
    private boolean busy, last;
    private int page;
    private AlertDialog dialog;
    public AdminUsersFragment() { super(FragmentAdminUsersBinding::inflate); }
    private ApiService api() { return RetrofitClient.getApiService(requireContext()); }
    @Override protected void onScreenCreated(View root, Bundle state) {
        preferences = new PreferenceManager(requireContext()); users.clear(); busy = false; last = false; page = 0;
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(requireContext())); binding.rvUsers.setAdapter(adapter);
        binding.btnReload.setOnClickListener(view -> { if (!busy) { page = 0; last = false; load(); } });
        binding.tvStatus.setOnClickListener(view -> load()); binding.btnMore.setOnClickListener(view -> load());
        binding.btnLogout.setOnClickListener(view -> logout()); load();
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
    private void load() {
        if (busy || last || !allowed()) return;
        busy(true); binding.tvStatus.setText(R.string.feature_loading);
        request(api().getAdminUsers(page, 20), response -> {
            busy(false);
            if (response.data == null || response.data.content == null) { error(getString(R.string.feature_invalid)); return; }
            if (page == 0) users.clear();
            int before = users.size();
            for (AdminUserDto user : response.data.content) {
                if (user == null || user.id == null || user.id.isEmpty()) continue;
                users.removeIf(old -> user.id.equals(old.id)); users.add(user);
            }
            last = Boolean.TRUE.equals(response.data.last) || response.data.content.isEmpty() || users.size() == before;
            page++; adapter.notifyDataSetChanged(); binding.tvStatus.setText(users.isEmpty() ? getString(R.string.admin_empty) : "");
            binding.btnMore.setVisibility(last ? View.GONE : View.VISIBLE);
        }, failure -> { busy(false); error(failure.message); });
    }
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
                        for (int i = 0; i < users.size(); i++) if (user.id.equals(users.get(i).id)) users.set(i, response.data);
                        adapter.notifyDataSetChanged(); binding.tvStatus.setText("");
                    }, failure -> { busy(false); error(failure.message); });
                }).show();
    }
    private void logout() {
        if (busy) return; busy(true);
        MainActivity host = (MainActivity) requireActivity();
        com.example.myapplication.ui.notification.PushRegistration.endSession(requireContext(), () -> {
            if (!host.isDestroyed()) host.showRootScreen(UiScreen.LOGIN);
        });
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
