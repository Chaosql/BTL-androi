package com.example.myapplication.ui.friends.adapter;

import com.example.myapplication.data.remote.dto.response.BlockedUserDto;
import com.example.myapplication.databinding.ItemBlockedUserBinding;
import com.example.myapplication.ui.friends.SocialUi;
import java.util.function.Consumer;

public class BlockedUsersAdapter extends SocialAdapter<BlockedUserDto, ItemBlockedUserBinding> {
    private final Consumer<BlockedUserDto> unblock;
    public BlockedUsersAdapter(Consumer<BlockedUserDto> unblock) {
        super(ItemBlockedUserBinding::inflate); this.unblock = unblock;
    }
    @Override protected void bind(ItemBlockedUserBinding binding, BlockedUserDto item) {
        binding.tvUserName.setText(SocialUi.name(item.firstName, item.lastName));
        binding.tvUserPhone.setText(SocialUi.value(item.phone));
        SocialUi.avatar(binding.ivAvatar, item.avatar);
        binding.btnUnblock.setEnabled(item.id != null && !item.id.isEmpty() && !busy.contains(item.id));
        binding.btnUnblock.setOnClickListener(view -> unblock.accept(item));
    }
}
