package com.example.myapplication.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;

public final class AdminUserDto {
    public String id, firstName, lastName, email, phone, university, majorName, role;
    @SerializedName(value = "userstatus", alternate = {"status", "userStatus"})
    public String status;
}
