package com.example.myapplication.ui.friends.adapter;

import com.example.myapplication.R;
import com.example.myapplication.data.remote.dto.response.UserSearchDto;
import com.example.myapplication.databinding.ItemUserSearchResultBinding;
import com.example.myapplication.ui.friends.SocialUi;
import java.util.function.Consumer;

public class UserSearchAdapter extends SocialAdapter<UserSearchDto, ItemUserSearchResultBinding> {
    private final Consumer<UserSearchDto> click;
    private final Consumer<UserSearchDto> add;
    public UserSearchAdapter(Consumer<UserSearchDto> click, Consumer<UserSearchDto> add) {
        super(ItemUserSearchResultBinding::inflate); this.click = click; this.add = add;
    }
    @Override protected void bind(ItemUserSearchResultBinding binding, UserSearchDto item) {
        binding.tvUserName.setText(SocialUi.name(item.firstName, item.lastName));
        binding.tvUserPhone.setText(SocialUi.value(item.phone));
        SocialUi.avatar(binding.ivAvatar, item.avatar);
        boolean friend = SocialUi.friend(item.friendshipStatus);
        boolean pending = SocialUi.pending(item.friendshipStatus);
        binding.btnAction.setText(friend ? R.string.chat_message : pending ? R.string.social_sent : R.string.social_add);
        binding.btnAction.setEnabled(!pending && !busy.contains(item.id) && item.id != null && !item.id.isEmpty());
        binding.btnAction.setAlpha(binding.btnAction.isEnabled() ? 1f : 0.6f);
        binding.getRoot().setOnClickListener(view -> click.accept(item));
        binding.btnAction.setOnClickListener(view -> add.accept(item));
    }
}
