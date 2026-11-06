package com.example.myapplication.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class VerifyOtpRequest {
    @SerializedName("email")
    public String email;
    @SerializedName("otpCode")
    public String otpCode;

    public VerifyOtpRequest() { }

    public VerifyOtpRequest(String email, String otpCode) {
        this.email = email;
        this.otpCode = otpCode;
    }

    public String getEmail() { return email; }

    public String getOtpCode() { return otpCode; }
}
