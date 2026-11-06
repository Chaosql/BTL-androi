package com.example.myapplication.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class UserSearchDto {
    @SerializedName("id")
    public String id;
    @SerializedName("firstName")
    public String firstName;
    @SerializedName("lastName")
    public String lastName;
    @SerializedName("avatar")
    public String avatar;
    @SerializedName("phone")
    public String phone;
    @SerializedName("friendshipStatus")
    public String friendshipStatus;

    public UserSearchDto() { }

    public UserSearchDto(String id, String firstName, String lastName, String avatar, String phone, String friendshipStatus) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatar = avatar;
        this.phone = phone;
        this.friendshipStatus = friendshipStatus;
    }

    public String getId() { return id; }

    public String getFirstName() { return firstName; }

    public String getLastName() { return lastName; }

    public String getAvatar() { return avatar; }

    public String getPhone() { return phone; }

    public String getFriendshipStatus() { return friendshipStatus; }
}
