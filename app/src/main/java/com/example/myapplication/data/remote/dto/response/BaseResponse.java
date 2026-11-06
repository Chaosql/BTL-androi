package com.example.myapplication.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class BaseResponse<T> {
    @SerializedName("codeStatus")
    public Integer codeStatus;
    @SerializedName("message")
    public String message;
    @SerializedName("data")
    public T data;
    @SerializedName("timestamp")
    public String timestamp;

    public BaseResponse() { }

    public BaseResponse(Integer codeStatus, String message, T data, String timestamp) {
        this.codeStatus = codeStatus;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

    public Integer getCodeStatus() { return codeStatus; }

    public String getMessage() { return message; }

    public T getData() { return data; }

    public String getTimestamp() { return timestamp; }
}
