package com.example.myapplication.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class ForwardRequest {
    @SerializedName("targetConversationId")
    public String targetConversationId;

    public ForwardRequest() { }

    public ForwardRequest(String targetConversationId) {
        this.targetConversationId = targetConversationId;
    }

    public String getTargetConversationId() { return targetConversationId; }
}
