package com.example.myapplication.ui.chat;

import com.example.myapplication.ui.chat.adapter.MessageAdapter;
import android.os.Bundle;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.main.UiScreen;

public class ChatFragment extends BaseFragment {
    private void bindChat(View root) {
        text(root, R.id.tvChatTitle, "Minh Anh");
        text(root, R.id.tvOnlineStatus, getString(R.string.ui_studyscreenfragment_text_21));
        MessageAdapter messages = new MessageAdapter(this);
        list(root, R.id.rvChatMessages, messages);
        on(root, R.id.btnSend, () -> {
            String message = value(root, R.id.etMessage).trim();
            if (message.isEmpty()) return;
            messages.addMessage(message);
            text(root, R.id.etMessage, "");
            ((RecyclerView) root.findViewById(R.id.rvChatMessages)).scrollToPosition(messages.getItemCount() - 1);
        });
        on(root, R.id.btnCreateSchedule, () -> go(UiScreen.CREATE_APPOINTMENT));
        on(root, R.id.tvChatTitle, () -> go(UiScreen.GROUP_DETAIL));
        on(root, R.id.btnSticker, () -> dialog(R.layout.layout_reaction_popup));
        on(root, R.id.btnGallery, this::previewNotice);
        on(root, R.id.btnFolder, this::previewNotice);
        on(root, R.id.btnMic, this::previewNotice);
        on(root, R.id.btnCancelReply, () -> hide(root, R.id.layoutReplyPreview));
    }

    public ChatFragment() { super(R.layout.fragment_chat); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        bindChat(root);
    }
}
