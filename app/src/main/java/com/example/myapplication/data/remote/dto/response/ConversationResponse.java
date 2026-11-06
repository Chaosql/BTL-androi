package com.example.myapplication.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class ConversationResponse {
    @SerializedName("id")
    public String id;
    @SerializedName("type")
    public String type;
    @SerializedName("groupName")
    public String groupName;
    @SerializedName("groupAvatar")
    public String groupAvatar;
    @SerializedName("memberCount")
    public Integer memberCount = 0;
    @SerializedName("unreadCount")
    public Integer unreadCount = 0;
    @SerializedName("hasLeft")
    public Boolean hasLeft = false;
    @SerializedName("lastMessage")
    public MessageResponse lastMessage;

    public ConversationResponse() { }

    public ConversationResponse(String id, String type, String groupName, String groupAvatar, Integer memberCount, Integer unreadCount, Boolean hasLeft, MessageResponse lastMessage) {
        this.id = id;
        this.type = type;
        this.groupName = groupName;
        this.groupAvatar = groupAvatar;
        this.memberCount = memberCount;
        this.unreadCount = unreadCount;
        this.hasLeft = hasLeft;
        this.lastMessage = lastMessage;
    }

    public String getId() { return id; }

    public String getType() { return type; }

    public String getGroupName() { return groupName; }

    public String getGroupAvatar() { return groupAvatar; }

    public Integer getMemberCount() { return memberCount; }

    public Integer getUnreadCount() { return unreadCount; }

    public Boolean getHasLeft() { return hasLeft; }

    public MessageResponse getLastMessage() { return lastMessage; }
}
