package com.example.myapplication.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class User {
    @SerializedName("id")
    public String id;
    @SerializedName("fullName")
    public String fullName;
    @SerializedName("avatar")
    public String avatar;
    @SerializedName("email")
    public String email = "";
    @SerializedName("online")
    public Boolean online = false;
    @SerializedName("lastSeen")
    public String lastSeen;

    public User() { }

    public User(String id, String fullName, String avatar, String email, Boolean online, String lastSeen) {
        this.id = id;
        this.fullName = fullName;
        this.avatar = avatar;
        this.email = email;
        this.online = online;
        this.lastSeen = lastSeen;
    }

    public String getId() { return id; }

    public String getFullName() { return fullName; }

    public String getAvatar() { return avatar; }

    public String getEmail() { return email; }

    public Boolean getOnline() { return online; }

    public String getLastSeen() { return lastSeen; }
}
