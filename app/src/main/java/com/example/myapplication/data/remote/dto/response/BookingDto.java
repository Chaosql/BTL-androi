package com.example.myapplication.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class BookingDto {
    @SerializedName("id")
    public String id;
    @SerializedName("status")
    public String status;
    @SerializedName("subject")
    public String subject;
    @SerializedName("scheduledAt")
    public String scheduledAt;
    @SerializedName(value = "durationMinutes", alternate = {"duration"})
    public Integer durationMinutes;
    @SerializedName(value = "locationName", alternate = {"location"})
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
    public Boolean isRecurring;
    @SerializedName("cancelledBy")
    public String cancelledBy;
    @SerializedName("cancelReason")
    public String cancelReason;
    @SerializedName("reminderMinutesBefore")
    public Integer reminderMinutesBefore;
    @SerializedName("creatorId")
    public String creatorId;
    @SerializedName("creatorName")
    public String creatorName;
    @SerializedName("creatorAvatar")
    public String creatorAvatar;
    @SerializedName("partnerId")
    public String partnerId;
    @SerializedName("partnerName")
    public String partnerName;
    @SerializedName("partnerAvatar")
    public String partnerAvatar;
    @SerializedName("participantStatus")
    public String participantStatus;
    @SerializedName("hasRated")
    public Boolean hasRated;
    @SerializedName("myRating")
    public Integer myRating;
    @SerializedName("messageId")
    public String messageId;
    @SerializedName("conversationId")
    public String conversationId;
    @SerializedName("createdDate")
    public String createdDate;
    @SerializedName("lastModifiedDate")
    public String lastModifiedDate;
    @SerializedName("participants")
    public List<BookingParticipantDto> participants;

    public BookingDto() { }

    public BookingDto(String id, String status, String subject, String scheduledAt, Integer durationMinutes, String locationName, String locationAddress, String locationDistrict, String locationCity, String note, Boolean isRecurring, String cancelledBy, String cancelReason, Integer reminderMinutesBefore, String creatorId, String creatorName, String creatorAvatar, String partnerId, String partnerName, String partnerAvatar, String participantStatus, Boolean hasRated, Integer myRating, String messageId, String conversationId, String createdDate, String lastModifiedDate, List<BookingParticipantDto> participants) {
        this.id = id;
        this.status = status;
        this.subject = subject;
        this.scheduledAt = scheduledAt;
        this.durationMinutes = durationMinutes;
        this.locationName = locationName;
        this.locationAddress = locationAddress;
        this.locationDistrict = locationDistrict;
        this.locationCity = locationCity;
        this.note = note;
        this.isRecurring = isRecurring;
        this.cancelledBy = cancelledBy;
        this.cancelReason = cancelReason;
        this.reminderMinutesBefore = reminderMinutesBefore;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.creatorAvatar = creatorAvatar;
        this.partnerId = partnerId;
        this.partnerName = partnerName;
        this.partnerAvatar = partnerAvatar;
        this.participantStatus = participantStatus;
        this.hasRated = hasRated;
        this.myRating = myRating;
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
        this.participants = participants;
    }

    public String getId() { return id; }

    public String getStatus() { return status; }

    public String getSubject() { return subject; }

    public String getScheduledAt() { return scheduledAt; }

    public Integer getDurationMinutes() { return durationMinutes; }

    public String getLocationName() { return locationName; }

    public String getLocationAddress() { return locationAddress; }

    public String getLocationDistrict() { return locationDistrict; }

    public String getLocationCity() { return locationCity; }

    public String getNote() { return note; }

    public Boolean getIsRecurring() { return isRecurring; }

    public String getCancelledBy() { return cancelledBy; }

    public String getCancelReason() { return cancelReason; }

    public Integer getReminderMinutesBefore() { return reminderMinutesBefore; }

    public String getCreatorId() { return creatorId; }

    public String getCreatorName() { return creatorName; }

    public String getCreatorAvatar() { return creatorAvatar; }

    public String getPartnerId() { return partnerId; }

    public String getPartnerName() { return partnerName; }

    public String getPartnerAvatar() { return partnerAvatar; }

    public String getParticipantStatus() { return participantStatus; }

    public Boolean getHasRated() { return hasRated; }

    public Integer getMyRating() { return myRating; }

    public String getMessageId() { return messageId; }

    public String getConversationId() { return conversationId; }

    public String getCreatedDate() { return createdDate; }

    public String getLastModifiedDate() { return lastModifiedDate; }

    public List<BookingParticipantDto> getParticipants() { return participants; }
}
