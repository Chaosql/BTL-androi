package com.example.myapplication.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class UserDto {
    @SerializedName("id")
    public String id;
    @SerializedName("firstName")
    public String firstName;
    @SerializedName("lastName")
    public String lastName;
    @SerializedName("avatar")
    public String avatar;
    @SerializedName("university")
    public String university;
    @SerializedName("majorName")
    public String majorName;
    @SerializedName("gender")
    public String gender;
    @SerializedName("dateOfBirth")
    public String dateOfBirth;
    @SerializedName("location")
    public String location;
    @SerializedName("trustScore")
    public Double trustScore;
    @SerializedName("buddyActive")
    public Boolean buddyActive;
    @SerializedName("statusTag")
    public String statusTag;
    @SerializedName("subjects")
    public List<SubjectDto> subjects;
    @SerializedName("email")
    public String email;
    @SerializedName("phone")
    public String phone;

    public UserDto() { }

    public UserDto(String id, String firstName, String lastName, String avatar, String university, String majorName, String gender, String dateOfBirth, String location, Double trustScore, Boolean buddyActive, String statusTag, List<SubjectDto> subjects, String email, String phone) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatar = avatar;
        this.university = university;
        this.majorName = majorName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.location = location;
        this.trustScore = trustScore;
        this.buddyActive = buddyActive;
        this.statusTag = statusTag;
        this.subjects = subjects;
        this.email = email;
        this.phone = phone;
    }

    public String getId() { return id; }

    public String getFirstName() { return firstName; }

    public String getLastName() { return lastName; }

    public String getAvatar() { return avatar; }

    public String getUniversity() { return university; }

    public String getMajorName() { return majorName; }

    public String getGender() { return gender; }

    public String getDateOfBirth() { return dateOfBirth; }

    public String getLocation() { return location; }

    public Double getTrustScore() { return trustScore; }

    public Boolean getBuddyActive() { return buddyActive; }

    public String getStatusTag() { return statusTag; }

    public List<SubjectDto> getSubjects() { return subjects; }

    public String getEmail() { return email; }

    public String getPhone() { return phone; }
}
