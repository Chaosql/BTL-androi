package com.example.myapplication.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class NearbyUserResponse {
    @SerializedName("userId")
    public String userId;
    @SerializedName("firstName")
    public String firstName;
    @SerializedName("lastName")
    public String lastName;
    @SerializedName("avatar")
    public String avatar;
    @SerializedName("university")
    public String university;
    @SerializedName("majorName")
    public String majorName;
    @SerializedName("statusTag")
    public String statusTag;
    @SerializedName("latitude")
    public Double latitude;
    @SerializedName("longitude")
    public Double longitude;
    @SerializedName("distanceKm")
    public Double distanceKm;

    public NearbyUserResponse() { }

    public NearbyUserResponse(String userId, String firstName, String lastName, String avatar, String university, String majorName, String statusTag, Double latitude, Double longitude, Double distanceKm) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatar = avatar;
        this.university = university;
        this.majorName = majorName;
        this.statusTag = statusTag;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceKm = distanceKm;
    }

    public String getUserId() { return userId; }

    public String getFirstName() { return firstName; }

    public String getLastName() { return lastName; }

    public String getAvatar() { return avatar; }

    public String getUniversity() { return university; }

    public String getMajorName() { return majorName; }

    public String getStatusTag() { return statusTag; }

    public Double getLatitude() { return latitude; }

    public Double getLongitude() { return longitude; }

    public Double getDistanceKm() { return distanceKm; }
}
