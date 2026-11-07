package com.example.myapplication.ui.notification;

import android.app.Application;
import com.example.myapplication.ui.base.BaseViewModel;
import com.example.myapplication.ui.base.UiOperation;
import com.example.myapplication.data.repository.notification.NotificationRepository;
import com.example.myapplication.data.remote.dto.request.*;
import com.example.myapplication.data.remote.dto.response.*;
import java.util.List;
import okhttp3.MultipartBody;

public final class NotificationViewModel extends BaseViewModel {
    private final NotificationRepository repository;

    public NotificationViewModel(Application application) {
        super(application);
        repository = new NotificationRepository(application);
        publishInbox();
    }

    public UiOperation<BaseResponse<PageResponse<NotificationDto>>> getNotifications(Integer page, Integer size, String sort) {
        return execute(repository.getNotifications(page, size, sort));
    }

    public UiOperation<BaseResponse<Integer>> getUnreadNotificationCount() {
        return execute(repository.getUnreadNotificationCount());
    }

    public UiOperation<BaseResponse<ActionStatusDto>> markNotificationAsRead(String notificationId) {
        return execute(repository.markNotificationAsRead(notificationId));
    }

    public UiOperation<BaseResponse<ActionStatusDto>> markAllNotificationsAsRead() {
        return execute(repository.markAllNotificationsAsRead());
    }

    public UiOperation<BaseResponse<ActionStatusDto>> registerDevice(RegisterDeviceRequest request) {
        return execute(repository.registerDevice(request));
    }

    public UiOperation<BaseResponse<ActionStatusDto>> unregisterDevice(String fcmToken) {
        return execute(repository.unregisterDevice(fcmToken));
    }

    public UiOperation<BaseResponse<ActionStatusDto>> deleteNotification(String notificationId) {
        return execute(repository.deleteNotification(notificationId));
    }

    public UiOperation<BaseResponse<ActionStatusDto>> deleteNotificationsBatch(DeleteNotificationsBatchRequest request) {
        return execute(repository.deleteNotificationsBatch(request));
    }

    public UiOperation<BaseResponse<ActionStatusDto>> deleteAllNotifications() {
        return execute(repository.deleteAllNotifications());
    }
    public static final class Inbox {
        public final List<NotificationDto> recent, earlier;
        public final boolean loading, mutating, last, filtered, hasItems;
        public final int unread;
        public final String error;
        Inbox(List<NotificationDto> recent, List<NotificationDto> earlier, boolean loading, boolean mutating,
                boolean last, boolean filtered, boolean hasItems, int unread, String error) {
            this.recent = java.util.Collections.unmodifiableList(recent); this.earlier = java.util.Collections.unmodifiableList(earlier);
            this.loading = loading; this.mutating = mutating; this.last = last; this.filtered = filtered;
            this.hasItems = hasItems; this.unread = unread; this.error = error;
        }
        public boolean busy() { return loading || mutating; }
    }
    private final java.util.Map<String, NotificationDto> items = new java.util.LinkedHashMap<>();
    private final androidx.lifecycle.MutableLiveData<Inbox> inbox = new androidx.lifecycle.MutableLiveData<>();
    private boolean inboxLoading, mutating, inboxLast, initialized;
    private int nextPage, unread, countVersion;
    private String inboxError;
    public androidx.lifecycle.LiveData<Inbox> inbox() { return inbox; }
    public Inbox snapshot() { return inbox.getValue(); }
    public void initializeInbox() {
        if (initialized) { publishInbox(); return; }
        initialized = true; loadInbox(); refreshCount();
    }
    private boolean signedIn() { return new com.example.myapplication.data.local.PreferenceManager(getApplication()).isLogin(); }
    public void refreshCount() {
        if (!signedIn()) return;
        int version = ++countVersion;
        watch(getUnreadNotificationCount(), response -> {
            if (version != countVersion) return;
            if (response.data != null) unread = Math.max(0, response.data); publishInbox();
        }, error -> { });
    }
    public void loadInbox() {
        if (inboxLoading || mutating || inboxLast || !signedIn()) return;
        inboxLoading = true; inboxError = null; publishInbox();
        watch(getNotifications(nextPage, 20, "createdDate,desc"), response -> {
            inboxLoading = false;
            if (response.data == null || response.data.content == null) {
                inboxError = getApplication().getString(com.example.myapplication.R.string.notification_invalid); publishInbox(); return;
            }
            int before = items.size();
            for (NotificationDto item : response.data.content) {
                if (item == null || item.id == null || item.id.trim().isEmpty()) continue;
                NotificationDto old = items.get(item.id);
                if (old != null && Boolean.TRUE.equals(old.isRead)) item.isRead = true;
                items.put(item.id, item);
            }
            inboxLast = Boolean.TRUE.equals(response.data.last) || response.data.content.isEmpty() || items.size() == before;
            nextPage++; publishInbox();
        }, error -> { inboxLoading = false; inboxError = error.message; publishInbox(); });
    }
    private void publishInbox() {
        java.util.List<NotificationDto> sorted = new java.util.ArrayList<>(items.values());
        sorted.sort((a, b) -> com.example.myapplication.ui.chat.ChatUi.instant(b.createdDate).compareTo(com.example.myapplication.ui.chat.ChatUi.instant(a.createdDate)));
        java.util.List<NotificationDto> now = new java.util.ArrayList<>(), old = new java.util.ArrayList<>();
        java.time.Instant start = java.time.LocalDate.now(com.example.myapplication.ui.schedule.BookingUi.ZONE).minusDays(6)
                .atStartOfDay(com.example.myapplication.ui.schedule.BookingUi.ZONE).toInstant();
        com.example.myapplication.data.local.NotificationPreferences preferences = new com.example.myapplication.data.local.NotificationPreferences(getApplication());
        int loadedUnread = 0;
        for (NotificationDto item : sorted) {
            if (!Boolean.TRUE.equals(item.isRead)) loadedUnread++;
            if (preferences.allows(item)) (com.example.myapplication.ui.chat.ChatUi.instant(item.createdDate).isBefore(start) ? old : now).add(item);
        }
        unread = Math.max(unread, loadedUnread);
        inbox.setValue(new Inbox(now, old, inboxLoading, mutating, inboxLast, preferences.hasFilter(), !items.isEmpty(), unread, inboxError));
    }
    private UiOperation<BaseResponse<ActionStatusDto>> mutate(UiOperation<BaseResponse<ActionStatusDto>> operation, Runnable success) {
        mutating = true; countVersion++; publishInbox();
        watch(operation, response -> { mutating = false; success.run(); publishInbox(); }, error -> { mutating = false; publishInbox(); });
        return operation;
    }
    public UiOperation<BaseResponse<ActionStatusDto>> markInboxRead(NotificationDto item) {
        return mutate(markNotificationAsRead(item.id), () -> {
            NotificationDto current = items.get(item.id); if (current != null) current.isRead = true;
            item.isRead = true; unread = Math.max(0, unread - 1);
        });
    }
    public UiOperation<BaseResponse<ActionStatusDto>> markAllInboxRead() {
        return mutate(markAllNotificationsAsRead(), () -> {
            for (NotificationDto item : items.values()) item.isRead = true; unread = 0;
        });
    }
    public UiOperation<BaseResponse<ActionStatusDto>> deleteInbox(NotificationDto item) {
        return mutate(item == null ? deleteAllNotifications() : deleteNotification(item.id), () -> {
            if (item == null) { items.clear(); unread = 0; inboxLast = true; }
            else { items.remove(item.id); if (!Boolean.TRUE.equals(item.isRead)) unread = Math.max(0, unread - 1); }
        });
    }
}
