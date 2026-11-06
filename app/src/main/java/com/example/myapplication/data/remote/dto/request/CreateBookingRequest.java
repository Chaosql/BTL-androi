package com.example.myapplication.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class CreateBookingRequest {
    @SerializedName("subject")
    public String subject;
    @SerializedName("scheduledAt")
    public String scheduledAt;
    @SerializedName(value = "durationMinutes", alternate = {"duration"})
    public Integer durationMinutes = 30;
    @SerializedName("locationName")
    public String locationName;
    @SerializedName("locationAddress")
    public String locationAddress;
    @SerializedName("locationDistrict")
    public String locationDistrict;
    @SerializedName("locationCity")
    public String locationCity;
    @SerializedName("note")
    public String note;
    @SerializedName("isRecurring")
    public Boolean isRecurring = false;
    @SerializedName("reminderMinutesBefore")
    public Integer reminderMinutesBefore = 0;

    public CreateBookingRequest() { }

    public CreateBookingRequest(String subject, String scheduledAt, Integer durationMinutes, String locationName, String locationAddress, String locationDistrict, String locationCity, String note, Boolean isRecurring, Integer reminderMinutesBefore) {
        this.subject = subject;
        this.scheduledAt = scheduledAt;
        this.durationMinutes = durationMinutes;
        this.locationName = locationName;
        this.locationAddress = locationAddress;
        this.locationDistrict = locationDistrict;
        this.locationCity = locationCity;
        this.note = note;
        this.isRecurring = isRecurring;
        this.reminderMinutesBefore = reminderMinutesBefore;
    }

    public String getSubject() { return subject; }

    public String getScheduledAt() { return scheduledAt; }

    public Integer getDurationMinutes() { return durationMinutes; }

    public String getLocationName() { return locationName; }

    public String getLocationAddress() { return locationAddress; }

    public String getLocationDistrict() { return locationDistrict; }

    public String getLocationCity() { return locationCity; }

    public String getNote() { return note; }

    public Boolean getIsRecurring() { return isRecurring; }

    public Integer getReminderMinutesBefore() { return reminderMinutesBefore; }
}
