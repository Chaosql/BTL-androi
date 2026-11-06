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
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.base.RowBindings;
import com.example.myapplication.ui.chat.ChatUi;
import java.util.*;
import java.util.function.Consumer;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.Holder> {
    private final String self;
    private final Consumer<MessageResponse> actions;
    private final Consumer<String> booking;
    private final List<MessageResponse> items = new ArrayList<>();
    private final Set<String> deleted = new HashSet<>();
    private final Set<String> recalled = new HashSet<>();
    public MessageAdapter(String self, Consumer<MessageResponse> actions, Consumer<String> booking) {
        this.self = self; this.actions = actions; this.booking = booking;
    }
    public List<MessageResponse> messages() { return new ArrayList<>(items); }
    public boolean contains(String id) { for (MessageResponse item : items) if (id.equals(item.id)) return true; return false; }
    public void merge(List<MessageResponse> data) {
        for (MessageResponse item : data) {
            if (item == null || item.id == null || item.id.isEmpty() || deleted.contains(item.id)) continue;
            if (recalled.contains(item.id)) item.isRecalled = true;
            if (Boolean.TRUE.equals(item.isRecalled)) recalled.add(item.id);
            items.removeIf(old -> item.id.equals(old.id)); items.add(item);
        }
        items.sort(Comparator.comparing((MessageResponse item) -> ChatUi.instant(item.createdDate)).thenComparing(item -> item.id));
        notifyDataSetChanged();
    }
    public void remove(String id) { deleted.add(id); items.removeIf(item -> id.equals(item.id)); notifyDataSetChanged(); }
    public void recall(String id) {
        recalled.add(id);
        for (MessageResponse item : items) if (id.equals(item.id)) item.isRecalled = true;
        notifyDataSetChanged();
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
        return new Holder(RowBindings.inflate(type, LayoutInflater.from(parent.getContext()), parent));
    }
    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        MessageResponse item = items.get(position);
        View root = holder.binding.getRoot();
        boolean recalled = Boolean.TRUE.equals(item.isRecalled);
        BaseFragment.text(root, R.id.tvMessageContent, recalled ? root.getContext().getString(R.string.chat_recalled) : ChatUi.value(item.content));
        BaseFragment.text(root, R.id.tvTime, ChatUi.time(item.createdDate));
        for (int id : new int[]{R.id.ivMyAvatar, R.id.ivSenderAvatar}) {
            ImageView avatar = root.findViewById(id);
            if (avatar != null) Glide.with(avatar).load(item.senderAvatar).placeholder(R.drawable.avatar).error(R.drawable.avatar).into(avatar);
        }
        ImageView image = root.findViewById(R.id.ivImageContent);
        if (image != null) {
            String url = item.attachments != null && !item.attachments.isEmpty() && item.attachments.get(0) != null
                    ? item.attachments.get(0).fileUrl : item.content;
            Glide.with(image).load(url).placeholder(R.drawable.bg_profile).error(R.drawable.bg_profile).into(image);
        }
        visible(root, R.id.layoutReplyInBubble, !recalled && item.replyToMessage != null);
        visible(root, R.id.tvForwardedHeader, !recalled && item.forwardedFrom != null);
        if (item.replyToMessage != null) {
            BaseFragment.text(root, R.id.tvReplySender, ChatUi.value(item.replyToMessage.senderName));
            BaseFragment.text(root, R.id.tvReplyContent, ChatUi.value(item.replyToMessage.content));
        }
        LinearLayout reactions = root.findViewById(R.id.layoutReactions);
        if (reactions != null) {
            reactions.removeAllViews();
            if (!recalled && item.reactions != null) for (ReactionResponse reaction : item.reactions) {
                if (reaction == null || reaction.emoji == null) continue;
                TextView label = new TextView(root.getContext());
                label.setText(root.getContext().getString(R.string.chat_reaction_count, reaction.emoji, reaction.count == null ? 0 : reaction.count));
                label.setTextColor(root.getContext().getColor(R.color.brown));
                label.setPadding(6, 0, 6, 0);
                label.setOnClickListener(view -> actions.accept(item)); reactions.addView(label);
            }
            reactions.setVisibility(reactions.getChildCount() == 0 ? View.GONE : View.VISIBLE);
        }
        String bookingId = ChatUi.bookingId(item);
        BaseFragment.text(root, R.id.tvBookingTitle, root.getContext().getString(R.string.title_booking_detail));
        BaseFragment.text(root, R.id.tvBookingReason, "");
        visible(root, R.id.btnAccept, false); visible(root, R.id.btnReject, false);
        BaseFragment.on(root, R.id.btnViewDetail, () -> { if (!bookingId.isEmpty()) booking.accept(bookingId); });
        root.setOnLongClickListener(view -> { actions.accept(item); return true; });
    }
    private void visible(View root, int id, boolean visible) {
        View view = root.findViewById(id); if (view != null) view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
    public static class Holder extends RecyclerView.ViewHolder {
        public final ViewBinding binding;
        Holder(ViewBinding binding) { super(binding.getRoot()); this.binding = binding; }
    }
}
