package com.example.myapplication.ui.chat.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.remote.dto.response.*;
import com.example.myapplication.databinding.*;
import com.example.myapplication.ui.chat.ChatUi;
import java.util.*;
import java.util.function.Consumer;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.Holder> {
    private final String self;
    private final Consumer<MessageResponse> actions;
    private final Consumer<String> booking;
    private final Consumer<MessageResponse> imageClick;
    private final List<MessageResponse> items = new ArrayList<>();
    public MessageAdapter(String self, Consumer<MessageResponse> actions, Consumer<String> booking) {
        this(self, actions, booking, image -> { });
    }
    public MessageAdapter(String self, Consumer<MessageResponse> actions, Consumer<String> booking, Consumer<MessageResponse> imageClick) {
        this.self = self; this.actions = actions; this.booking = booking;
        this.imageClick = imageClick;
    }
    public List<MessageResponse> messages() { return new ArrayList<>(items); }
    public boolean contains(String id) { for (MessageResponse item : items) if (id.equals(item.id)) return true; return false; }
    public void submit(List<MessageResponse> messages) {
        items.clear(); items.addAll(messages); notifyDataSetChanged();
    }
    @Override public int getItemCount() { return items.size(); }
    @Override public int getItemViewType(int position) {
        MessageResponse item = items.get(position);
        boolean own = self.equals(item.senderId);
        if (!Boolean.TRUE.equals(item.isRecalled) && !ChatUi.bookingId(item).isEmpty())
            return own ? R.layout.item_chat_booking_outgoing : R.layout.item_chat_booking_incoming;
        if (!Boolean.TRUE.equals(item.isRecalled) && "IMAGE".equalsIgnoreCase(item.messageType))
            return own ? R.layout.item_chat_image_outgoing : R.layout.item_chat_image_incoming;
        return own ? R.layout.item_chat_outgoing : R.layout.item_chat_incoming;
    }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (type == R.layout.item_chat_incoming) return new Holder(ItemChatIncomingBinding.inflate(inflater, parent, false));
        if (type == R.layout.item_chat_outgoing) return new Holder(ItemChatOutgoingBinding.inflate(inflater, parent, false));
        if (type == R.layout.item_chat_image_incoming) return new Holder(ItemChatImageIncomingBinding.inflate(inflater, parent, false));
        if (type == R.layout.item_chat_image_outgoing) return new Holder(ItemChatImageOutgoingBinding.inflate(inflater, parent, false));
        if (type == R.layout.item_chat_booking_incoming) return new Holder(ItemChatBookingIncomingBinding.inflate(inflater, parent, false));
        if (type == R.layout.item_chat_booking_outgoing) return new Holder(ItemChatBookingOutgoingBinding.inflate(inflater, parent, false));
        throw new IllegalArgumentException("Unknown message layout: " + type);
    }
    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        MessageResponse item = items.get(position);
        View root = holder.binding.getRoot();
        boolean recalled = Boolean.TRUE.equals(item.isRecalled);
        setText(holder.message, recalled ? root.getContext().getString(R.string.chat_recalled) : ChatUi.value(item.content));
        setText(holder.time, ChatUi.time(item.createdDate));
        Glide.with(holder.avatar).load(item.senderAvatar).placeholder(R.drawable.avatar).error(R.drawable.avatar).into(holder.avatar);
        ImageView image = holder.image;
        if (image != null) {
            Glide.with(image).load(ChatUi.imageUrl(item)).placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground).into(image);
            image.setOnClickListener(view -> {
                if (!Boolean.TRUE.equals(item.isRecalled)) imageClick.accept(item);
            });
            image.setOnLongClickListener(view -> { actions.accept(item); return true; });
        }
        String bookingId = ChatUi.bookingId(item);
        setText(holder.bookingTitle, root.getContext().getString(R.string.title_booking_detail));
        setText(holder.bookingReason, "");
        if (holder.accept != null) holder.accept.setVisibility(View.GONE);
        if (holder.reject != null) holder.reject.setVisibility(View.GONE);
        if (holder.detail != null) holder.detail.setOnClickListener(view -> { if (!bookingId.isEmpty()) booking.accept(bookingId); });
        root.setOnLongClickListener(view -> { actions.accept(item); return true; });
    }
    private static void setText(TextView view, String text) {
        if (view != null) view.setText(text);
    }
    public static class Holder extends RecyclerView.ViewHolder {
        public final ViewBinding binding;
        final TextView message, time, bookingTitle, bookingReason;
        final ImageView avatar, image;
        final View accept, reject, detail;

        Holder(ItemChatIncomingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            message = binding.tvMessageContent;
            time = binding.tvTime;
            bookingTitle = null;
            bookingReason = null;
            image = null;
            accept = null;
            reject = null;
            detail = null;
            avatar = binding.ivSenderAvatar;
        }

        Holder(ItemChatOutgoingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            message = binding.tvMessageContent;
            time = binding.tvTime;
            bookingTitle = null;
            bookingReason = null;
            image = null;
            accept = null;
            reject = null;
            detail = null;
            avatar = binding.ivMyAvatar;
        }

        Holder(ItemChatImageIncomingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            message = null;
            time = binding.tvTime;
            bookingTitle = null;
            bookingReason = null;
            image = binding.ivImageContent;
            accept = null;
            reject = null;
            detail = null;
            avatar = binding.ivSenderAvatar;
        }

        Holder(ItemChatImageOutgoingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            message = null;
            time = binding.tvTime;
            bookingTitle = null;
            bookingReason = null;
            image = binding.ivImageContent;
            accept = null;
            reject = null;
            detail = null;
            avatar = binding.ivMyAvatar;
        }

        Holder(ItemChatBookingIncomingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            message = null;
            time = binding.tvTime;
            bookingTitle = binding.tvBookingTitle;
            bookingReason = binding.tvBookingReason;
            image = null;
            accept = binding.btnAccept;
            reject = binding.btnReject;
            detail = binding.btnViewDetail;
            avatar = binding.ivSenderAvatar;
        }

        Holder(ItemChatBookingOutgoingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            message = null;
            time = binding.tvTime;
            bookingTitle = binding.tvBookingTitle;
            bookingReason = binding.tvBookingReason;
            image = null;
            accept = null;
            reject = null;
            detail = binding.btnViewDetail;
            avatar = binding.ivMyAvatar;
        }
    }
}
