package com.example.myapplication.ui.notification;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.example.myapplication.R;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.response.NotificationDto;
import com.example.myapplication.databinding.FragmentNotificationBinding;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.chat.ChatUi;
import com.example.myapplication.ui.friends.UserInfoDialog;
import com.example.myapplication.ui.main.MainActivity;
import com.example.myapplication.ui.main.UiScreen;
import com.example.myapplication.ui.schedule.BookingUi;
import java.util.Locale;

public class NotificationFragment extends BaseFragment<FragmentNotificationBinding> {
    private android.widget.PopupWindow notificationMenu;
    private NotificationViewModel model;
    private NotificationAdapter recent, earlier;
    private androidx.appcompat.app.AlertDialog readAllDialog;
    public NotificationFragment() { super(FragmentNotificationBinding::inflate); }
    @Override protected void onScreenCreated(View root, Bundle state) {
        enableScreenCatalog(binding.tvScreenTitle);
        binding.ivBack.setOnClickListener(view -> back());
        model = viewModel(NotificationViewModel.class);
        recent = new NotificationAdapter(this::select, this::delete);
        earlier = new NotificationAdapter(this::select, this::delete);
        binding.rvRecentNotifications.setAdapter(recent); binding.rvEarlierNotifications.setAdapter(earlier);
        binding.tvNotificationStatus.setOnClickListener(view -> model.loadInbox());
        binding.tvNotificationMore.setOnClickListener(view -> model.loadInbox());
        binding.tvMarkAllRead.setOnClickListener(view -> markAll());
        binding.btnNotificationMenu.setOnClickListener(view -> {
            if (notificationMenu != null && notificationMenu.isShowing()) { notificationMenu.dismiss(); return; }
            com.example.myapplication.databinding.LayoutNotificationMenuPopupBinding menu =
                    com.example.myapplication.databinding.LayoutNotificationMenuPopupBinding.inflate(getLayoutInflater());
            menu.actionNotificationSettings.setOnClickListener(item -> {
                notificationMenu.dismiss(); go(UiScreen.NOTIFICATION_SETTINGS);
            });
            menu.actionNotificationDeleteAll.setOnClickListener(item -> {
                notificationMenu.dismiss(); delete(null);
            });
            notificationMenu = com.example.myapplication.ui.main.UiDialogs.showAnchoredPopup(
                    (MainActivity) requireActivity(), view, menu.getRoot());
        });
        model.inbox().observe(getViewLifecycleOwner(), this::render);
        model.initializeInbox();
    }
    private void render(NotificationViewModel.Inbox state) {
        binding.tvScreenTitle.setText(state.unread > 0 ? getString(R.string.notification_title_count, state.unread) : getString(R.string.title_notification));
        binding.tvMarkAllRead.setEnabled(!state.busy() && state.unread > 0);
        recent.submit(state.recent); earlier.submit(state.earlier);
        binding.tvRecentHeader.setVisibility(state.recent.isEmpty() ? View.GONE : View.VISIBLE);
        binding.tvEarlierHeader.setVisibility(state.earlier.isEmpty() ? View.GONE : View.VISIBLE);
        ((View) binding.rvRecentNotifications.getParent()).setVisibility(state.recent.isEmpty() ? View.GONE : View.VISIBLE);
        ((View) binding.rvEarlierNotifications.getParent()).setVisibility(state.earlier.isEmpty() ? View.GONE : View.VISIBLE);
        boolean visible = !state.recent.isEmpty() || !state.earlier.isEmpty();
        binding.tvNotificationEmpty.setText(state.hasItems && state.filtered ? R.string.notification_filtered_empty : R.string.empty_notification);
        binding.layoutEmptyState.setVisibility(!visible && !state.loading ? View.VISIBLE : View.GONE);
        binding.scrollViewContent.setVisibility(visible ? View.VISIBLE : View.GONE);
        binding.tvNotificationMore.setVisibility(!state.last && !state.loading ? View.VISIBLE : View.GONE);
        if (!new PreferenceManager(requireContext()).isLogin()) {
            binding.tvNotificationStatus.setText(R.string.notification_login);
            binding.tvNotificationStatus.setVisibility(View.VISIBLE);
        } else if (state.loading) {
            binding.tvNotificationStatus.setText(R.string.notification_loading); binding.tvNotificationStatus.setVisibility(View.VISIBLE);
        } else if (state.error != null) {
            binding.tvNotificationStatus.setText(getString(R.string.notification_retry, state.error)); binding.tvNotificationStatus.setVisibility(View.VISIBLE);
        } else binding.tvNotificationStatus.setVisibility(View.GONE);
    }
    private void select(NotificationDto item) {
        if (model.snapshot().busy()) return;
        if (Boolean.TRUE.equals(item.isRead)) { open(item); return; }
        request(model.markInboxRead(item), response -> open(item), error -> showError(error.message));
    }
    private void markAll() {
        if (model.snapshot().busy() || model.snapshot().unread <= 0) return;
        if (model.snapshot().filtered) {
            if (readAllDialog != null && readAllDialog.isShowing()) return;
            readAllDialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setMessage(R.string.notification_mark_hidden)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> markAllConfirmed())
                    .setNegativeButton(android.R.string.cancel, null).show();
        } else markAllConfirmed();
    }
    private void markAllConfirmed() {
        if (model.snapshot().busy() || model.snapshot().unread <= 0) return;
        request(model.markAllInboxRead(), response -> { }, error -> showError(error.message));
    }
    private void showError(String message) {
        Toast.makeText(requireContext(), message == null || message.trim().isEmpty()
                ? getString(R.string.notification_action_error) : message, Toast.LENGTH_LONG).show();
    }
    private void delete(NotificationDto item) {
        if (model.snapshot().busy() || !new PreferenceManager(requireContext()).isLogin()) return;
        if (readAllDialog != null && readAllDialog.isShowing()) return;
        readAllDialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setMessage(item == null ? R.string.notification_delete_all_confirm : R.string.notification_delete_confirm)
                .setNegativeButton(android.R.string.cancel, null).setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    if (model.snapshot().busy()) return;
                    request(model.deleteInbox(item), response -> { }, error -> showError(error.message));
                }).show();
    }
    private void open(NotificationDto item) {
        String type = ChatUi.value(item.targetType).trim();
        if (type.isEmpty()) type = ChatUi.value(item.type);
        String id = ChatUi.value(item.targetId).trim();
        switch (type.toUpperCase(Locale.ROOT)) {
            case "FRIENDSHIP":
                go(ChatUi.value(item.type).contains("ACCEPTED") ? UiScreen.FRIENDS : UiScreen.PENDING_REQUESTS); return;
            case "BOOKING": case "APPOINTMENT":
                if (!id.isEmpty()) { Bundle args = new Bundle(); args.putString(BookingUi.BOOKING_ID, id); go(UiScreen.SCHEDULE, args); return; }
                break;
            case "CONVERSATION":
                if (!id.isEmpty()) { go(UiScreen.CHAT, ChatUi.args(id, "", null)); return; }
                break;
            case "CHAT": case "MESSAGE": go(UiScreen.CONVERSATIONS); return;
            case "FRIEND_REQUEST": go(UiScreen.PENDING_REQUESTS); return;
            case "FRIEND": case "USER":
                if (id.isEmpty()) id = ChatUi.value(item.actorId);
                if (!id.isEmpty()) { new UserInfoDialog((MainActivity) requireActivity(), id, null).show(getViewLifecycleOwner()); return; }
                break;
            case "SCHEDULE": case "TIMETABLE": go(UiScreen.SCHEDULE); return;
            default: break;
        }
        Toast.makeText(requireContext(), R.string.notification_no_target, Toast.LENGTH_SHORT).show();
    }
    @Override public void onDestroyView() {
        if (notificationMenu != null) { notificationMenu.dismiss(); notificationMenu = null; }
        if (readAllDialog != null) { readAllDialog.dismiss(); readAllDialog = null; }
        super.onDestroyView();
    }
}
