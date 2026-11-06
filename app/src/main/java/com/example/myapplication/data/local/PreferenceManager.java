package com.example.myapplication.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.myapplication.data.remote.dto.response.LoginData;

public final class PreferenceManager {
    private final SharedPreferences preferences;

    public PreferenceManager(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences("studydate_session", Context.MODE_PRIVATE);
    }

    public String getAccessToken() { return preferences.getString("access_token", ""); }
    public String getRefreshToken() { return preferences.getString("refresh_token", ""); }
    public String getUserId() { return preferences.getString("user_id", ""); }
    public String getEmail() { return preferences.getString("email", ""); }
    public boolean isLogin() { return !getAccessToken().isEmpty() && !getUserId().isEmpty(); }

    public synchronized void saveLogin(LoginData data, String email) {
        if (data == null || data.accessToken == null || data.accessToken.isEmpty()
                || data.id == null || data.id.isEmpty()) throw new IllegalArgumentException("Invalid login data");
        preferences.edit().putString("access_token", data.accessToken)
                .putString("refresh_token", data.refreshToken == null ? "" : data.refreshToken)
                .putString("user_id", data.id).putString("email", email == null ? getEmail() : email).commit();
    }

    public synchronized void logout() { preferences.edit().clear().commit(); }
}
