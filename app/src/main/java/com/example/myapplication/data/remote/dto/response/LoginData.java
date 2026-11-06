package com.example.myapplication.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.example.myapplication.data.model.*;

public class LoginData {
    @SerializedName("tokenType")
    public String tokenType;
    @SerializedName("accessToken")
    public String accessToken;
    @SerializedName("refreshToken")
    public String refreshToken;
    @SerializedName("id")
    public String id;
    @SerializedName("authorities")
    public List<Authority> authorities;

    public LoginData() { }

    public LoginData(String tokenType, String accessToken, String refreshToken, String id, List<Authority> authorities) {
        this.tokenType = tokenType;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.id = id;
        this.authorities = authorities;
    }

    public String getTokenType() { return tokenType; }

    public String getAccessToken() { return accessToken; }

    public String getRefreshToken() { return refreshToken; }

    public String getId() { return id; }

    public List<Authority> getAuthorities() { return authorities; }
}
