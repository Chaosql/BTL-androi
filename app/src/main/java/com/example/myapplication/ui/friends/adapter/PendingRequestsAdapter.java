package com.example.myapplication.ui.friends.adapter;

import com.example.myapplication.data.remote.dto.response.FriendDto;
import com.example.myapplication.databinding.ItemPendingRequestBinding;
import com.example.myapplication.ui.friends.SocialUi;
import java.util.function.Consumer;

public class PendingRequestsAdapter extends SocialAdapter<FriendDto, ItemPendingRequestBinding> {
    private final Consumer<FriendDto> click;
    private final Consumer<FriendDto> accept;
    private final Consumer<FriendDto> reject;
    public PendingRequestsAdapter(Consumer<FriendDto> click, Consumer<FriendDto> accept, Consumer<FriendDto> reject) {
        super(ItemPendingRequestBinding::inflate); this.click = click; this.accept = accept; this.reject = reject;
    }
    @Override protected void bind(ItemPendingRequestBinding binding, FriendDto item) {
        binding.tvUserName.setText(SocialUi.name(item.firstName, item.lastName));
        binding.tvUserPhone.setText(SocialUi.value(item.phone));
        SocialUi.avatar(binding.ivAvatar, item.avatar);
        boolean enabled = item.requestId != null && !item.requestId.isEmpty() && !busy.contains(item.requestId);
        binding.btnAccept.setEnabled(enabled);
        binding.btnReject.setEnabled(enabled);
        binding.getRoot().setOnClickListener(view -> click.accept(item));
        binding.btnAccept.setOnClickListener(view -> accept.accept(item));
        binding.btnReject.setOnClickListener(view -> reject.accept(item));
    }
}
