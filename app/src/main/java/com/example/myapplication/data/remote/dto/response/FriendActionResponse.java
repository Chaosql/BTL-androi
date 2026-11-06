package com.example.myapplication.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class FriendActionResponse {
    @SerializedName("status")
    public Boolean status = false;
    @SerializedName("message")
    public String message;

    public FriendActionResponse() { }

    public FriendActionResponse(Boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    public Boolean getStatus() { return status; }

    public String getMessage() { return message; }
}
