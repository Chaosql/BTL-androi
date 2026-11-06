package com.example.myapplication.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class AddSubjectRequest {
    @SerializedName("name")
    public String name;
    @SerializedName("type")
    public String type;

    public AddSubjectRequest() { }

    public AddSubjectRequest(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() { return name; }

    public String getType() { return type; }
}
