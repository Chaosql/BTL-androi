package com.example.myapplication.ui.main;

import com.example.myapplication.R;
public enum UiScreen {
    LOGIN(R.string.login, R.layout.fragment_login),
    REGISTER(R.string.ui_uiscreen_text_2, R.layout.fragment_register),
    VERIFY_EMAIL(R.string.ui_uiscreen_text_3, R.layout.fragment_verify_email),
    INPUT_EMAIL(R.string.ui_uiscreen_text_4, R.layout.fragment_input_email),
    VERIFY_OTP(R.string.ui_uiscreen_text_5, R.layout.fragment_verify_otp),
    FORGOT_PASSWORD(R.string.ui_uiscreen_text_6, R.layout.fragment_forgot_password),
    MAP(R.string.ui_uiscreen_text_7, R.layout.fragment_map),
    PROFILE(R.string.profile_title, R.layout.fragment_profile),
    EDIT_PROFILE(R.string.ui_uiscreen_text_9, R.layout.fragment_edit_profile),
    ACCOUNT_SETTINGS(R.string.setting, R.layout.fragment_account_settings),
    REGISTRATION_INFO(R.string.ui_fragment_registration_info_text_1, R.layout.fragment_registration_info),
    CHANGE_PASSWORD(R.string.change_password, R.layout.fragment_change_password),
    NOTIFICATION_SETTINGS(R.string.notification_settings, R.layout.fragment_notification_settings),
    BLOCKED_USERS(R.string.ui_uiscreen_text_14, R.layout.fragment_blocked_users),
    FRIENDS(R.string.ui_uiscreen_text_15, R.layout.fragment_friends_list),
    SEARCH_USER(R.string.ui_uiscreen_text_16, R.layout.fragment_search_user),
    PENDING_REQUESTS(R.string.ui_fragment_pending_requests_text_1, R.layout.fragment_pending_requests),
    CHAT(R.string.ui_uiscreen_text_18, R.layout.fragment_chat),
    CONVERSATIONS(R.string.chat_conversations, R.layout.fragment_friends_list),
    GROUP_DETAIL(R.string.ui_uiscreen_text_19, R.layout.fragment_friends_list),
    NOTIFICATIONS(R.string.title_notification, R.layout.fragment_notification),
    SCHEDULE(R.string.ui_uiscreen_text_21, R.layout.fragment_schedule),
    CREATE_APPOINTMENT(R.string.ui_uiscreen_text_22, R.layout.fragment_create_appointment);

    public final int title;
    public final int layout;
    UiScreen(int title, int layout) { this.title = title; this.layout = layout; }
}
