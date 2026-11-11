package com.example.myapplication.ui.auth;

import android.app.Application;
import com.example.myapplication.ui.base.BaseViewModel;
import com.example.myapplication.ui.base.UiOperation;
import com.example.myapplication.data.repository.auth.AuthRepository;
import com.example.myapplication.data.remote.dto.request.*;
import com.example.myapplication.data.remote.dto.response.*;
import java.util.List;
import okhttp3.MultipartBody;

public final class AuthViewModel extends BaseViewModel {
    private final AuthRepository repository;

    public AuthViewModel(Application application) {
        super(application);
        repository = new AuthRepository(application);
    }

    private final androidx.lifecycle.MutableLiveData<com.example.myapplication.ui.base.UiState<LoginResponse>> signIn =
            new androidx.lifecycle.MutableLiveData<>();
    public androidx.lifecycle.LiveData<com.example.myapplication.ui.base.UiState<LoginResponse>> signInState() { return signIn; }
    public void acknowledgeSignIn() { signIn.setValue(null); }
    public void authenticate(LoginRequest request) {
        if (signIn.getValue() != null && signIn.getValue().status == com.example.myapplication.ui.base.UiState.Status.LOADING) return;
        signIn.setValue(com.example.myapplication.ui.base.UiState.loading());
        watch(login(request), response -> signIn.setValue(com.example.myapplication.ui.base.UiState.success(response)),
                error -> signIn.setValue(com.example.myapplication.ui.base.UiState.error(error)));
    }
    public static boolean validEmail(String value) { return android.util.Patterns.EMAIL_ADDRESS.matcher(value.trim()).matches(); }
    public static boolean validPassword(String value) { return value.matches("^(?=.*[0-9])(?=.*[a-z])(?=\\S+$).{8,}$"); }
    public static boolean validResetPassword(String value) { return value.matches("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"); }
    public static java.time.LocalDate birthDate(String value) {
        java.time.LocalDate date = java.time.LocalDate.parse(value.trim(), java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu")
                .withResolverStyle(java.time.format.ResolverStyle.STRICT));
        if (!date.isBefore(java.time.LocalDate.now())) throw new IllegalArgumentException();
        return date;
    }

    public UiOperation<LoginResponse> login(LoginRequest request) {
        return execute(repository.login(request), response -> {
            try { new com.example.myapplication.data.local.PreferenceManager(getApplication()).saveLogin(response.data, request.email); }
            catch (IllegalArgumentException invalid) { throw new IllegalArgumentException(getApplication().getString(com.example.myapplication.R.string.api_invalid_login)); }
            return response;
        });
    }

    public UiOperation<BaseResponse<CommonResponse>> register(RegisterRequest request) {
        return execute(repository.register(request));
    }

    public UiOperation<BaseResponse<CommonResponse>> verifyRegisterOtp(VerifyOtpRequest request) {
        return execute(repository.verifyRegisterOtp(request));
    }

    public UiOperation<BaseResponse<CommonResponse>> sendOtp(SendOtpRequest request) {
        return execute(repository.sendOtp(request));
    }

    public UiOperation<BaseResponse<CommonResponse>> forgotPasswordSendOtp(SendOtpRequest request) {
        return execute(repository.forgotPasswordSendOtp(request));
    }

    public UiOperation<BaseResponse<CommonResponse>> forgotPasswordVerifyOtp(VerifyOtpRequest request) {
        return execute(repository.forgotPasswordVerifyOtp(request));
    }

    public UiOperation<BaseResponse<CommonResponse>> resetPassword(ResetPasswordRequest request) {
        return execute(repository.resetPassword(request));
    }

    public UiOperation<LoginResponse> refreshToken(RefreshTokenRequest request) {
        return execute(repository.refreshToken(request));
    }

    public UiOperation<BaseResponse<Object>> logout(LogoutRequest request) {
        return execute(repository.logout(request));
    }
    public static final class FlowResult {
        public final com.example.myapplication.ui.main.UiScreen next;
        public final String email;
        public final boolean reset;
        public final int notice;
        FlowResult(com.example.myapplication.ui.main.UiScreen next, String email, boolean reset, int notice) {
            this.next = next; this.email = email; this.reset = reset; this.notice = notice;
        }
    }
    private final androidx.lifecycle.MutableLiveData<com.example.myapplication.ui.base.UiState<FlowResult>> flow = new androidx.lifecycle.MutableLiveData<>();
    public androidx.lifecycle.LiveData<com.example.myapplication.ui.base.UiState<FlowResult>> flowState() { return flow; }
    public void acknowledgeFlow() { flow.setValue(null); }
    private <T> void perform(UiOperation<T> operation, FlowResult next) {
        perform(operation, next, () -> { });
    }
    private <T> void perform(UiOperation<T> operation, FlowResult next, Runnable success) {
        if (flow.getValue() != null && flow.getValue().status == com.example.myapplication.ui.base.UiState.Status.LOADING) { operation.cancel(); return; }
        flow.setValue(com.example.myapplication.ui.base.UiState.loading());
        watch(operation, response -> {
            success.run(); flow.setValue(com.example.myapplication.ui.base.UiState.success(next));
        }, error -> {
            if (error.code == 429) startOtpCooldown();
            flow.setValue(com.example.myapplication.ui.base.UiState.error(error));
        });
    }
    public void registerAccount(RegisterRequest request) {
        perform(register(request), new FlowResult(com.example.myapplication.ui.main.UiScreen.VERIFY_EMAIL, request.email, false, 0));
    }
    public void sendResetOtp(String email) {
        perform(forgotPasswordSendOtp(new SendOtpRequest(email)), new FlowResult(com.example.myapplication.ui.main.UiScreen.VERIFY_OTP, email, true, 0));
    }
    public void sendRegistrationOtp(String email) {
        perform(sendOtp(new SendOtpRequest(email)), new FlowResult(com.example.myapplication.ui.main.UiScreen.VERIFY_OTP, email, false, 0));
    }
    private boolean initialOtpHandled;
    public void ensureInitialOtp(String email, boolean reset, boolean alreadySent) {
        if (initialOtpHandled) return;
        initialOtpHandled = true;
        if (alreadySent) startOtpCooldown();
        else if (validEmail(email)) resendOtp(email, reset);
    }
    public static boolean validOtp(String otp) { return otp.matches("[0-9]{5}"); }
    public void verifyOtp(String email, String otp, boolean reset) {
        VerifyOtpRequest request = new VerifyOtpRequest(email, otp);
        perform(reset ? forgotPasswordVerifyOtp(request) : verifyRegisterOtp(request),
                new FlowResult(reset ? com.example.myapplication.ui.main.UiScreen.FORGOT_PASSWORD : com.example.myapplication.ui.main.UiScreen.LOGIN,
                        email, reset, reset ? 0 : com.example.myapplication.R.string.api_verified));
    }
    public void resendOtp(String email, boolean reset) {
        if (otpCooldown.getValue() != null && otpCooldown.getValue() > 0) return;
        SendOtpRequest request = new SendOtpRequest(email);
        perform(reset ? forgotPasswordSendOtp(request) : sendOtp(request), new FlowResult(null, email, reset, com.example.myapplication.R.string.api_otp_sent), this::startOtpCooldown);
    }
    private final android.os.Handler otpTimer = new android.os.Handler(android.os.Looper.getMainLooper());
    private final androidx.lifecycle.MutableLiveData<Integer> otpCooldown = new androidx.lifecycle.MutableLiveData<>(0);
    private long otpRetryAt;
    public androidx.lifecycle.LiveData<Integer> otpCooldown() { return otpCooldown; }
    private final Runnable otpTick = new Runnable() {
        @Override public void run() {
            int remaining = (int) Math.max(0, (otpRetryAt - android.os.SystemClock.elapsedRealtime() + 999) / 1000);
            otpCooldown.setValue(remaining);
            if (remaining > 0) otpTimer.postDelayed(this, 1000);
        }
    };
    private void startOtpCooldown() {
        otpRetryAt = android.os.SystemClock.elapsedRealtime() + 60000;
        otpTimer.removeCallbacks(otpTick); otpTick.run();
    }
    @Override protected void onCleared() {
        otpTimer.removeCallbacksAndMessages(null);
        super.onCleared();
    }
    public void resetAccountPassword(ResetPasswordRequest request) {
        perform(resetPassword(request), new FlowResult(com.example.myapplication.ui.main.UiScreen.LOGIN, request.email, true, com.example.myapplication.R.string.api_reset_success));
    }
}
