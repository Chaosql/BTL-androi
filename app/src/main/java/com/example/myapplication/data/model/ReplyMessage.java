package com.example.myapplication.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ReplyMessage {
    @SerializedName("id")
    public String id;
    @SerializedName("senderName")
    public String senderName;
    @SerializedName("content")
    public String content;
    @SerializedName("messageType")
    public String messageType;

    public ReplyMessage() { }

    public ReplyMessage(String id, String senderName, String content, String messageType) {
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
