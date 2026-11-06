package com.example.myapplication.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class RateBookingRequest {
    @SerializedName("score")
    public Integer score = 5;

    public RateBookingRequest() { }

    public RateBookingRequest(Integer score) {
        this.score = score;
    }

    public Integer getScore() { return score; }
}
