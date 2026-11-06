package com.example.myapplication.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class PageableInfo {
    @SerializedName("pageNumber")
    public Integer pageNumber = 0;
    @SerializedName("pageSize")
    public Integer pageSize = 20;

    public PageableInfo() { }

    public PageableInfo(Integer pageNumber, Integer pageSize) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    public Integer getPageNumber() { return pageNumber; }

    public Integer getPageSize() { return pageSize; }
}
