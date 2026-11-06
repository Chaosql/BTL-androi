package com.example.myapplication.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class SearchConversationResponse {
    @SerializedName("conversationId")
    public Long conversationId;
    @SerializedName("conversationName")
    public String conversationName;
    @SerializedName("avatar")
    public String avatar;
    @SerializedName("lastMessage")
    public Message lastMessage;

    public SearchConversationResponse() { }

    public SearchConversationResponse(Long conversationId, String conversationName, String avatar, Message lastMessage) {
        this.conversationId = conversationId;
        this.conversationName = conversationName;
        this.avatar = avatar;
        this.lastMessage = lastMessage;
    }

    public Long getConversationId() { return conversationId; }

    public String getConversationName() { return conversationName; }

    public String getAvatar() { return avatar; }

    public Message getLastMessage() { return lastMessage; }
}
