package com.example.myapplication.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class SendMessageRequest {
    @SerializedName("conversationId")
    public Long conversationId;
    @SerializedName("content")
    public String content;
    @SerializedName("type")
    public MessageType type;

    public SendMessageRequest() { }

    public SendMessageRequest(Long conversationId, String content, MessageType type) {
        this.conversationId = conversationId;
        this.content = content;
        this.type = type;
    }

    public Long getConversationId() { return conversationId; }

    public String getContent() { return content; }

    public MessageType getType() { return type; }
}
