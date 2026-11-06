package com.example.myapplication.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class WeeklyBookingResponse {
    @SerializedName("weekStart")
    public String weekStart;
    @SerializedName("weekEnd")
    public String weekEnd;
    @SerializedName("daySchedules")
    public Map<String, List<BookingDto>> daySchedules;

    public WeeklyBookingResponse() { }

    public WeeklyBookingResponse(String weekStart, String weekEnd, Map<String, List<BookingDto>> daySchedules) {
        this.weekStart = weekStart;
        this.weekEnd = weekEnd;
        this.daySchedules = daySchedules;
    }

    public String getWeekStart() { return weekStart; }

    public String getWeekEnd() { return weekEnd; }

    public Map<String, List<BookingDto>> getDaySchedules() { return daySchedules; }
}
