package com.example.myapplication.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class FriendDto {
    @SerializedName("requestId")
    public String requestId;
    @SerializedName("userId")
    public String userId;
    @SerializedName("firstName")
    public String firstName;
    @SerializedName("lastName")
    public String lastName;
    @SerializedName("avatar")
    public String avatar;
    @SerializedName("phone")
    public String phone;
    @SerializedName("status")
    public String status;
    @SerializedName("createdDate")
    public String createdDate;

    public FriendDto() { }

    public FriendDto(String requestId, String userId, String firstName, String lastName, String avatar, String phone, String status, String createdDate) {
        this.requestId = requestId;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatar = avatar;
        this.phone = phone;
        this.status = status;
        this.createdDate = createdDate;
    }

    public String getRequestId() { return requestId; }

    public String getUserId() { return userId; }

    public String getFirstName() { return firstName; }

    public String getLastName() { return lastName; }

    public String getAvatar() { return avatar; }

    public String getPhone() { return phone; }

    public String getStatus() { return status; }

    public String getCreatedDate() { return createdDate; }
}
