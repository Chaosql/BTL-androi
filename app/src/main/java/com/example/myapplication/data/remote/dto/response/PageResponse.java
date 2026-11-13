package com.example.myapplication.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class PageResponse<T> {
    @SerializedName(value = "content", alternate = {"items"})
    public List<T> content = new ArrayList<>();
    @SerializedName("meta")
    public PagingMeta meta;

    public static final class PagingMeta {
        public Integer pageNum, pageSize, totalPages;
        public Long totalElements;
    }

    @SerializedName("pageable")
    public PageableInfo pageable;
    @SerializedName("totalPages")
    public Integer totalPages = 0;
    @SerializedName("totalElements")
    public Long totalElements = 0L;
    @SerializedName("last")
    public Boolean last = true;
    @SerializedName("first")
    public Boolean first = true;
    @SerializedName("empty")
    public Boolean empty = true;

    public PageResponse() { }

    public PageResponse(List<T> content, PageableInfo pageable, Integer totalPages, Long totalElements, Boolean last, Boolean first, Boolean empty) {
        this.content = content;
        this.pageable = pageable;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.last = last;
        this.first = first;
        this.empty = empty;
    }

    public List<T> getContent() { return content; }

    public PageableInfo getPageable() { return pageable; }

    public Integer getTotalPages() { return totalPages; }

    public Long getTotalElements() { return totalElements; }

    public Boolean getLast() { return last; }

    public Boolean getFirst() { return first; }

    public Boolean getEmpty() { return empty; }
}
