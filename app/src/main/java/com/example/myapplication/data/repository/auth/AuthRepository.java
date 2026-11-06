package com.example.myapplication.data.repository.auth;

import android.content.Context;
import com.example.myapplication.data.repository.BaseRepository;
import com.example.myapplication.data.remote.api.ApiService;
import com.example.myapplication.data.remote.dto.request.*;
import com.example.myapplication.data.remote.dto.response.*;
import java.util.List;
import okhttp3.MultipartBody;
import retrofit2.Call;

public final class AuthRepository extends BaseRepository {
    public AuthRepository(Context context) { super(context); }
    public AuthRepository(ApiService api) { super(api); }

    public Call<LoginResponse> login(LoginRequest request) {
        return api.login(request);
    }



    public Call<BaseResponse<CommonResponse>> register(RegisterRequest request) {
        return api.register(request);
    }

    public Call<BaseResponse<CommonResponse>> verifyRegisterOtp(VerifyOtpRequest request) {
        return api.verifyRegisterOtp(request);
    }

    public Call<BaseResponse<CommonResponse>> sendOtp(SendOtpRequest request) {
        return api.sendOtp(request);
    }

    public Call<BaseResponse<CommonResponse>> forgotPasswordSendOtp(SendOtpRequest request) {
        return api.forgotPasswordSendOtp(request);
    }

    public Call<BaseResponse<CommonResponse>> forgotPasswordVerifyOtp(VerifyOtpRequest request) {
        return api.forgotPasswordVerifyOtp(request);
    }

    public Call<BaseResponse<CommonResponse>> resetPassword(ResetPasswordRequest request) {
        return api.resetPassword(request);
    }

    public Call<LoginResponse> refreshToken(RefreshTokenRequest request) {
        return api.refreshToken(request);
    }

    public Call<BaseResponse<Object>> logout(LogoutRequest request) {
        return api.logout(request);
    }
}
