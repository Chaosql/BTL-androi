package com.example.myapplication.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class UpdatePersonalInfoRequest {
    @SerializedName("firstName")
    public String firstName;
    @SerializedName("lastName")
    public String lastName;
    @SerializedName("gender")
    public String gender;
    @SerializedName("dateOfBirth")
    public String dateOfBirth;
    @SerializedName("phone")
    public String phone;
    @SerializedName("email")
    public String email;

    public UpdatePersonalInfoRequest() { }

    public UpdatePersonalInfoRequest(String firstName, String lastName, String gender, String dateOfBirth, String phone, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
        this.email = email;
    }

    public String getFirstName() { return firstName; }

    public String getLastName() { return lastName; }

    public String getGender() { return gender; }

    public String getDateOfBirth() { return dateOfBirth; }

    public String getPhone() { return phone; }

    public String getEmail() { return email; }
}
