package com.example.myapplication.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class OnlineStatusDto {
    @SerializedName("userId")
    public String userId;
    @SerializedName("isOnline")
    public Boolean isOnline;
    @SerializedName("lastSeen")
    public String lastSeen;

    public OnlineStatusDto() { }

    public OnlineStatusDto(String userId, Boolean isOnline, String lastSeen) {
        this.userId = userId;
        this.isOnline = isOnline;
        this.lastSeen = lastSeen;
    }

    public String getUserId() { return userId; }

    public Boolean getIsOnline() { return isOnline; }

    public String getLastSeen() { return lastSeen; }
}
