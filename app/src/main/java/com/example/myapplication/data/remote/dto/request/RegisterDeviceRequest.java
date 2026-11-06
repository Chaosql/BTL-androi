package com.example.myapplication.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class RegisterDeviceRequest {
    @SerializedName("fcmToken")
    public String fcmToken;
    @SerializedName("deviceType")
    public String deviceType = "ANDROID";
    @SerializedName("deviceName")
    public String deviceName;

    public RegisterDeviceRequest() { }

    public RegisterDeviceRequest(String fcmToken, String deviceType, String deviceName) {
        this.fcmToken = fcmToken;
        this.deviceType = deviceType;
        this.deviceName = deviceName;
    }

    public String getFcmToken() { return fcmToken; }

    public String getDeviceType() { return deviceType; }

    public String getDeviceName() { return deviceName; }
}
