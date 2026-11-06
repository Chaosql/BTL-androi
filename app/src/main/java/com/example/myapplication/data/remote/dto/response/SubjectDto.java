package com.example.myapplication.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class SubjectDto {
    @SerializedName("id")
    public String id;
    @SerializedName("name")
    public String name;
    @SerializedName("type")
    public String type;
    @SerializedName("createdDate")
    public String createdDate;
    @SerializedName("lastModifiedDate")
    public String lastModifiedDate;

    public SubjectDto() { }

    public SubjectDto(String id, String name, String type, String createdDate, String lastModifiedDate) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getId() { return id; }

    public String getName() { return name; }

    public String getType() { return type; }

    public String getCreatedDate() { return createdDate; }

    public String getLastModifiedDate() { return lastModifiedDate; }
}
