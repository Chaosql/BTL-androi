package com.example.myapplication.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class UpdateGroupRequest {
    @SerializedName("groupName")
    public String groupName;
    @SerializedName("groupAvatar")
    public String groupAvatar;

    public UpdateGroupRequest() { }

    public UpdateGroupRequest(String groupName, String groupAvatar) {
        this.groupName = groupName;
        this.groupAvatar = groupAvatar;
    }

    public String getGroupName() { return groupName; }

    public String getGroupAvatar() { return groupAvatar; }
}
