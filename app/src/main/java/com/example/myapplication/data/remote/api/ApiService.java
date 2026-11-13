package com.example.myapplication.data.remote.api;

import com.example.myapplication.data.remote.dto.request.*;
import com.example.myapplication.data.remote.dto.response.*;
import java.util.List;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @GET("api/v1/user")
    Call<BaseResponse<PageResponse<AdminUserDto>>> getAdminUsers(@Query("pageNum") int page, @Query("pageSize") int size);
    @GET("api/v1/user/{id}")
    Call<BaseResponse<AdminUserDto>> getAdminUser(@Path("id") String id);
    @PATCH("api/v1/user/{id}/lock")
    Call<BaseResponse<Object>> lockAdminUser(@Path("id") String id, @Body AccountStatusRequest request);
    @PATCH("api/v1/user/{id}/unlock")
    Call<BaseResponse<Object>> unlockAdminUser(@Path("id") String id, @Body AccountStatusRequest request);
    @POST("api/v1/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);


    @POST("api/v1/auth/register")
    Call<BaseResponse<CommonResponse>> register(@Body RegisterRequest request);

    @POST("api/v1/auth/verify-register-otp")
    Call<BaseResponse<CommonResponse>> verifyRegisterOtp(@Body VerifyOtpRequest request);

    @POST("api/v1/auth/send-otp")
    Call<BaseResponse<CommonResponse>> sendOtp(@Body SendOtpRequest request);

    @POST("api/v1/auth/forgot-password/send-otp")
    Call<BaseResponse<CommonResponse>> forgotPasswordSendOtp(@Body SendOtpRequest request);

    @POST("api/v1/auth/forgot-password/verify-otp")
    Call<BaseResponse<CommonResponse>> forgotPasswordVerifyOtp(@Body VerifyOtpRequest request);

    @POST("api/v1/auth/forgot-password/reset")
    Call<BaseResponse<CommonResponse>> resetPassword(@Body ResetPasswordRequest request);

    @POST("api/v1/auth/refresh")
    Call<LoginResponse> refreshToken(@Body RefreshTokenRequest request);

    @POST("api/v1/auth/logout")
    Call<BaseResponse<Object>> logout(@Body LogoutRequest request);

    @GET("api/v1/user/current")
    Call<BaseResponse<UserDto>> getCurrentUser();

    @GET("api/v1/profile/{userId}")
    Call<BaseResponse<UserDto>> getProfile(@Path("userId") String userId);

    @PUT("api/v1/profile/{userId}/status-tag")
    Call<BaseResponse<UserDto>> updateStatusTag(@Path("userId") String userId, @Body UpdateStatusTagRequest request);

    @PUT("api/v1/profile/{userId}/personal-info")
    Call<BaseResponse<UserDto>> updatePersonalInfo(@Path("userId") String userId, @Body UpdatePersonalInfoRequest request);

    @PUT("api/v1/profile/{userId}/academic-info")
    Call<BaseResponse<UserDto>> updateAcademicInfo(@Path("userId") String userId, @Body UpdateAcademicInfoRequest request);

    @PUT("api/v1/profile/{userId}/location")
    Call<BaseResponse<UserDto>> updateProfileLocation(@Path("userId") String userId, @Body UpdateProfileLocationRequest request);

    @Multipart
    @POST("api/v1/profile/{userId}/avatar")
    Call<BaseResponse<UserDto>> uploadAvatar(@Path("userId") String userId, @Part MultipartBody.Part file);

    @PATCH("api/v1/profile/{userId}/status")
    Call<BaseResponse<UserDto>> toggleBuddyStatus(@Path("userId") String userId, @Body ToggleBuddyStatusRequest request);

    @PUT("api/v1/profile/{userId}/password")
    Call<BaseResponse<CommonResponse>> changePassword(@Path("userId") String userId, @Body ChangePasswordRequest request);

    @GET("api/v1/profile/{userId}/subjects")
    Call<BaseResponse<List<SubjectDto>>> getSubjects(@Path("userId") String userId, @Query("type") String type);

    @POST("api/v1/profile/{userId}/subjects")
    Call<BaseResponse<SubjectDto>> addSubject(@Path("userId") String userId, @Body AddSubjectRequest request);

    @DELETE("api/v1/profile/{userId}/subjects/{subjectId}")
    Call<BaseResponse<CommonResponse>> deleteSubject(@Path("userId") String userId, @Path("subjectId") String subjectId);

    @DELETE("api/v1/profile/{userId}")
    Call<BaseResponse<CommonResponse>> deleteAccount(@Path("userId") String userId);

    @PUT("api/v1/location/update")
    Call<BaseResponse<CommonResponse>> updateLocation(@Body UpdateLocationRequest request);

    @DELETE("api/v1/location/remove")
    Call<BaseResponse<CommonResponse>> removeLocation();

    @GET("api/v1/location/radar")
    Call<BaseResponse<List<NearbyUserResponse>>> getNearbyUsers(@Query("lat") Double lat, @Query("lng") Double lng, @Query("radius") Double radius);

    @GET("api/v1/chat/conversations")
    Call<BaseResponse<PageResponse<ConversationResponse>>> getConversations(@Query("page") Integer page, @Query("size") Integer size, @Query("sort") String sort);

    @GET("api/v1/chat/conversations/search")
    Call<BaseResponse<PageResponse<ConversationResponse>>> searchConversations(@Query("keyword") String keyword, @Query("page") Integer page, @Query("size") Integer size, @Query("sort") String sort);

    @POST("api/v1/chat/conversations/direct")
    Call<BaseResponse<ConversationResponse>> createOrGetDirectConversation(@Body DirectChatRequest request);

    @GET("api/v1/chat/conversations/{id}/messages")
    Call<BaseResponse<PageResponse<MessageResponse>>> getMessages(@Path("id") String id, @Query("page") Integer page, @Query("size") Integer size, @Query("sort") String sort);


    @PUT("api/v1/chat/messages/{msgId}/recall")
    Call<BaseResponse<Object>> recallMessage(@Path("msgId") String msgId);

    @DELETE("api/v1/chat/messages/{msgId}")
    Call<BaseResponse<Object>> deleteMessage(@Path("msgId") String msgId);





    @Multipart
    @POST("api/v1/chat/conversations/{id}/upload")
    Call<BaseResponse<Object>> uploadImage(@Path("id") String conversationId, @Part MultipartBody.Part file);

    @POST("api/v1/chat/conversations/{id}/schedule-invite")
    Call<BaseResponse<Object>> scheduleInvite(@Path("id") String conversationId, @Body ScheduleInviteRequest request);

    @GET("api/v1/users/search")
    Call<BaseResponse<List<UserSearchDto>>> searchUsers(@Query("keyword") String keyword);

    @POST("api/v1/friends/request/{targetUserId}")
    Call<BaseResponse<FriendActionResponse>> sendFriendRequest(@Path("targetUserId") String targetUserId);

    @PUT("api/v1/friends/request/{requestId}/accept")
    Call<BaseResponse<FriendActionResponse>> acceptFriendRequest(@Path("requestId") String requestId);

    @PUT("api/v1/friends/request/{requestId}/reject")
    Call<BaseResponse<FriendActionResponse>> rejectFriendRequest(@Path("requestId") String requestId);

    @GET("api/v1/friends")
    Call<BaseResponse<PageResponse<FriendDto>>> getFriends(@Query("page") Integer page, @Query("size") Integer size, @Query("sort") String sort);

    @GET("api/v1/friends/search")
    Call<BaseResponse<PageResponse<FriendDto>>> searchFriends(@Query("keyword") String keyword, @Query("page") Integer page, @Query("size") Integer size, @Query("sort") String sort);

    @GET("api/v1/friends/requests/pending")
    Call<BaseResponse<PageResponse<FriendDto>>> getPendingFriendRequests(@Query("page") Integer page, @Query("size") Integer size, @Query("sort") String sort);

    @DELETE("api/v1/friends/{friendId}")
    Call<BaseResponse<FriendActionResponse>> unfriend(@Path("friendId") String friendId);

    @POST("api/v1/bookings/conversation/{conversationId}")
    Call<BaseResponse<BookingDto>> createBookingInConversation(@Path("conversationId") String conversationId, @Body CreateBookingRequest request);

    @GET("api/v1/bookings")
    Call<BaseResponse<List<BookingDto>>> getMyBookings();

    @GET("api/v1/bookings/weekly")
    Call<BaseResponse<WeeklyBookingResponse>> getWeeklyBookings(@Query("weekStart") String weekStart);

    @GET("api/v1/bookings/{bookingId}")
    Call<BaseResponse<BookingDto>> getBookingDetail(@Path("bookingId") String bookingId);

    @PUT("api/v1/bookings/{bookingId}/accept")
    Call<BaseResponse<BookingDto>> acceptBooking(@Path("bookingId") String bookingId);

    @PUT("api/v1/bookings/{bookingId}/reject")
    Call<BaseResponse<BookingDto>> rejectBooking(@Path("bookingId") String bookingId);

    @PATCH("api/v1/bookings/{bookingId}/complete")
    Call<BaseResponse<BookingDto>> completeBooking(@Path("bookingId") String bookingId);

    @PATCH("api/v1/bookings/{bookingId}/cancel")
    Call<BaseResponse<BookingDto>> cancelBooking(@Path("bookingId") String bookingId, @Body CancelBookingRequest request);

    @POST("api/v1/bookings/{bookingId}/rate")
    Call<BaseResponse<ActionStatusDto>> rateBooking(@Path("bookingId") String bookingId, @Body RateBookingRequest request);

    @GET("api/v1/notifications")
    Call<BaseResponse<PageResponse<NotificationDto>>> getNotifications(@Query("page") Integer page, @Query("size") Integer size, @Query("sort") String sort);

    @GET("api/v1/notifications/unread-count")
    Call<BaseResponse<Integer>> getUnreadNotificationCount();

    @PATCH("api/v1/notifications/{notificationId}/read")
    Call<BaseResponse<ActionStatusDto>> markNotificationAsRead(@Path("notificationId") String notificationId);

    @PATCH("api/v1/notifications/read-all")
    Call<BaseResponse<ActionStatusDto>> markAllNotificationsAsRead();

    @POST("api/v1/devices")
    Call<BaseResponse<ActionStatusDto>> registerDevice(@Body RegisterDeviceRequest request);

    @DELETE("api/v1/devices")
    Call<BaseResponse<ActionStatusDto>> unregisterDevice(@Query("fcmToken") String fcmToken);

    @DELETE("api/v1/notifications/{notificationId}")
    Call<BaseResponse<ActionStatusDto>> deleteNotification(@Path("notificationId") String notificationId);

    @HTTP(method = "DELETE", path = "api/v1/notifications/batch", hasBody = true)
    Call<BaseResponse<ActionStatusDto>> deleteNotificationsBatch(@Body DeleteNotificationsBatchRequest request);

    @DELETE("api/v1/notifications/all")
    Call<BaseResponse<ActionStatusDto>> deleteAllNotifications();

    @GET("api/v1/user/{userId}/online-status")
    Call<BaseResponse<OnlineStatusDto>> getUserOnlineStatus(@Path("userId") String userId);

    @POST("api/v1/users/block/{userId}")
    Call<BaseResponse<Object>> blockUser(@Path("userId") String userId);

    @DELETE("api/v1/users/block/{userId}")
    Call<BaseResponse<Object>> unblockUser(@Path("userId") String userId);

    @GET("api/v1/users/blocked")
    Call<BaseResponse<List<BlockedUserDto>>> getBlockedUsers();
}
