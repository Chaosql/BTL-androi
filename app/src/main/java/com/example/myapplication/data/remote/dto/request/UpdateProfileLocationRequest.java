package com.example.myapplication.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class UpdateProfileLocationRequest {
    @SerializedName("location")
    public String location;

    public UpdateProfileLocationRequest() { }

    public UpdateProfileLocationRequest(String location) {
        this.location = location;
    }

    public String getLocation() { return location; }
}
