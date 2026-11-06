package com.example.myapplication.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class UpdateStatusTagRequest {
    @SerializedName("statusTag")
    public String statusTag;

    public UpdateStatusTagRequest() { }

    public UpdateStatusTagRequest(String statusTag) {
        this.statusTag = statusTag;
    }

    public String getStatusTag() { return statusTag; }
}
