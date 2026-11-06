package com.example.myapplication.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class ReplyMessageResponse {
    @SerializedName("id")
    public String id;
    @SerializedName("senderName")
    public String senderName;
    @SerializedName("content")
    public String content;
    @SerializedName("messageType")
    public String messageType;

    public ReplyMessageResponse() { }

    public ReplyMessageResponse(String id, String senderName, String content, String messageType) {
        this.id = id;
        this.senderName = senderName;
        this.content = content;
        this.messageType = messageType;
    }

    public String getId() { return id; }

    public String getSenderName() { return senderName; }

    public String getContent() { return content; }

    public String getMessageType() { return messageType; }
}
