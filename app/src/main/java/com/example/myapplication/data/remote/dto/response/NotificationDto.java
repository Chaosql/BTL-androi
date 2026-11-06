package com.example.myapplication.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class NotificationDto {
    @SerializedName("id")
    public String id;
    @SerializedName("type")
    public String type;
    @SerializedName("content")
    public String content;
    @SerializedName("actorId")
    public String actorId;
    @SerializedName("actorFirstName")
    public String actorFirstName;
    @SerializedName("actorLastName")
    public String actorLastName;
    @SerializedName("actorAvatar")
    public String actorAvatar;
    @SerializedName("targetId")
    public String targetId;
    @SerializedName("targetType")
    public String targetType;
    @SerializedName("isRead")
    public Boolean isRead;
    @SerializedName("createdDate")
    public String createdDate;

    public NotificationDto() { }

    public NotificationDto(String id, String type, String content, String actorId, String actorFirstName, String actorLastName, String actorAvatar, String targetId, String targetType, Boolean isRead, String createdDate) {
        this.id = id;
        this.type = type;
        this.content = content;
        this.actorId = actorId;
        this.actorFirstName = actorFirstName;
        this.actorLastName = actorLastName;
        this.actorAvatar = actorAvatar;
        this.targetId = targetId;
        this.targetType = targetType;
        this.isRead = isRead;
        this.createdDate = createdDate;
    }

    public String getId() { return id; }

    public String getType() { return type; }

    public String getContent() { return content; }

    public String getActorId() { return actorId; }

    public String getActorFirstName() { return actorFirstName; }

    public String getActorLastName() { return actorLastName; }

    public String getActorAvatar() { return actorAvatar; }

    public String getTargetId() { return targetId; }

    public String getTargetType() { return targetType; }

    public Boolean getIsRead() { return isRead; }

    public String getCreatedDate() { return createdDate; }
}
