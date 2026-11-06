package com.example.myapplication.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class CreateGroupRequest {
    @SerializedName("groupName")
    public String groupName;
    @SerializedName("groupAvatar")
    public String groupAvatar;
    @SerializedName("memberIds")
    public List<String> memberIds;

    public CreateGroupRequest() { }

    public CreateGroupRequest(String groupName, String groupAvatar, List<String> memberIds) {
        this.groupName = groupName;
        this.groupAvatar = groupAvatar;
        this.memberIds = memberIds;
    }

    public String getGroupName() { return groupName; }

    public String getGroupAvatar() { return groupAvatar; }

    public List<String> getMemberIds() { return memberIds; }
}
