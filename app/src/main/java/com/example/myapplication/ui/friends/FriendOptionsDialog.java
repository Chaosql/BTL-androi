package com.example.myapplication.ui.friends;

import com.example.myapplication.R;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import android.view.View;
import androidx.appcompat.app.AlertDialog;

public class FriendOptionsDialog extends BasePreviewDialog {
    public FriendOptionsDialog(MainActivity activity) { super(activity, R.layout.dialog_friend_options); }

    @Override protected void bind(View content, AlertDialog dialog) {

    }
}
