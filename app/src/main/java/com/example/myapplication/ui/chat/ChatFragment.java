package com.example.myapplication.ui.chat;

import com.example.myapplication.databinding.FragmentChatBinding;
import com.example.myapplication.ui.chat.adapter.MessageAdapter;
import android.os.Bundle;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.main.UiScreen;

public class ChatFragment extends BaseFragment<FragmentChatBinding> {
    private void bindChat(View root) {
        binding.tvChatTitle.setText("Minh Anh");
        binding.tvOnlineStatus.setText(getString(R.string.ui_studyscreenfragment_text_21));
        MessageAdapter messages = new MessageAdapter(this);
        list(root, R.id.rvChatMessages, messages);
        binding.btnSend.setOnClickListener(bindingView -> {
            String message = binding.etMessage.getText().toString().trim();
            if (message.isEmpty()) return;
            messages.addMessage(message);
            binding.etMessage.setText("");
            ((RecyclerView) binding.rvChatMessages).scrollToPosition(messages.getItemCount() - 1);
        });
        binding.btnCreateSchedule.setOnClickListener(bindingView -> go(UiScreen.CREATE_APPOINTMENT));
        binding.tvChatTitle.setOnClickListener(bindingView -> go(UiScreen.GROUP_DETAIL));
        binding.btnSticker.setOnClickListener(bindingView -> dialog(R.layout.layout_reaction_popup));
        binding.btnGallery.setOnClickListener(bindingView -> this.previewNotice());
        binding.btnFolder.setOnClickListener(bindingView -> this.previewNotice());
        binding.btnMic.setOnClickListener(bindingView -> this.previewNotice());
        binding.btnCancelReply.setOnClickListener(bindingView -> binding.layoutReplyPreview.setVisibility(android.view.View.GONE));
    }

    public ChatFragment() { super(FragmentChatBinding::inflate); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        bindChat(root);
    }
}
