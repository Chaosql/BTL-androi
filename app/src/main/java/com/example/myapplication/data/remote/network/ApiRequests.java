package com.example.myapplication.data.remote.network;

import com.example.myapplication.data.remote.dto.response.BaseResponse;
import com.example.myapplication.data.remote.dto.response.CommonResponse;
import com.example.myapplication.data.remote.dto.response.LoginResponse;
import com.example.myapplication.data.remote.dto.response.ActionStatusDto;
import com.example.myapplication.data.remote.dto.response.FriendActionResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class ApiRequests {
    public static <T> void enqueue(Call<T> call, ApiCallback<T> callback) {
        call.enqueue(new Callback<T>() {
            @Override public void onResponse(Call<T> request, Response<T> response) {
                if (request.isCanceled()) { if (response.errorBody() != null) response.errorBody().close(); return; }
                if (!response.isSuccessful()) {
                    String message = "Yêu cầu thất bại (" + response.code() + ").";
                    if (response.errorBody() != null) {
                        try {
                            JsonObject body = JsonParser.parseString(response.errorBody().string()).getAsJsonObject();
                            if (body.has("message") && !body.get("message").isJsonNull()) message = body.get("message").getAsString();
                        } catch (Exception ignored) { }
                        finally { response.errorBody().close(); }
                    }
                    callback.onError(new ApiError(response.code(), message));
                    return;
                }
                T result = response.body();
                if (result == null) { callback.onError(new ApiError(response.code(), "Máy chủ trả về dữ liệu trống.")); return; }
                if (result instanceof BaseResponse) {
                    BaseResponse<?> body = (BaseResponse<?>) result;
                    if (body.codeStatus != null && body.codeStatus >= 400) {
                        callback.onError(new ApiError(body.codeStatus, body.message)); return;
                    }
                    if (body.data instanceof CommonResponse && Boolean.FALSE.equals(((CommonResponse) body.data).status)) {
                        callback.onError(new ApiError(422, ((CommonResponse) body.data).message)); return;
                    }
                    if (body.data instanceof ActionStatusDto && Boolean.FALSE.equals(((ActionStatusDto) body.data).status)) {
                        callback.onError(new ApiError(422, ((ActionStatusDto) body.data).message)); return;
                    }
                    if (body.data instanceof FriendActionResponse && Boolean.FALSE.equals(((FriendActionResponse) body.data).status)) {
                        callback.onError(new ApiError(422, ((FriendActionResponse) body.data).message)); return;
                    }
                }
                if (result instanceof LoginResponse) {
                    LoginResponse body = (LoginResponse) result;
                    if (body.codeStatus != null && body.codeStatus >= 400) {
                        callback.onError(new ApiError(body.codeStatus, body.message)); return;
                    }
                }
                callback.onSuccess(result);
            }
            @Override public void onFailure(Call<T> request, Throwable error) {
                if (!request.isCanceled()) callback.onError(new ApiError(0, "Không thể kết nối máy chủ. Vui lòng kiểm tra mạng và thử lại."));
            }
        });
    }
    private ApiRequests() { }
}
