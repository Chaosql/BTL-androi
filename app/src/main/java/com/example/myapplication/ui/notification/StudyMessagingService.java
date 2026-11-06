package com.example.myapplication.ui.notification;

import com.google.firebase.messaging.*;
import com.example.myapplication.R;
import com.example.myapplication.data.local.*;
import com.example.myapplication.data.remote.dto.response.NotificationDto;
import com.example.myapplication.data.remote.dto.response.*;
import com.example.myapplication.data.remote.network.*;
import com.example.myapplication.data.repository.schedule.BookingRepository;
import com.example.myapplication.ui.schedule.BookingReminders;
import java.util.Map;

public final class StudyMessagingService extends FirebaseMessagingService {
    @Override public void onNewToken(String token) { PushRegistration.token(this, token); }
    @Override public void onMessageReceived(RemoteMessage message) {
        PreferenceManager session = new PreferenceManager(this);
        if (!session.isLogin()) return;
        String owner = session.getUserId(); Map<String, String> data = message.getData();
        String recipient = data.get("recipientId");
        if (recipient != null && !recipient.equals(owner)) return;
        NotificationDto item = new NotificationDto(); item.type = data.get("type"); item.targetType = data.get("targetType");
        if (!new NotificationPreferences(this).allows(item)) return;
        String type = item.targetType == null ? item.type : item.targetType;
        boolean appointment = type != null && (type.equalsIgnoreCase("BOOKING") || type.equalsIgnoreCase("APPOINTMENT") || type.startsWith("BOOKING_"));
        String booking = appointment ? data.get("targetId") : null;
        if (appointment && (booking == null || booking.isEmpty())) booking = data.get("bookingId");
        if (booking != null && !booking.isEmpty()) {
            String id = booking;
            ApiRequests.enqueue(new BookingRepository(this).getBookingDetail(id), new ApiCallback<BaseResponse<BookingDto>>() {
                @Override public void onSuccess(BaseResponse<BookingDto> response) {
                    if (owner.equals(new PreferenceManager(StudyMessagingService.this).getUserId()) && response.data != null && id.equals(response.data.id))
                        BookingReminders.sync(StudyMessagingService.this, response.data);
                }
                @Override public void onError(ApiError error) { }
            });
        }
        RemoteMessage.Notification notification = message.getNotification();
        String title = data.get("title"), body = data.get("body");
        if (title == null && notification != null) title = notification.getTitle();
        if (body == null && notification != null) body = notification.getBody();
        BookingReminders.show(this, owner, booking, title == null ? getString(R.string.title_notification) : title, body == null ? "" : body);
    }
}
