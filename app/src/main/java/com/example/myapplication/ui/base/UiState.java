package com.example.myapplication.ui.base;

import com.example.myapplication.data.remote.network.ApiError;

public final class UiState<T> {
    public enum Status { LOADING, SUCCESS, ERROR, CANCELLED }
    public final Status status;
    public final T data;
    public final ApiError error;

    private UiState(Status status, T data, ApiError error) {
        this.status = status; this.data = data; this.error = error;
    }
    public static <T> UiState<T> loading() { return new UiState<>(Status.LOADING, null, null); }
    public static <T> UiState<T> success(T data) { return new UiState<>(Status.SUCCESS, data, null); }
    public static <T> UiState<T> error(ApiError error) { return new UiState<>(Status.ERROR, null, error); }
    public static <T> UiState<T> cancelled() { return new UiState<>(Status.CANCELLED, null, null); }
    public boolean isFinished() { return status != Status.LOADING; }
}
