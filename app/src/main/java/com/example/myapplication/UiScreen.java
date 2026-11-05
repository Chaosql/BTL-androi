package com.example.myapplication;

public enum UiScreen {
    LOGIN("Đăng nhập", R.layout.fragment_login),
    REGISTER("Đăng ký", R.layout.fragment_register),
    VERIFY_EMAIL("Xác thực email", R.layout.fragment_verify_email),
    INPUT_EMAIL("Nhập email khôi phục", R.layout.fragment_input_email),
    VERIFY_OTP("Nhập mã OTP", R.layout.fragment_verify_otp),
    FORGOT_PASSWORD("Đặt lại mật khẩu", R.layout.fragment_forgot_password),
    MAP("Bản đồ / Radar", R.layout.fragment_map),
    PROFILE("Hồ sơ cá nhân", R.layout.fragment_profile),
    EDIT_PROFILE("Chỉnh sửa hồ sơ", R.layout.fragment_edit_profile),
    ACCOUNT_SETTINGS("Cài đặt tài khoản", R.layout.fragment_account_settings),
    REGISTRATION_INFO("Thông tin đăng ký", R.layout.fragment_registration_info),
    CHANGE_PASSWORD("Đổi mật khẩu", R.layout.fragment_change_password),
    NOTIFICATION_SETTINGS("Cài đặt thông báo", R.layout.fragment_notification_settings),
    BLOCKED_USERS("Người dùng đã chặn", R.layout.fragment_blocked_users),
    FRIENDS("Bạn bè / Hội thoại", R.layout.fragment_friends_list),
    SEARCH_USER("Tìm người dùng", R.layout.fragment_search_user),
    PENDING_REQUESTS("Lời mời kết bạn", R.layout.fragment_pending_requests),
    CHAT("Trò chuyện", R.layout.fragment_chat),
    GROUP_DETAIL("Chi tiết nhóm", R.layout.fragment_friends_list),
    NOTIFICATIONS("Thông báo", R.layout.fragment_notification),
    SCHEDULE("Lịch hẹn", R.layout.fragment_schedule),
    CREATE_APPOINTMENT("Tạo lịch hẹn", R.layout.fragment_create_appointment);

    public final String title;
    public final int layout;
    UiScreen(String title, int layout) { this.title = title; this.layout = layout; }
}
