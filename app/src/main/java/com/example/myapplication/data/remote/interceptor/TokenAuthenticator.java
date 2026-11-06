package com.example.myapplication.data.remote.interceptor;

import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.api.ApiService;
import com.example.myapplication.data.remote.dto.request.RefreshTokenRequest;
import com.example.myapplication.data.remote.dto.response.LoginData;
import com.example.myapplication.data.remote.dto.response.LoginResponse;
import java.io.IOException;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public final class TokenAuthenticator implements Authenticator {
    private final PreferenceManager preferences;
    private final ApiService publicApi;
    public TokenAuthenticator(PreferenceManager preferences, ApiService publicApi) {
        this.preferences = preferences;
        this.publicApi = publicApi;
    }

    @Override public synchronized Request authenticate(Route route, Response response) throws IOException {
        if (response.request().url().encodedPath().contains("/auth/") || response.priorResponse() != null) return null;
        String token = preferences.getAccessToken();
        if (token.isEmpty()) return null;
        if (!("Bearer " + token).equals(response.request().header("Authorization")))
            return response.request().newBuilder().header("Authorization", "Bearer " + token).build();
        String refreshToken = preferences.getRefreshToken();
        if (refreshToken.isEmpty()) { preferences.logout(); return null; }
        retrofit2.Response<LoginResponse> refresh;
        try {
            refresh = publicApi.refreshToken(new RefreshTokenRequest(refreshToken)).execute();
        } catch (IOException error) {
            return null;
        }
        if (!refreshToken.equals(preferences.getRefreshToken())) return null;
        LoginData data = refresh.body() == null ? null : refresh.body().data;
        Integer status = refresh.body() == null ? null : refresh.body().codeStatus;
        if (!refresh.isSuccessful() || (status != null && status >= 400) || data == null || data.accessToken == null || data.accessToken.isEmpty()) {
            if (refresh.code() == 401 || refresh.code() == 403 || Integer.valueOf(401).equals(status) || Integer.valueOf(403).equals(status)) preferences.logout();
            if (refresh.errorBody() != null) refresh.errorBody().close();
            return null;
        }
        if (data.id == null || data.id.isEmpty()) data.id = preferences.getUserId();
        if (data.refreshToken == null || data.refreshToken.isEmpty()) data.refreshToken = refreshToken;
        if (data.authorities == null) data.authorities = java.util.Collections.singletonList(
                new com.example.myapplication.data.remote.dto.response.Authority(preferences.isAdmin() ? "ADMIN" : "USER"));
        preferences.saveLogin(data, preferences.getEmail());
        return response.request().newBuilder().header("Authorization", "Bearer " + data.accessToken).build();
    }
}
