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
                    String message = null;
                    if (response.errorBody() != null) {
                        try {
                            JsonObject body = JsonParser.parseString(response.errorBody().string()).getAsJsonObject();
                            message = errorMessage(body);
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
                    if (body.data instanceof java.util.Map) {
                        java.util.Map<?, ?> data = (java.util.Map<?, ?>) body.data;
                        if (Boolean.FALSE.equals(data.get("status"))) {
                            Object message = data.get("message");
                            callback.onError(new ApiError(422, message instanceof String ? (String) message : body.message)); return;
                        }
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
                if (request.isCanceled()) return;
                if (com.example.myapplication.BuildConfig.DEBUG)
                    android.util.Log.w("ApiRequests", "Request failed: " + error.getClass().getSimpleName());
                callback.onError(failure(error));
            }
        });
    }
    private static String errorMessage(JsonObject body) {
        String detail = detail(body.get("data"));
        if (detail == null) detail = detail(body.get("errors"));
        if (detail == null) detail = primitive(body.get("message"));
        if (detail == null) detail = primitive(body.get("error"));
        return detail;
    }
    private static String primitive(com.google.gson.JsonElement value) {
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()
                && !value.getAsString().trim().isEmpty() ? value.getAsString() : null;
    }
    private static String detail(com.google.gson.JsonElement value) {
        if (value == null || value.isJsonNull()) return null;
        String text = primitive(value);
        if (text != null) return text;
        java.util.LinkedHashSet<String> messages = new java.util.LinkedHashSet<>();
        if (value.isJsonObject()) {
            JsonObject object = value.getAsJsonObject();
            text = primitive(object.get("message"));
            if (text == null) text = primitive(object.get("error"));
            if (text != null) return text;
            for (java.util.Map.Entry<String, com.google.gson.JsonElement> field : object.entrySet()) {
                text = detail(field.getValue());
                if (text != null) messages.add(text);
            }
        } else if (value.isJsonArray()) {
            for (com.google.gson.JsonElement item : value.getAsJsonArray()) {
                text = detail(item);
                if (text != null) messages.add(text);
            }
        }
        return messages.isEmpty() ? null : String.join("\n", messages);
    }
    private static ApiError failure(Throwable error) {
        String message;
        if (error instanceof java.net.UnknownHostException)
            message = "Không phân giải được địa chỉ máy chủ. Vui lòng kiểm tra mạng hoặc DNS.";
        else if (error instanceof javax.net.ssl.SSLException)
            message = "Không thể xác thực kết nối HTTPS. Vui lòng kiểm tra ngày giờ thiết bị và chứng chỉ máy chủ.";
        else if (error instanceof java.net.SocketTimeoutException)
            message = "Máy chủ phản hồi quá lâu. Vui lòng thử lại sau.";
        else if (error instanceof java.net.ConnectException)
            message = "Không mở được kết nối tới máy chủ. Vui lòng kiểm tra mạng và trạng thái server.";
        else if (error instanceof com.google.gson.JsonParseException
                || error instanceof com.google.gson.stream.MalformedJsonException
                || error instanceof java.io.EOFException || error instanceof IllegalStateException)
            message = "Dữ liệu phản hồi từ máy chủ không hợp lệ. Vui lòng thử lại sau.";
        else message = "Không thể kết nối máy chủ. Vui lòng kiểm tra mạng và thử lại.";
        return new ApiError(0, message);
    }
    private ApiRequests() { }
}
