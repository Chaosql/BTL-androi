package com.example.myapplication.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class ReplyRequest {
    @SerializedName("content")
    public String content;

    public ReplyRequest() { }

    public ReplyRequest(String content) {
        this.content = content;
    }

    public String getContent() { return content; }
}
