package com.example.myapplication.ui.chat;

import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import com.example.myapplication.data.remote.dto.response.*;
import com.google.gson.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public final class ChatUi {
    public static final String UPDATED = "chat_updated";
    public static final String CONVERSATION = "conversation_id";
    public static final String NAME = "user_name";
    public static final String TARGET = "target_user_id";
    public static String value(String value) { return value == null ? "" : value; }
    public static Bundle args(String conversation, String name, String target) {
        Bundle data = new Bundle(); data.putString(CONVERSATION, conversation); data.putString(NAME, name); data.putString(TARGET, target); return data;
    }
    public static Instant instant(String raw) {
        if (raw == null || raw.isEmpty()) return Instant.EPOCH;
        try { return Instant.parse(raw); }
        catch (RuntimeException ignored) {
            try { return OffsetDateTime.parse(raw).toInstant(); }
            catch (RuntimeException invalid) {
                try { return LocalDateTime.parse(raw.replace(' ', 'T')).toInstant(ZoneOffset.UTC); }
                catch (RuntimeException missing) { return Instant.EPOCH; }
            }
        }
    }
    public static String time(String raw) {
        return time(raw, Clock.systemUTC());
    }
    static String time(String raw, Clock clock) {
        Instant time = instant(raw);
        if (time.equals(Instant.EPOCH)) return "";
        ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");
        boolean today = time.atZone(zone).toLocalDate().equals(LocalDate.now(clock.withZone(zone)));
        return DateTimeFormatter.ofPattern(today ? "HH:mm" : "HH:mm, dd/MM/yyyy")
                .withZone(zone).format(time);
    }
    public static JsonObject data(String body) {
        JsonObject raw = JsonParser.parseString(body).getAsJsonObject();
        return raw.has("data") && raw.get("data").isJsonObject() ? raw.getAsJsonObject("data") : raw;
    }
    public static String field(JsonObject data, String key) {
        return !data.has(key) || data.get(key).isJsonNull() || !data.get(key).isJsonPrimitive() ? "" : data.get(key).getAsString();
    }
    public static MessageResponse message(JsonObject data) {
        MessageResponse result = new Gson().fromJson(data, MessageResponse.class);
        if (result.id == null) result.id = field(data, "messageId");
        if (result.createdDate == null) result.createdDate = field(data, "createdAt");
        if (result.content == null) result.content = field(data, "text");
        if (result.messageType == null) result.messageType = field(data, "type");
        if (data.has("sender") && data.get("sender").isJsonObject()) {
            JsonObject sender = data.getAsJsonObject("sender");
            if (result.senderId == null) result.senderId = field(sender, "id");
            if (result.senderName == null) result.senderName = field(sender, "fullName");
            if (result.senderAvatar == null) result.senderAvatar = field(sender, "avatar");
        }
        return result;
    }
    public static String bookingId(MessageResponse message) {
        try {
            JsonObject data = data(message.content);
            String id = field(data, "bookingId");
            if (id.isEmpty() && data.has("scheduledAt")) id = field(data, "id");
            return id;
        } catch (RuntimeException invalid) { return ""; }
    }
    public static String conversationName(ConversationResponse item, String self) {
        if (item.groupName != null && !item.groupName.isEmpty()) return item.groupName;
        if (item.lastMessage != null && !self.equals(item.lastMessage.senderId) && item.lastMessage.senderName != null)
            return item.lastMessage.senderName;
        return "—";
    }
    public static String preview(android.content.Context context, MessageResponse message) {
        if (message == null) return "";
        if (Boolean.TRUE.equals(message.isRecalled)) return context.getString(com.example.myapplication.R.string.chat_recalled);
        if (!bookingId(message).isEmpty()) return context.getString(com.example.myapplication.R.string.title_booking_detail);
        if ("IMAGE".equalsIgnoreCase(message.messageType)) return context.getString(com.example.myapplication.R.string.chat_image_preview);
        if ("FILE".equalsIgnoreCase(message.messageType)) return context.getString(com.example.myapplication.R.string.chat_file_preview);
        return value(message.content);
    }
    public static String imageUrl(MessageResponse message) {
        if (message.attachments != null) for (AttachmentResponse attachment : message.attachments) {
            if (attachment != null && remoteUrl(attachment.fileUrl)) return attachment.fileUrl;
        }
        return remoteUrl(message.content) ? message.content : null;
    }
    private static boolean remoteUrl(String url) {
        if (url == null) return false;
        okhttp3.HttpUrl parsed = okhttp3.HttpUrl.parse(url);
        return parsed != null && (parsed.scheme().equals("https") || parsed.scheme().equals("http"));
    }
    public static void updated(FragmentActivity activity) { activity.getSupportFragmentManager().setFragmentResult(UPDATED, new Bundle()); }
    private ChatUi() { }
}
