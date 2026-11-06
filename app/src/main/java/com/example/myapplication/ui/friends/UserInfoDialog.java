package com.example.myapplication.ui.friends;

import com.example.myapplication.databinding.DialogUserInfoBinding;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.ui.base.BaseFragment;

public class UserInfoDialog extends BasePreviewDialog<DialogUserInfoBinding> {
    public UserInfoDialog(MainActivity activity) { super(activity, DialogUserInfoBinding::inflate); }

    @Override protected void bind(View content, AlertDialog dialog) {

        binding.tvUserName.setText("Minh Anh");
        binding.tvDistance.setText(activity.getString(R.string.ui_uidialogs_text_27));
        binding.tvOnlineStatus.setText(activity.getString(R.string.ui_studyscreenfragment_text_21));
        binding.tvStatusTag.setText(activity.getString(R.string.ui_uidialogs_text_29));
        binding.tvSchool.setText(activity.getString(R.string.ui_uidialogs_text_30));
        binding.tvMajor.setText(activity.getString(R.string.ui_uidialogs_text_31));
    }
}
