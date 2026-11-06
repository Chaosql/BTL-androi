package com.example.myapplication.ui.notification;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.example.myapplication.R;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.local.NotificationPreferences;
import com.example.myapplication.data.remote.dto.response.NotificationDto;
import com.example.myapplication.data.repository.notification.NotificationRepository;
import com.example.myapplication.databinding.FragmentNotificationBinding;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.chat.ChatUi;
import com.example.myapplication.ui.friends.UserInfoDialog;
import com.example.myapplication.ui.main.MainActivity;
import com.example.myapplication.ui.main.UiScreen;
import com.example.myapplication.ui.schedule.BookingUi;
import java.time.*;
import java.util.*;

public class NotificationFragment extends BaseFragment<FragmentNotificationBinding> {
    private final Map<String, NotificationDto> items = new LinkedHashMap<>();
    private NotificationRepository repository;
    private NotificationAdapter recent, earlier;
    private boolean loading, last, marking;
    private int nextPage, unread;
    private int countVersion;
    private NotificationPreferences preferences;
    private androidx.appcompat.app.AlertDialog readAllDialog;
    public NotificationFragment() { super(FragmentNotificationBinding::inflate); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        repository = new NotificationRepository(requireContext());
        preferences = new NotificationPreferences(requireContext());
        items.clear(); loading = false; last = false; marking = false; nextPage = 0; unread = 0;
        recent = new NotificationAdapter(this::select, this::delete); earlier = new NotificationAdapter(this::select, this::delete);
        binding.rvRecentNotifications.setAdapter(recent); binding.rvEarlierNotifications.setAdapter(earlier);
        binding.tvNotificationStatus.setOnClickListener(view -> load());
        binding.tvNotificationMore.setOnClickListener(view -> load());
        binding.tvMarkAllRead.setOnClickListener(view -> markAll());
        binding.btnNotificationMenu.setOnClickListener(view -> {
            android.widget.PopupMenu menu = new android.widget.PopupMenu(requireContext(), view);
            menu.getMenu().add(0, R.string.notification_settings, 0, R.string.notification_settings).setOnMenuItemClickListener(item -> { go(UiScreen.NOTIFICATION_SETTINGS); return true; });
            menu.getMenu().add(0, R.string.notification_delete_all, 1, R.string.notification_delete_all).setOnMenuItemClickListener(item -> { delete(null); return true; });
            menu.show();
        });
        title();
        if (!new PreferenceManager(requireContext()).isLogin()) {
            binding.tvNotificationStatus.setText(R.string.notification_login); return;
        }
        load(); count();
    }
    private void count() {
        int version = ++countVersion;
        request(repository.getUnreadNotificationCount(), response -> {
            if (version != countVersion) return;
            if (response.data != null) unread = Math.max(0, response.data);
            render();
        }, error -> { });
    }
    private void title() {
        binding.tvScreenTitle.setText(unread > 0 ? getString(R.string.notification_title_count, unread)
                : getString(R.string.title_notification));
        binding.tvMarkAllRead.setEnabled(!marking && !loading && unread > 0);
    }
    private void load() {
        if (loading || marking || last || !new PreferenceManager(requireContext()).isLogin()) return;
        loading = true;
        title();
        binding.tvNotificationStatus.setText(R.string.notification_loading);
        binding.tvNotificationStatus.setVisibility(View.VISIBLE);
        binding.tvNotificationMore.setVisibility(View.GONE);
        request(repository.getNotifications(nextPage, 20, "createdDate,desc"), response -> {
            loading = false;
            if (response.data == null || response.data.content == null) { failed(getString(R.string.notification_invalid)); return; }
            int before = items.size();
            for (NotificationDto item : response.data.content) {
                if (item == null || ChatUi.value(item.id).trim().isEmpty()) continue;
                NotificationDto old = items.get(item.id);
                if (old != null && Boolean.TRUE.equals(old.isRead)) item.isRead = true;
                items.put(item.id, item);
            }
            last = Boolean.TRUE.equals(response.data.last) || response.data.content.isEmpty() || items.size() == before;
            nextPage++; binding.tvNotificationStatus.setVisibility(View.GONE); render();
        }, error -> { loading = false; failed(error.message); });
    }
    private void failed(String message) {
        title();
        binding.tvNotificationStatus.setText(getString(R.string.notification_retry, message));
        binding.tvNotificationStatus.setVisibility(View.VISIBLE);
    }
    private void render() {
        List<NotificationDto> sorted = new ArrayList<>(items.values());
        sorted.sort((a, b) -> ChatUi.instant(b.createdDate).compareTo(ChatUi.instant(a.createdDate)));
        List<NotificationDto> now = new ArrayList<>(), old = new ArrayList<>();
        Instant start = LocalDate.now(BookingUi.ZONE).minusDays(6).atStartOfDay(BookingUi.ZONE).toInstant();
        int loadedUnread = 0;
        for (NotificationDto item : sorted) {
            if (!Boolean.TRUE.equals(item.isRead)) loadedUnread++;
            if (preferences.allows(item)) (ChatUi.instant(item.createdDate).isBefore(start) ? old : now).add(item);
        }
        unread = Math.max(unread, loadedUnread);
        recent.submit(now); earlier.submit(old);
        binding.tvRecentHeader.setVisibility(now.isEmpty() ? View.GONE : View.VISIBLE);
        binding.tvEarlierHeader.setVisibility(old.isEmpty() ? View.GONE : View.VISIBLE);
        ((View) binding.rvRecentNotifications.getParent()).setVisibility(now.isEmpty() ? View.GONE : View.VISIBLE);
        ((View) binding.rvEarlierNotifications.getParent()).setVisibility(old.isEmpty() ? View.GONE : View.VISIBLE);
        boolean visible = !now.isEmpty() || !old.isEmpty();
        binding.tvNotificationEmpty.setText(!items.isEmpty() && preferences.hasFilter()
                ? R.string.notification_filtered_empty : R.string.empty_notification);
        binding.layoutEmptyState.setVisibility(!visible && !loading ? View.VISIBLE : View.GONE);
        binding.scrollViewContent.setVisibility(visible ? View.VISIBLE : View.GONE);
        binding.tvNotificationMore.setVisibility(!last && !loading ? View.VISIBLE : View.GONE);
        title();
    }
    private void select(NotificationDto item) {
        if (marking || loading) return;
        if (Boolean.TRUE.equals(item.isRead)) { open(item); return; }
        marking = true; countVersion++; title();
        request(repository.markNotificationAsRead(item.id), response -> {
            marking = false;
            NotificationDto current = items.get(item.id);
            if (current != null) current.isRead = true;
            item.isRead = true; unread = Math.max(0, unread - 1); render(); open(item);
        }, error -> { marking = false; title(); showError(error.message); });
    }
    private void markAll() {
        if (marking || loading || unread <= 0) return;
        if (preferences.hasFilter()) {
            if (readAllDialog != null && readAllDialog.isShowing()) return;
            readAllDialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setMessage(R.string.notification_mark_hidden)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> markAllConfirmed())
                    .setNegativeButton(android.R.string.cancel, null).show();
        } else markAllConfirmed();
    }
    private void markAllConfirmed() {
        if (marking || loading || unread <= 0) return;
        marking = true; countVersion++; title();
        request(repository.markAllNotificationsAsRead(), response -> {
            marking = false; for (NotificationDto item : items.values()) item.isRead = true;
            unread = 0; render();
        }, error -> { marking = false; title(); showError(error.message); });
    }
    private void showError(String message) {
        Toast.makeText(requireContext(), message == null || message.trim().isEmpty()
                ? getString(R.string.notification_action_error) : message, Toast.LENGTH_LONG).show();
    }
    private void delete(NotificationDto item) {
        if (loading || marking || !new PreferenceManager(requireContext()).isLogin()) return;
        if (readAllDialog != null && readAllDialog.isShowing()) return;
        readAllDialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setMessage(item == null ? R.string.notification_delete_all_confirm : R.string.notification_delete_confirm)
                .setNegativeButton(android.R.string.cancel, null).setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    if (marking || loading) return;
                    marking = true; countVersion++; title();
                    request(item == null ? repository.deleteAllNotifications() : repository.deleteNotification(item.id), response -> {
                        marking = false;
                        if (item == null) { items.clear(); unread = 0; last = true; }
                        else { items.remove(item.id); if (!Boolean.TRUE.equals(item.isRead)) unread = Math.max(0, unread - 1); }
                        render();
                    }, error -> { marking = false; title(); showError(error.message); });
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
        if (readAllDialog != null) { readAllDialog.dismiss(); readAllDialog = null; }
        super.onDestroyView();
    }
}
