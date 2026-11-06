package com.example.myapplication.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Reaction {
    @SerializedName("emoji")
    public String emoji;
    @SerializedName("count")
    public Integer count;
    @SerializedName("userIds")
    public List<String> userIds = new ArrayList<>();
    @SerializedName("userNames")
    public List<String> userNames = new ArrayList<>();

    public Reaction() { }

    public Reaction(String emoji, Integer count, List<String> userIds, List<String> userNames) {
        this.emoji = emoji;
        this.count = count;
        this.userIds = userIds;
        this.userNames = userNames;
    }

    public String getEmoji() { return emoji; }

    public Integer getCount() { return count; }

    public List<String> getUserIds() { return userIds; }

    public List<String> getUserNames() { return userNames; }
}
