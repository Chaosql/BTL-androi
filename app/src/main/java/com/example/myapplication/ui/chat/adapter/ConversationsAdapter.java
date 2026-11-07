package com.example.myapplication.ui.chat.adapter;

import android.view.View;
import com.example.myapplication.data.remote.dto.response.ConversationResponse;
import com.example.myapplication.databinding.ItemFriendBinding;
import com.example.myapplication.ui.chat.ChatUi;
import com.example.myapplication.ui.friends.SocialUi;
import com.example.myapplication.ui.friends.adapter.SocialAdapter;
import java.util.function.Consumer;

public class ConversationsAdapter extends SocialAdapter<ConversationResponse, ItemFriendBinding> {
    private final String self;
    private final Consumer<ConversationResponse> click;
    public ConversationsAdapter(String self, Consumer<ConversationResponse> click) {
        super(ItemFriendBinding::inflate); this.self = self; this.click = click;
    }
    @Override protected void bind(ItemFriendBinding binding, ConversationResponse item) {
        binding.tvFriendName.setText(ChatUi.conversationName(item, self));
        binding.tvLastMessage.setText(ChatUi.preview(binding.getRoot().getContext(), item.lastMessage));
        binding.tvTime.setText(item.lastMessage == null ? "" : ChatUi.time(item.lastMessage.createdDate));
        binding.viewOnlineBadge.setVisibility(View.GONE); binding.btnMore.setVisibility(View.GONE);
        String avatar = item.groupAvatar;
        if ((avatar == null || avatar.isEmpty()) && item.lastMessage != null && !self.equals(item.lastMessage.senderId))
            avatar = item.lastMessage.senderAvatar;
        SocialUi.avatar(binding.ivFriendAvatar, avatar);
        binding.getRoot().setOnClickListener(view -> click.accept(item));
    }
}
