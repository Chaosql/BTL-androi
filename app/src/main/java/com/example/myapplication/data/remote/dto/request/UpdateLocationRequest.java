package com.example.myapplication.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class UpdateLocationRequest {
    @SerializedName("latitude")
    public Double latitude;
    @SerializedName("longitude")
    public Double longitude;

    public UpdateLocationRequest() { }

    public UpdateLocationRequest(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() { return latitude; }

    public Double getLongitude() { return longitude; }
}
