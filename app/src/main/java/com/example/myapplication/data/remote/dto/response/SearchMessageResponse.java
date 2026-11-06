package com.example.myapplication.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class SearchMessageResponse {
    @SerializedName("messageId")
    public Long messageId;
    @SerializedName("conversationId")
    public Long conversationId;
    @SerializedName("sender")
    public User sender;
    @SerializedName("content")
    public String content;
    @SerializedName("createdAt")
    public String createdAt;

    public SearchMessageResponse() { }

    public SearchMessageResponse(Long messageId, Long conversationId, User sender, String content, String createdAt) {
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.sender = sender;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getMessageId() { return messageId; }

    public Long getConversationId() { return conversationId; }

    public User getSender() { return sender; }

    public String getContent() { return content; }

    public String getCreatedAt() { return createdAt; }
}
