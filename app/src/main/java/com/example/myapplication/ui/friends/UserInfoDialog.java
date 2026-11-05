package com.example.myapplication.ui.friends;

import com.example.myapplication.R;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.ui.base.BaseFragment;

public class UserInfoDialog extends BasePreviewDialog {
    public UserInfoDialog(MainActivity activity) { super(activity, R.layout.dialog_user_info); }

    @Override protected void bind(View content, AlertDialog dialog) {

        BaseFragment.text(content, R.id.tvUserName, "Minh Anh");
        BaseFragment.text(content, R.id.tvDistance, activity.getString(R.string.ui_uidialogs_text_27));
        BaseFragment.text(content, R.id.tvOnlineStatus, activity.getString(R.string.ui_studyscreenfragment_text_21));
        BaseFragment.text(content, R.id.tvStatusTag, activity.getString(R.string.ui_uidialogs_text_29));
        BaseFragment.text(content, R.id.tvSchool, activity.getString(R.string.ui_uidialogs_text_30));
        BaseFragment.text(content, R.id.tvMajor, activity.getString(R.string.ui_uidialogs_text_31));
    }
}
