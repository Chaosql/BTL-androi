package com.example.myapplication.data.repository;

import android.content.Context;
import com.example.myapplication.data.remote.api.ApiService;
import com.example.myapplication.data.remote.network.RetrofitClient;

public abstract class BaseRepository {
    protected final ApiService api;
    protected BaseRepository(Context context) { api = RetrofitClient.getApiService(context); }
    protected BaseRepository(ApiService api) { this.api = api; }
}
