package com.example.myapplication.data.remote.network;

import android.content.Context;
import com.example.myapplication.BuildConfig;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.api.ApiService;
import com.example.myapplication.data.remote.interceptor.AuthInterceptor;
import com.example.myapplication.data.remote.interceptor.TokenAuthenticator;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {
    private static volatile ApiService instance;

    @androidx.annotation.VisibleForTesting
    public static synchronized void setApiServiceForTests(ApiService service) {
        if (!BuildConfig.DEBUG) throw new IllegalStateException("Debug only");
        instance = service;
    }

    public static ApiService getApiService(Context context) {
        if (instance == null) {
            synchronized (RetrofitClient.class) {
                if (instance == null) instance = create(BuildConfig.API_BASE_URL, new PreferenceManager(context));
            }
        }
        return instance;
    }

    public static ApiService create(String baseUrl, PreferenceManager preferences) {
        if (!baseUrl.endsWith("/")) throw new IllegalArgumentException("API base URL must end with /");
        OkHttpClient publicClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).build();
        ApiService publicApi = retrofit(baseUrl, publicClient);
        OkHttpClient client = publicClient.newBuilder().addInterceptor(new AuthInterceptor(preferences))
                .authenticator(new TokenAuthenticator(preferences, publicApi)).build();
        return retrofit(baseUrl, client);
    }

    private static ApiService retrofit(String baseUrl, OkHttpClient client) {
        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        return new Retrofit.Builder().baseUrl(baseUrl).client(client)
                .callbackExecutor(command -> mainHandler.post(command))
                .addConverterFactory(GsonConverterFactory.create()).validateEagerly(true).build().create(ApiService.class);
    }

    private RetrofitClient() { }
}
