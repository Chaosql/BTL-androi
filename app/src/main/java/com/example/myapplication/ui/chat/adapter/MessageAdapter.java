package com.example.myapplication.ui.chat.adapter;

import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.base.BasePreviewAdapter;
import com.example.myapplication.ui.base.PreviewRow;
import android.view.View;
import com.example.myapplication.ui.main.UiDialogs;
import com.example.myapplication.ui.main.MainActivity;

public class MessageAdapter extends BasePreviewAdapter {
    public MessageAdapter(BaseFragment owner) {
        super(owner,
                new PreviewRow(R.layout.item_chat_incoming).with(R.id.tvMessageContent, owner.getString(R.string.ui_uirows_text_28)).with(R.id.tvTime, "14:20"),
                new PreviewRow(R.layout.item_chat_outgoing).with(R.id.tvMessageContent, owner.getString(R.string.ui_uirows_text_29)).with(R.id.tvTime, "14:21"),
                new PreviewRow(R.layout.item_chat_image_incoming).with(R.id.tvTime, "14:22"),
                new PreviewRow(R.layout.item_chat_image_outgoing).with(R.id.tvTime, "14:23"),
                new PreviewRow(R.layout.item_chat_booking_incoming).with(R.id.tvBookingTitle, owner.getString(R.string.ui_uirows_text_30))
                        .with(R.id.tvBookingReason, owner.getString(R.string.ui_uirows_text_31)).with(R.id.tvTime, "14:24"),
                new PreviewRow(R.layout.item_chat_booking_outgoing).with(R.id.tvBookingTitle, owner.getString(R.string.ui_uirows_text_32))
                        .with(R.id.tvBookingReason, owner.getString(R.string.ui_uirows_text_33)).with(R.id.tvTime, "14:25"));
    }

    @Override protected void bindActions(View root, PreviewRow row) {
        BaseFragment.on(root, R.id.btnAccept, owner::previewNotice);
        BaseFragment.on(root, R.id.btnReject, owner::previewNotice);
        BaseFragment.on(root, R.id.btnViewDetail, () -> owner.dialog(R.layout.dialog_booking_detail));
        root.setOnLongClickListener(view -> {
            boolean message = row.layout == R.layout.item_chat_incoming || row.layout == R.layout.item_chat_outgoing
                    || row.layout == R.layout.item_chat_image_incoming || row.layout == R.layout.item_chat_image_outgoing;
            if (message) UiDialogs.showPopup((MainActivity) owner.requireActivity(), root, R.layout.layout_reaction_popup);
            return message;
        });
    }

    public void addMessage(String message) {
        addRow(new PreviewRow(R.layout.item_chat_outgoing).with(R.id.tvMessageContent, message)
                .with(R.id.tvTime, owner.getString(R.string.ui_uirows_text_1)));
    }
}
