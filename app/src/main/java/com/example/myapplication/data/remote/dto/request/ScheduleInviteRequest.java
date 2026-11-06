package com.example.myapplication.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class ScheduleInviteRequest {
    @SerializedName("subject")
    public String subject;
    @SerializedName("location")
    public String location;
    @SerializedName("scheduledAt")
    public String scheduledAt;
    @SerializedName("duration")
    public Integer duration = 0;
    @SerializedName("note")
    public String note;

    public ScheduleInviteRequest() { }

    public ScheduleInviteRequest(String subject, String location, String scheduledAt, Integer duration, String note) {
        this.subject = subject;
        this.location = location;
        this.scheduledAt = scheduledAt;
        this.duration = duration;
        this.note = note;
    }

    public String getSubject() { return subject; }

    public String getLocation() { return location; }

    public String getScheduledAt() { return scheduledAt; }

    public Integer getDuration() { return duration; }

    public String getNote() { return note; }
}
