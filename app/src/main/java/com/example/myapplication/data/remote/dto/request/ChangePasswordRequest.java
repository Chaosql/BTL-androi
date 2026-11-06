package com.example.myapplication.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class ChangePasswordRequest {
    @SerializedName("oldPassword")
    public String oldPassword;
    @SerializedName("newPassword")
    public String newPassword;
    @SerializedName("confirmPassword")
    public String confirmPassword;

    public ChangePasswordRequest() { }

    public ChangePasswordRequest(String oldPassword, String newPassword, String confirmPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    public String getOldPassword() { return oldPassword; }

    public String getNewPassword() { return newPassword; }

    public String getConfirmPassword() { return confirmPassword; }
}
