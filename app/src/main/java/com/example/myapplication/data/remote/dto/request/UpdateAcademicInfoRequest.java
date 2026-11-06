package com.example.myapplication.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class UpdateAcademicInfoRequest {
    @SerializedName("university")
    public String university;
    @SerializedName("majorName")
    public String majorName;

    public UpdateAcademicInfoRequest() { }

    public UpdateAcademicInfoRequest(String university, String majorName) {
        this.university = university;
        this.majorName = majorName;
    }

    public String getUniversity() { return university; }

    public String getMajorName() { return majorName; }
}
