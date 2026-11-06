package com.example.myapplication.data.remote.interceptor;

import com.example.myapplication.data.local.PreferenceManager;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class AuthInterceptor implements Interceptor {
    private final PreferenceManager preferences;
    public AuthInterceptor(PreferenceManager preferences) { this.preferences = preferences; }

    @Override public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder request = original.newBuilder().header("Accept-Language", "vi");
        String token = preferences.getAccessToken();
        if (!original.url().encodedPath().contains("/auth/") && !token.isEmpty())
            request.header("Authorization", "Bearer " + token);
        return chain.proceed(request.build());
    }
}
