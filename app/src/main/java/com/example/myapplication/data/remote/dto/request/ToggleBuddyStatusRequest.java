package com.example.myapplication.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class ToggleBuddyStatusRequest {
    @SerializedName("buddyActive")
    public Boolean buddyActive;

    public ToggleBuddyStatusRequest() { }

    public ToggleBuddyStatusRequest(Boolean buddyActive) {
        this.buddyActive = buddyActive;
    }

    public Boolean getBuddyActive() { return buddyActive; }
}
