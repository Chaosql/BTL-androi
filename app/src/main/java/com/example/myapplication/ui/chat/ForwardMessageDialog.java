package com.example.myapplication.ui.chat;

import com.example.myapplication.R;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import android.widget.Toast;
import com.example.myapplication.ui.base.BasePreviewAdapter;
import com.example.myapplication.ui.base.PreviewRow;
import com.example.myapplication.ui.base.BaseFragment;

public class ForwardMessageDialog extends BasePreviewDialog {
    public ForwardMessageDialog(MainActivity activity) { super(activity, R.layout.dialog_forward_select); }

    @Override protected void bind(View content, AlertDialog dialog) {

        androidx.fragment.app.Fragment current = activity.getSupportFragmentManager().findFragmentById(R.id.screenHost);
        if (current instanceof BaseFragment) {
            Runnable select = () -> {
                Toast.makeText(activity, activity.getString(R.string.ui_uidialogs_text_39), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            };
            BasePreviewAdapter conversations = new BasePreviewAdapter((BaseFragment) current,
                    new PreviewRow(R.layout.item_friend).with(R.id.tvFriendName, "Minh Anh")
                            .with(R.id.tvLastMessage, activity.getString(R.string.ui_uidialogs_text_40)).click(select),
                    new PreviewRow(R.layout.item_friend).with(R.id.tvFriendName, activity.getString(R.string.ui_studyscreenfragment_text_2))
                            .with(R.id.tvLastMessage, activity.getString(R.string.ui_uidialogs_text_42)).click(select));
            BaseFragment.list(content, R.id.rvConversations, conversations);
        }
    }
}
