package com.example.myapplication.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Message {
    @SerializedName("id")
    public String id;
    @SerializedName("conversationId")
    public String conversationId;
    @SerializedName("sender")
    public User sender;
    @SerializedName("content")
    public String content;
    @SerializedName("type")
    public MessageType type;
    @SerializedName("status")
    public MessageStatus status = MessageStatus.SENT;
    @SerializedName("createdAt")
    public String createdAt = "";
    @SerializedName("updatedAt")
    public String updatedAt = "";
    @SerializedName("isRecalled")
    public Boolean isRecalled = false;
    @SerializedName("isDeleted")
    public Boolean isDeleted = false;
    @SerializedName("replyToMessage")
    public ReplyMessage replyToMessage;
    @SerializedName("forwardedFrom")
    public ReplyMessage forwardedFrom;
    @SerializedName("reactions")
    public List<Reaction> reactions = new ArrayList<>();

    public Message() { }

    public Message(String id, String conversationId, User sender, String content, MessageType type, MessageStatus status, String createdAt, String updatedAt, Boolean isRecalled, Boolean isDeleted, ReplyMessage replyToMessage, ReplyMessage forwardedFrom, List<Reaction> reactions) {
        this.id = id;
        this.conversationId = conversationId;
        this.sender = sender;
        this.content = content;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isRecalled = isRecalled;
        this.isDeleted = isDeleted;
        this.replyToMessage = replyToMessage;
        this.forwardedFrom = forwardedFrom;
        this.reactions = reactions;
    }

    public String getId() { return id; }

    public String getConversationId() { return conversationId; }

    public User getSender() { return sender; }

    public String getContent() { return content; }

    public MessageType getType() { return type; }

    public MessageStatus getStatus() { return status; }

    public String getCreatedAt() { return createdAt; }

    public String getUpdatedAt() { return updatedAt; }

    public Boolean getIsRecalled() { return isRecalled; }

    public Boolean getIsDeleted() { return isDeleted; }

    public ReplyMessage getReplyToMessage() { return replyToMessage; }

    public ReplyMessage getForwardedFrom() { return forwardedFrom; }

    public List<Reaction> getReactions() { return reactions; }
}
