package com.example.myapplication.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class MessageResponse {
    @SerializedName("id")
    public String id;
    @SerializedName("conversationId")
    public String conversationId;
    @SerializedName("senderId")
    public String senderId;
    @SerializedName("senderName")
    public String senderName;
    @SerializedName("senderAvatar")
    public String senderAvatar;
    @SerializedName("content")
    public String content;
    @SerializedName("messageType")
    public String messageType;
    @SerializedName("isRecalled")
    public Boolean isRecalled = false;
    @SerializedName("createdDate")
    public String createdDate;
    @SerializedName("isPinned")
    public Boolean isPinned = false;
    @SerializedName("attachments")
    public List<AttachmentResponse> attachments;
    @SerializedName("replyToMessage")
    public ReplyMessageResponse replyToMessage;
    @SerializedName("forwardedFrom")
    public ReplyMessageResponse forwardedFrom;
    @SerializedName("reactions")
    public List<ReactionResponse> reactions;

    public MessageResponse() { }

    public MessageResponse(String id, String conversationId, String senderId, String senderName, String senderAvatar, String content, String messageType, Boolean isRecalled, String createdDate, Boolean isPinned, List<AttachmentResponse> attachments, ReplyMessageResponse replyToMessage, ReplyMessageResponse forwardedFrom, List<ReactionResponse> reactions) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderAvatar = senderAvatar;
        this.content = content;
        this.messageType = messageType;
        this.isRecalled = isRecalled;
        this.createdDate = createdDate;
        this.isPinned = isPinned;
        this.attachments = attachments;
        this.replyToMessage = replyToMessage;
        this.forwardedFrom = forwardedFrom;
        this.reactions = reactions;
    }

    public String getId() { return id; }

    public String getConversationId() { return conversationId; }

    public String getSenderId() { return senderId; }

    public String getSenderName() { return senderName; }

    public String getSenderAvatar() { return senderAvatar; }

    public String getContent() { return content; }

    public String getMessageType() { return messageType; }

    public Boolean getIsRecalled() { return isRecalled; }

    public String getCreatedDate() { return createdDate; }

    public Boolean getIsPinned() { return isPinned; }

    public List<AttachmentResponse> getAttachments() { return attachments; }

    public ReplyMessageResponse getReplyToMessage() { return replyToMessage; }

    public ReplyMessageResponse getForwardedFrom() { return forwardedFrom; }

    public List<ReactionResponse> getReactions() { return reactions; }
}
