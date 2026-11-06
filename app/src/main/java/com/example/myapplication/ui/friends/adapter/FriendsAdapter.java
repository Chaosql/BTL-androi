package com.example.myapplication.ui.friends.adapter;

import android.view.View;
import com.example.myapplication.data.remote.dto.response.FriendDto;
import com.example.myapplication.databinding.ItemFriendBinding;
import com.example.myapplication.ui.friends.SocialUi;
import java.util.function.Consumer;

public class FriendsAdapter extends SocialAdapter<FriendDto, ItemFriendBinding> {
    private final Consumer<FriendDto> click;
    private final Consumer<FriendDto> more;
    public FriendsAdapter(Consumer<FriendDto> click, Consumer<FriendDto> more) {
        super(ItemFriendBinding::inflate); this.click = click; this.more = more;
    }
    @Override protected void bind(ItemFriendBinding binding, FriendDto item) {
        binding.tvFriendName.setText(SocialUi.name(item.firstName, item.lastName));
        binding.tvLastMessage.setText(SocialUi.value(item.phone));
        binding.tvTime.setText("");
        binding.viewOnlineBadge.setVisibility(View.GONE);
        SocialUi.avatar(binding.ivFriendAvatar, item.avatar);
        binding.getRoot().setOnClickListener(view -> click.accept(item));
        binding.btnMore.setEnabled(item.userId != null && !item.userId.isEmpty());
        binding.btnMore.setOnClickListener(view -> more.accept(item));
    }
}
