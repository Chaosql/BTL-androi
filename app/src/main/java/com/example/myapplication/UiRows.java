package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class UiRows {
    static final class Row {
        final int layout;
        final Map<Integer, String> text = new LinkedHashMap<>();
        Runnable click;
        Row(int layout) { this.layout = layout; }
        Row with(int id, String value) { text.put(id, value); return this; }
        Row click(Runnable action) { click = action; return this; }
    }

    public static class Adapter extends RecyclerView.Adapter<Adapter.Holder> {
        private final List<Row> source = new ArrayList<>();
        private final List<Row> visible = new ArrayList<>();
        private final StudyScreenFragment owner;
        Adapter(StudyScreenFragment owner, Row... rows) {
            this.owner = owner;
            for (Row row : rows) source.add(row);
            visible.addAll(source);
        }
        @Override public int getItemViewType(int position) { return visible.get(position).layout; }
        @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int layout) {
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false));
        }
        @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
            Row row = visible.get(position);
            for (Map.Entry<Integer, String> entry : row.text.entrySet())
                StudyScreenFragment.text(holder.itemView, entry.getKey(), entry.getValue());
            for (int id : new int[]{R.id.ivFriendAvatar, R.id.ivAvatar, R.id.ivSenderAvatar, R.id.ivMyAvatar, R.id.imgAvatar}) {
                View view = holder.itemView.findViewById(id);
                if (view instanceof ImageView) ((ImageView) view).setImageResource(R.drawable.avatar);
            }
            View image = holder.itemView.findViewById(R.id.ivImageContent);
            if (image instanceof ImageView) ((ImageView) image).setImageResource(R.drawable.bg_profile);
            holder.itemView.setOnClickListener(v -> { if (row.click != null) row.click.run(); });
            holder.itemView.setOnLongClickListener(v -> {
                if (row.layout == R.layout.item_chat_incoming || row.layout == R.layout.item_chat_outgoing
                        || row.layout == R.layout.item_chat_image_incoming || row.layout == R.layout.item_chat_image_outgoing) {
                    UiDialogs.showPopup((MainActivity) owner.requireActivity(), holder.itemView, R.layout.layout_reaction_popup);
                    return true;
                }
                return false;
            });
            StudyScreenFragment.on(holder.itemView, R.id.btnMore, () -> owner.dialog(R.layout.dialog_friend_options));
            StudyScreenFragment.on(holder.itemView, R.id.btnAction, owner::previewNotice);
            StudyScreenFragment.on(holder.itemView, R.id.btnAccept, owner::previewNotice);
            StudyScreenFragment.on(holder.itemView, R.id.btnReject, owner::previewNotice);
            StudyScreenFragment.on(holder.itemView, R.id.btnUnblock, () -> owner.dialog(R.layout.dialog_confirm_delete));
            StudyScreenFragment.on(holder.itemView, R.id.btnViewDetail, () -> owner.dialog(R.layout.dialog_booking_detail));
        }
        @Override public int getItemCount() { return visible.size(); }
        void filter(String query) {
            String keyword = query.trim().toLowerCase(Locale.ROOT);
            visible.clear();
            for (Row row : source) {
                String content = String.join(" ", row.text.values()).toLowerCase(Locale.ROOT);
                if (content.contains(keyword)) visible.add(row);
            }
            notifyDataSetChanged();
        }
        void addMessage(String message) {
            Row row = new Row(R.layout.item_chat_outgoing).with(R.id.tvMessageContent, message).with(R.id.tvTime, owner.getString(R.string.ui_uirows_text_1));
            source.add(row);
            visible.add(row);
            notifyItemInserted(visible.size() - 1);
        }
        static class Holder extends RecyclerView.ViewHolder { Holder(View view) { super(view); } }
    }

    static Adapter friends(StudyScreenFragment owner, boolean group) {
        return new Adapter(owner,
                new Row(R.layout.item_friend).with(R.id.tvFriendName, "Minh Anh").with(R.id.tvLastMessage, owner.getString(R.string.ui_uirows_text_2))
                        .with(R.id.tvTime, "14:30").click(() -> owner.go(UiScreen.CHAT)),
                new Row(R.layout.item_friend).with(R.id.tvFriendName, owner.getString(R.string.ui_uirows_text_3)).with(R.id.tvLastMessage, owner.getString(R.string.ui_uirows_text_4))
                        .with(R.id.tvTime, "13:15").click(() -> owner.go(UiScreen.CHAT)),
                new Row(R.layout.item_friend).with(R.id.tvFriendName, group ? owner.getString(R.string.ui_uirows_text_5) : owner.getString(R.string.ui_studyscreenfragment_text_2))
                        .with(R.id.tvLastMessage, group ? owner.getString(R.string.ui_uirows_text_7) : owner.getString(R.string.ui_uirows_text_8))
                        .with(R.id.tvTime, owner.getString(R.string.ui_uirows_text_9)).click(() -> owner.go(group ? UiScreen.PROFILE : UiScreen.GROUP_DETAIL)));
    }
    static Adapter users(StudyScreenFragment owner, boolean pending) {
        int layout = pending ? R.layout.item_pending_request : R.layout.item_user_search_result;
        return new Adapter(owner,
                new Row(layout).with(R.id.tvUserName, owner.getString(R.string.ui_uirows_text_5)).with(R.id.tvUserPhone, owner.getString(R.string.ui_uirows_text_11))
                        .with(R.id.btnAction, owner.getString(R.string.ui_uirows_text_12)).click(() -> owner.dialog(R.layout.dialog_user_info)),
                new Row(layout).with(R.id.tvUserName, owner.getString(R.string.ui_uirows_text_13)).with(R.id.tvUserPhone, owner.getString(R.string.ui_uirows_text_14))
                        .with(R.id.btnAction, owner.getString(R.string.ui_uirows_text_12)).click(() -> owner.dialog(R.layout.dialog_user_info)));
    }
    static Adapter blocked(StudyScreenFragment owner) {
        return new Adapter(owner, new Row(R.layout.item_blocked_user)
                .with(R.id.tvUserName, owner.getString(R.string.ui_uirows_text_16)).with(R.id.tvUserPhone, owner.getString(R.string.ui_uirows_text_17)));
    }
    static Adapter notifications(StudyScreenFragment owner, boolean recent) {
        return new Adapter(owner,
                new Row(R.layout.item_notification).with(R.id.tvContent, recent ? owner.getString(R.string.ui_uirows_text_18) : owner.getString(R.string.ui_uirows_text_19))
                        .with(R.id.tvTimeAgo, recent ? owner.getString(R.string.ui_uirows_text_20) : owner.getString(R.string.ui_uirows_text_9)).click(() -> owner.go(UiScreen.SCHEDULE)),
                new Row(R.layout.item_notification).with(R.id.tvContent, recent ? owner.getString(R.string.ui_uirows_text_22) : owner.getString(R.string.ui_uirows_text_23))
                        .with(R.id.tvTimeAgo, recent ? owner.getString(R.string.ui_uirows_text_24) : owner.getString(R.string.ui_uirows_text_25)).click(() -> owner.go(UiScreen.PENDING_REQUESTS)));
    }
    static Adapter schedule(StudyScreenFragment owner) {
        return new Adapter(owner,
                new Row(R.layout.item_schedule).with(R.id.tvTime, "14:30").with(R.id.tvTaskDescription, owner.getString(R.string.ui_uirows_text_26))
                        .click(() -> owner.dialog(R.layout.dialog_booking_detail)),
                new Row(R.layout.item_schedule).with(R.id.tvTime, "17:00").with(R.id.tvTaskDescription, owner.getString(R.string.ui_uirows_text_27))
                        .click(() -> owner.dialog(R.layout.dialog_booking_detail)));
    }
    static Adapter messages(StudyScreenFragment owner) {
        return new Adapter(owner,
                new Row(R.layout.item_chat_incoming).with(R.id.tvMessageContent, owner.getString(R.string.ui_uirows_text_28)).with(R.id.tvTime, "14:20"),
                new Row(R.layout.item_chat_outgoing).with(R.id.tvMessageContent, owner.getString(R.string.ui_uirows_text_29)).with(R.id.tvTime, "14:21"),
                new Row(R.layout.item_chat_image_incoming).with(R.id.tvTime, "14:22"),
                new Row(R.layout.item_chat_image_outgoing).with(R.id.tvTime, "14:23"),
                new Row(R.layout.item_chat_booking_incoming).with(R.id.tvBookingTitle, owner.getString(R.string.ui_uirows_text_30))
                        .with(R.id.tvBookingReason, owner.getString(R.string.ui_uirows_text_31)).with(R.id.tvTime, "14:24"),
                new Row(R.layout.item_chat_booking_outgoing).with(R.id.tvBookingTitle, owner.getString(R.string.ui_uirows_text_32))
                        .with(R.id.tvBookingReason, owner.getString(R.string.ui_uirows_text_33)).with(R.id.tvTime, "14:25"));
    }
    private UiRows() { }
}
