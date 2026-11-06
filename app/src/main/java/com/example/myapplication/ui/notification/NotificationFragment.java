package com.example.myapplication.ui.notification;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.example.myapplication.R;
import com.example.myapplication.data.local.PreferenceManager;
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
    public NotificationFragment() { super(FragmentNotificationBinding::inflate); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        repository = new NotificationRepository(requireContext());
        items.clear(); loading = false; last = false; marking = false; nextPage = 0; unread = 0;
        recent = new NotificationAdapter(this::select); earlier = new NotificationAdapter(this::select);
        binding.rvRecentNotifications.setAdapter(recent); binding.rvEarlierNotifications.setAdapter(earlier);
        binding.tvNotificationStatus.setOnClickListener(view -> load());
        binding.tvNotificationMore.setOnClickListener(view -> load());
        binding.tvMarkAllRead.setOnClickListener(view -> markAll());
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
            (ChatUi.instant(item.createdDate).isBefore(start) ? old : now).add(item);
            if (!Boolean.TRUE.equals(item.isRead)) loadedUnread++;
        }
        unread = Math.max(unread, loadedUnread);
        recent.submit(now); earlier.submit(old);
        binding.tvRecentHeader.setVisibility(now.isEmpty() ? View.GONE : View.VISIBLE);
        binding.tvEarlierHeader.setVisibility(old.isEmpty() ? View.GONE : View.VISIBLE);
        binding.layoutEmptyState.setVisibility(items.isEmpty() && !loading ? View.VISIBLE : View.GONE);
        binding.scrollViewContent.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
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
    private void open(NotificationDto item) {
        String type = ChatUi.value(item.targetType).trim();
        if (type.isEmpty()) type = ChatUi.value(item.type);
        String id = ChatUi.value(item.targetId).trim();
        switch (type.toUpperCase(Locale.ROOT)) {
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
}
