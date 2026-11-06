package com.example.myapplication.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class SendOtpRequest {
    @SerializedName("email")
    public String email;

    public SendOtpRequest() { }

    public SendOtpRequest(String email) {
        this.email = email;
    }

    public String getEmail() { return email; }
}
