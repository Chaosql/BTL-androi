package com.example.myapplication.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class BookingParticipantDto {
    @SerializedName("id")
    public String id;
    @SerializedName("userId")
    public String userId;
    @SerializedName("status")
    public String status;
    @SerializedName("reminderMinutesBefore")
    public Integer reminderMinutesBefore;
    @SerializedName("respondedAt")
    public String respondedAt;

    public BookingParticipantDto() { }

    public BookingParticipantDto(String id, String userId, String status, Integer reminderMinutesBefore, String respondedAt) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.reminderMinutesBefore = reminderMinutesBefore;
        this.respondedAt = respondedAt;
    }

    public String getId() { return id; }

    public String getUserId() { return userId; }

    public String getStatus() { return status; }

    public Integer getReminderMinutesBefore() { return reminderMinutesBefore; }

    public String getRespondedAt() { return respondedAt; }
}
