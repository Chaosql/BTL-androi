package com.example.myapplication.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class ResetPasswordRequest {
    @SerializedName("email")
    public String email;
    @SerializedName("newPassword")
    public String newPassword;
    @SerializedName("confirmPassword")
    public String confirmPassword;

    public ResetPasswordRequest() { }

    public ResetPasswordRequest(String email, String newPassword, String confirmPassword) {
        this.email = email;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    public String getEmail() { return email; }

    public String getNewPassword() { return newPassword; }

    public String getConfirmPassword() { return confirmPassword; }
}
