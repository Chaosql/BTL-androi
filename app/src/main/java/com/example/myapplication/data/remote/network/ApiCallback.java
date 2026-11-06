package com.example.myapplication.data.remote.network;

public interface ApiCallback<T> {
    void onSuccess(T result);
    void onError(ApiError error);
}
