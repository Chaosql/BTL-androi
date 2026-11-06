package com.example.myapplication.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class DirectChatRequest {
    @SerializedName("targetUserId")
    public String targetUserId;

    public DirectChatRequest() { }

    public DirectChatRequest(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getTargetUserId() { return targetUserId; }
}
