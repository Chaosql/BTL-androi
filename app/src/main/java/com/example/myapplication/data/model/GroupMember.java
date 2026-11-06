package com.example.myapplication.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class GroupMember {
    @SerializedName("id")
    public String id;
    @SerializedName("conversationId")
    public String conversationId;
    @SerializedName("user")
    public User user;
    @SerializedName("role")
    public MemberRole role;
    @SerializedName("joinedAt")
    public String joinedAt;

    public GroupMember() { }

    public GroupMember(String id, String conversationId, User user, MemberRole role, String joinedAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.user = user;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public String getId() { return id; }

    public String getConversationId() { return conversationId; }

    public User getUser() { return user; }

    public MemberRole getRole() { return role; }

    public String getJoinedAt() { return joinedAt; }
}
