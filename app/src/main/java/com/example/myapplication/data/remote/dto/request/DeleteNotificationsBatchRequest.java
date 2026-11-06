package com.example.myapplication.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class DeleteNotificationsBatchRequest {
    @SerializedName("notificationIds")
    public List<String> notificationIds;

    public DeleteNotificationsBatchRequest() { }

    public DeleteNotificationsBatchRequest(List<String> notificationIds) {
        this.notificationIds = notificationIds;
    }

    public List<String> getNotificationIds() { return notificationIds; }
}
