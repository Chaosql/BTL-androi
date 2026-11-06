package com.example.myapplication.data.remote.network;

public final class ApiError {
    public final int code;
    public final String message;
    public ApiError(int code, String message) {
        this.code = code;
        this.message = message == null || message.trim().isEmpty() ? "Không thể thực hiện yêu cầu. Vui lòng thử lại." : message;
    }
}
