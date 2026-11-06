package com.example.myapplication.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Attachment {
    @SerializedName("id")
    public String id;
    @SerializedName("url")
    public String url;
    @SerializedName("thumbnail")
    public String thumbnail;
    @SerializedName("fileName")
    public String fileName;
    @SerializedName("fileSize")
    public Long fileSize;
    @SerializedName("mimeType")
    public String mimeType;

    public Attachment() { }

    public Attachment(String id, String url, String thumbnail, String fileName, Long fileSize, String mimeType) {
        this.id = id;
        this.url = url;
        this.thumbnail = thumbnail;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
    }

    public String getId() { return id; }

    public String getUrl() { return url; }

    public String getThumbnail() { return thumbnail; }

    public String getFileName() { return fileName; }

    public Long getFileSize() { return fileSize; }

    public String getMimeType() { return mimeType; }
}
