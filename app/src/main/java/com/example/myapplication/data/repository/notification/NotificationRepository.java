package com.example.myapplication.data.repository.notification;

import android.content.Context;
import com.example.myapplication.data.repository.BaseRepository;
import com.example.myapplication.data.remote.api.ApiService;
import com.example.myapplication.data.remote.dto.request.*;
import com.example.myapplication.data.remote.dto.response.*;
import java.util.List;
import okhttp3.MultipartBody;
import retrofit2.Call;

public final class NotificationRepository extends BaseRepository {
    public NotificationRepository(Context context) { super(context); }
    public NotificationRepository(ApiService api) { super(api); }

    public Call<BaseResponse<PageResponse<NotificationDto>>> getNotifications(Integer page, Integer size, String sort) {
        return api.getNotifications(page, size, sort);
    }

    public Call<BaseResponse<Integer>> getUnreadNotificationCount() {
        return api.getUnreadNotificationCount();
    }

    public Call<BaseResponse<ActionStatusDto>> markNotificationAsRead(String notificationId) {
        return api.markNotificationAsRead(notificationId);
    }

    public Call<BaseResponse<ActionStatusDto>> markAllNotificationsAsRead() {
        return api.markAllNotificationsAsRead();
    }

    public Call<BaseResponse<ActionStatusDto>> registerDevice(RegisterDeviceRequest request) {
        return api.registerDevice(request);
    }

    public Call<BaseResponse<ActionStatusDto>> unregisterDevice(String fcmToken) {
        return api.unregisterDevice(fcmToken);
    }

    public Call<BaseResponse<ActionStatusDto>> deleteNotification(String notificationId) {
        return api.deleteNotification(notificationId);
    }

    public Call<BaseResponse<ActionStatusDto>> deleteNotificationsBatch(DeleteNotificationsBatchRequest request) {
        return api.deleteNotificationsBatch(request);
    }

    public Call<BaseResponse<ActionStatusDto>> deleteAllNotifications() {
        return api.deleteAllNotifications();
    }
}
