package com.example.myapplication.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class AttachmentDto {
    @SerializedName("id")
    public String id;
    @SerializedName("fileUrl")
    public String fileUrl;
    @SerializedName("fileName")
    public String fileName;
    @SerializedName("fileType")
    public String fileType;
    @SerializedName("fileSize")
    public Long fileSize = 0L;

    public AttachmentDto() { }

    public AttachmentDto(String id, String fileUrl, String fileName, String fileType, Long fileSize) {
        this.id = id;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
    }

    public String getId() { return id; }

    public String getFileUrl() { return fileUrl; }

    public String getFileName() { return fileName; }

    public String getFileType() { return fileType; }

    public Long getFileSize() { return fileSize; }
}
