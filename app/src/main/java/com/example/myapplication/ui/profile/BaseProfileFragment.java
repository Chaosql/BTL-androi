package com.example.myapplication.ui.profile;

import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.main.UiScreen;

public abstract class BaseProfileFragment extends BaseFragment {
    protected BaseProfileFragment(int layout) { super(layout); }

    protected void bindProfile(View root, boolean editingProfile) {
        text(root, R.id.tvUsername, "Minh An");
        text(root, R.id.tvBuddyStatus, getString(R.string.ui_studyscreenfragment_text_13));
        text(root, R.id.tvIntroduction, getString(R.string.ui_studyscreenfragment_text_14));
        text(root, R.id.tvAge, getString(R.string.ui_studyscreenfragment_text_15));
        text(root, R.id.tvGender, getString(R.string.ui_studyscreenfragment_text_16));
        text(root, R.id.tvSchool, getString(R.string.ui_studyscreenfragment_text_17));
        text(root, R.id.tvMajor, getString(R.string.ui_studyscreenfragment_text_18));
        text(root, R.id.tvLocation, getString(R.string.ui_studyscreenfragment_text_19));
        text(root, R.id.tvFriendsCount, getString(R.string.ui_studyscreenfragment_text_20));
        text(root, R.id.tvFavoriteRating, "100.0");
        text(root, R.id.tvAppointmentCount, "8");
        text(root, R.id.tvSubjectCount, "3");
        on(root, R.id.btnUpdateProfile, () -> {
            if (!editingProfile) go(UiScreen.EDIT_PROFILE);
            else previewNotice();
        });
        on(root, R.id.cvSettings, () -> go(UiScreen.ACCOUNT_SETTINGS));
        on(root, R.id.cvAppointment, () -> go(UiScreen.SCHEDULE));
        on(root, R.id.cvHonor, () -> dialog(R.layout.dialog_subject_management));
        on(root, R.id.tvAddInfo, () -> dialog(R.layout.dialog_edit_personal_info));
        on(root, R.id.tvIntroduction, () -> dialog(R.layout.dialog_edit_introduction));
        on(root, R.id.imgEditAvatar, this::previewNotice);
        on(root, R.id.cvInfo, () -> dialog(R.layout.dialog_edit_personal_info));
    }
}
