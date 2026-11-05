package com.example.myapplication.ui.notification;

import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.base.BasePreviewAdapter;
import com.example.myapplication.ui.base.PreviewRow;
import com.example.myapplication.ui.main.UiScreen;

public class NotificationAdapter extends BasePreviewAdapter {
    public NotificationAdapter(BaseFragment owner, boolean recent) {
        super(owner,
                new PreviewRow(R.layout.item_notification).with(R.id.tvContent, recent ? owner.getString(R.string.ui_uirows_text_18) : owner.getString(R.string.ui_uirows_text_19))
                        .with(R.id.tvTimeAgo, recent ? owner.getString(R.string.ui_uirows_text_20) : owner.getString(R.string.ui_uirows_text_9)).click(() -> owner.go(UiScreen.SCHEDULE)),
                new PreviewRow(R.layout.item_notification).with(R.id.tvContent, recent ? owner.getString(R.string.ui_uirows_text_22) : owner.getString(R.string.ui_uirows_text_23))
                        .with(R.id.tvTimeAgo, recent ? owner.getString(R.string.ui_uirows_text_24) : owner.getString(R.string.ui_uirows_text_25)).click(() -> owner.go(UiScreen.PENDING_REQUESTS)));
    }
}
