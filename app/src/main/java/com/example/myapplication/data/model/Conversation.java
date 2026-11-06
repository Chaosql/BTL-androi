package com.example.myapplication.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Conversation {
    @SerializedName("id")
    public String id;
    @SerializedName("type")
    public ConversationType type;
    @SerializedName("name")
    public String name;
    @SerializedName("avatar")
    public String avatar;
    @SerializedName("ownerId")
    public String ownerId;
    @SerializedName("lastMessage")
    public Message lastMessage;
    @SerializedName("lastMessageTime")
    public String lastMessageTime = "";
    @SerializedName("lastSenderId")
    public String lastSenderId;
    @SerializedName("memberCount")
    public Integer memberCount = 0;
    @SerializedName("unreadCount")
    public Integer unreadCount = 0;
    @SerializedName("hasLeft")
    public Boolean hasLeft = false;
    @SerializedName("createdAt")
    public String createdAt = "";
    @SerializedName("updatedAt")
    public String updatedAt = "";

    public Conversation() { }

    public Conversation(String id, ConversationType type, String name, String avatar, String ownerId, Message lastMessage, String lastMessageTime, String lastSenderId, Integer memberCount, Integer unreadCount, Boolean hasLeft, String createdAt, String updatedAt) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.avatar = avatar;
        this.ownerId = ownerId;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.lastSenderId = lastSenderId;
        this.memberCount = memberCount;
        this.unreadCount = unreadCount;
        this.hasLeft = hasLeft;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }

    public ConversationType getType() { return type; }

    public String getName() { return name; }

    public String getAvatar() { return avatar; }

    public String getOwnerId() { return ownerId; }

    public Message getLastMessage() { return lastMessage; }

    public String getLastMessageTime() { return lastMessageTime; }

    public String getLastSenderId() { return lastSenderId; }

    public Integer getMemberCount() { return memberCount; }

    public Integer getUnreadCount() { return unreadCount; }

    public Boolean getHasLeft() { return hasLeft; }

    public String getCreatedAt() { return createdAt; }

    public String getUpdatedAt() { return updatedAt; }
}
