package com.example.myapplication.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class LoginResponse {
    @SerializedName("codeStatus")
    public Integer codeStatus;
    @SerializedName("message")
    public String message;
    @SerializedName("data")
    public LoginData data;
    @SerializedName("timestamp")
    public String timestamp;

    public LoginResponse() { }

    public LoginResponse(Integer codeStatus, String message, LoginData data, String timestamp) {
        this.codeStatus = codeStatus;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

    public Integer getCodeStatus() { return codeStatus; }

    public String getMessage() { return message; }

    public LoginData getData() { return data; }

    public String getTimestamp() { return timestamp; }
}
