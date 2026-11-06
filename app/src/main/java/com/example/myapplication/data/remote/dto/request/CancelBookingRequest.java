package com.example.myapplication.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class CancelBookingRequest {
    @SerializedName("cancelReason")
    public String cancelReason;

    public CancelBookingRequest() { }

    public CancelBookingRequest(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public String getCancelReason() { return cancelReason; }
}
