package com.example.myapplication.ui.main;

import android.os.Bundle;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.auth.LoginFragment;
import com.example.myapplication.ui.auth.RegisterFragment;
import com.example.myapplication.ui.auth.VerifyEmailFragment;
import com.example.myapplication.ui.auth.InputEmailFragment;
import com.example.myapplication.ui.auth.VerifyOtpFragment;
import com.example.myapplication.ui.auth.ForgotPasswordFragment;
import com.example.myapplication.ui.map.MapFragment;
import com.example.myapplication.ui.profile.ProfileFragment;
import com.example.myapplication.ui.profile.EditProfileFragment;
import com.example.myapplication.ui.profile.AccountSettingsFragment;
import com.example.myapplication.ui.profile.RegistrationInfoFragment;
import com.example.myapplication.ui.profile.ChangePasswordFragment;
import com.example.myapplication.ui.notification.NotificationSettingsFragment;
import com.example.myapplication.ui.friends.BlockedUsersFragment;
import com.example.myapplication.ui.friends.FriendsListFragment;
import com.example.myapplication.ui.friends.SearchUserFragment;
import com.example.myapplication.ui.friends.PendingRequestsFragment;
import com.example.myapplication.ui.chat.ChatFragment;
import com.example.myapplication.ui.chat.group.GroupDetailFragment;
import com.example.myapplication.ui.notification.NotificationFragment;
import com.example.myapplication.ui.schedule.ScheduleFragment;
import com.example.myapplication.ui.schedule.CreateAppointmentFragment;

public final class ScreenFactory {
    public static BaseFragment create(UiScreen screen) { return create(screen, null); }

    public static BaseFragment create(UiScreen screen, Bundle arguments) {
        BaseFragment fragment;
        switch (screen) {
            case LOGIN: fragment = new LoginFragment(); break;
            case REGISTER: fragment = new RegisterFragment(); break;
            case VERIFY_EMAIL: fragment = new VerifyEmailFragment(); break;
            case INPUT_EMAIL: fragment = new InputEmailFragment(); break;
            case VERIFY_OTP: fragment = new VerifyOtpFragment(); break;
            case FORGOT_PASSWORD: fragment = new ForgotPasswordFragment(); break;
            case MAP: fragment = new MapFragment(); break;
            case PROFILE: fragment = new ProfileFragment(); break;
            case EDIT_PROFILE: fragment = new EditProfileFragment(); break;
            case ACCOUNT_SETTINGS: fragment = new AccountSettingsFragment(); break;
            case REGISTRATION_INFO: fragment = new RegistrationInfoFragment(); break;
            case CHANGE_PASSWORD: fragment = new ChangePasswordFragment(); break;
            case NOTIFICATION_SETTINGS: fragment = new NotificationSettingsFragment(); break;
            case BLOCKED_USERS: fragment = new BlockedUsersFragment(); break;
            case FRIENDS: fragment = new FriendsListFragment(); break;
            case SEARCH_USER: fragment = new SearchUserFragment(); break;
            case PENDING_REQUESTS: fragment = new PendingRequestsFragment(); break;
            case CHAT: fragment = new ChatFragment(); break;
            case CONVERSATIONS: fragment = new com.example.myapplication.ui.chat.ConversationsFragment(); break;
            case GROUP_DETAIL: fragment = new GroupDetailFragment(); break;
            case NOTIFICATIONS: fragment = new NotificationFragment(); break;
            case SCHEDULE: fragment = new ScheduleFragment(); break;
            case CREATE_APPOINTMENT: fragment = new CreateAppointmentFragment(); break;
            default: throw new IllegalArgumentException(screen.name());
        }
        fragment.setArguments(arguments == null ? new Bundle() : new Bundle(arguments));
        return fragment;
    }

    private ScreenFactory() { }
}
