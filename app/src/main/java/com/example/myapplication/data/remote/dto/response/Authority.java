package com.example.myapplication.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class Authority {
    @SerializedName("authority")
    public String authority;

    public Authority() { }

    public Authority(String authority) {
        this.authority = authority;
    }

    public String getAuthority() { return authority; }
}
