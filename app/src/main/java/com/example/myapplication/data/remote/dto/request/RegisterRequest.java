package com.example.myapplication.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class RegisterRequest {
    @SerializedName("email")
    public String email;
    @SerializedName("password")
    public String password;
    @SerializedName("confirmPassword")
    public String confirmPassword;
    @SerializedName("firstName")
    public String firstName;
    @SerializedName("lastName")
    public String lastName;
    @SerializedName("gender")
    public String gender;
    @SerializedName("dateOfBirth")
    public String dateOfBirth;

    public RegisterRequest() { }

    public RegisterRequest(String email, String password, String confirmPassword, String firstName, String lastName, String gender, String dateOfBirth) {
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
    }

    public String getEmail() { return email; }

    public String getPassword() { return password; }

    public String getConfirmPassword() { return confirmPassword; }

    public String getFirstName() { return firstName; }

    public String getLastName() { return lastName; }

    public String getGender() { return gender; }

    public String getDateOfBirth() { return dateOfBirth; }
}
