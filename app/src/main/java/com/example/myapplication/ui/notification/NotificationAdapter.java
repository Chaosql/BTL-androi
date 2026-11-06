package com.example.myapplication.ui.notification;

import android.view.*;
import android.graphics.Typeface;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.remote.dto.response.NotificationDto;
import com.example.myapplication.databinding.ItemNotificationBinding;
import com.example.myapplication.ui.chat.ChatUi;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

public final class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.Holder> {
    private final List<NotificationDto> items = new ArrayList<>();
    private final Consumer<NotificationDto> click;
    private Consumer<NotificationDto> delete = item -> { };
    public NotificationAdapter(Consumer<NotificationDto> click) { this.click = click; }
    public NotificationAdapter(Consumer<NotificationDto> click, Consumer<NotificationDto> delete) { this(click); this.delete = delete; }
    public void submit(List<NotificationDto> data) { items.clear(); items.addAll(data); notifyDataSetChanged(); }
    @Override public int getItemCount() { return items.size(); }
    @Override public Holder onCreateViewHolder(ViewGroup parent, int type) {
        return new Holder(ItemNotificationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override public void onBindViewHolder(Holder holder, int position) {
        NotificationDto item = items.get(position);
        ItemNotificationBinding b = holder.binding;
        boolean unread = !Boolean.TRUE.equals(item.isRead);
        b.tvContent.setText(ChatUi.value(item.content));
        b.tvContent.setTypeface(null, unread ? Typeface.BOLD : Typeface.NORMAL);
        b.viewUnreadDot.setVisibility(unread ? View.VISIBLE : View.GONE);
        java.time.Instant time = ChatUi.instant(item.createdDate);
        b.tvTimeAgo.setText(time.equals(java.time.Instant.EPOCH) ? "" : DateTimeFormatter
                .ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("Asia/Ho_Chi_Minh")).format(time));
        Glide.with(b.imgAvatar).load(item.actorAvatar).placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground).circleCrop().into(b.imgAvatar);
        b.getRoot().setOnClickListener(view -> click.accept(item));
        b.getRoot().setOnLongClickListener(view -> { delete.accept(item); return true; });
    }
    static final class Holder extends RecyclerView.ViewHolder {
        final ItemNotificationBinding binding;
        Holder(ItemNotificationBinding binding) { super(binding.getRoot()); this.binding = binding; }
    }
}
