package com.example.myapplication;

import android.os.Looper;
import android.widget.EditText;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.api.ApiService;
import com.example.myapplication.data.remote.dto.request.LoginRequest;
import com.example.myapplication.data.remote.dto.response.LoginData;
import com.example.myapplication.data.remote.network.RetrofitClient;
import com.example.myapplication.ui.main.MainActivity;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class ApiIntegrationTest {
    private MockWebServer server;
    private PreferenceManager preferences;
    private ApiService api;

    @Before public void setup() throws Exception {
        server = new MockWebServer();
        server.start();
        preferences = new PreferenceManager(RuntimeEnvironment.getApplication());
        preferences.logout();
        api = RetrofitClient.create(server.url("/").toString(), preferences);
        RetrofitClient.setApiServiceForTests(api);
    }

    @After public void cleanup() throws Exception {
        RetrofitClient.setApiServiceForTests(null);
        preferences.logout();
        server.shutdown();
    }

    private MockResponse json(String body) {
        return new MockResponse().setHeader("Content-Type", "application/json").setBody(body);
    }

    private void session() {
        preferences.saveLogin(new LoginData("Bearer", "old", "refresh", "user-1", null), "test@example.com");
    }

    @Test public void malformedOtpResponseIsNotReportedAsConnectionFailure() throws Exception {
        server.enqueue(json("not-json"));
        java.util.concurrent.atomic.AtomicReference<com.example.myapplication.data.remote.network.ApiError> result =
                new java.util.concurrent.atomic.AtomicReference<>();
        com.example.myapplication.data.remote.network.ApiRequests.enqueue(
                api.forgotPasswordSendOtp(new com.example.myapplication.data.remote.dto.request.SendOtpRequest("test@example.com")),
                new com.example.myapplication.data.remote.network.ApiCallback<com.example.myapplication.data.remote.dto.response.BaseResponse<com.example.myapplication.data.remote.dto.response.CommonResponse>>() {
                    @Override public void onSuccess(com.example.myapplication.data.remote.dto.response.BaseResponse<com.example.myapplication.data.remote.dto.response.CommonResponse> value) {
                        fail("Malformed JSON must fail");
                    }
                    @Override public void onError(com.example.myapplication.data.remote.network.ApiError error) { result.set(error); }
                });
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (result.get() == null && System.nanoTime() < deadline) {
            Shadows.shadowOf(Looper.getMainLooper()).idle(); Thread.sleep(10);
        }
        assertNotNull(result.get());
        assertEquals("Dữ liệu phản hồi từ máy chủ không hợp lệ. Vui lòng thử lại sau.", result.get().message);
        assertEquals("/api/v1/auth/forgot-password/send-otp", server.takeRequest(2, TimeUnit.SECONDS).getPath());
    }

    @Test public void loginUsesExpectedBodyAndNoBearer() throws Exception {
        session();
        server.enqueue(json("{\"codeStatus\":200,\"data\":{\"id\":\"user-1\",\"accessToken\":\"new\"}}"));
        assertEquals("new", api.login(new LoginRequest("test@example.com", "secret")).execute().body().data.accessToken);
        RecordedRequest request = server.takeRequest(2, TimeUnit.SECONDS);
        assertEquals("/api/v1/auth/login", request.getPath());
        assertNull(request.getHeader("Authorization"));
        assertTrue(request.getBody().readUtf8().contains("test@example.com"));
    }

    @Test public void unauthorizedRequestRefreshesAndRetriesOnce() throws Exception {
        session();
        server.enqueue(new MockResponse().setResponseCode(401));
        server.enqueue(json("{\"codeStatus\":200,\"data\":{\"accessToken\":\"new\"}}"));
        server.enqueue(json("{\"codeStatus\":200,\"data\":{\"id\":\"user-1\"}}"));
        assertTrue(api.getCurrentUser().execute().isSuccessful());
        assertEquals("Bearer old", server.takeRequest().getHeader("Authorization"));
        RecordedRequest refresh = server.takeRequest();
        assertEquals("/api/v1/auth/refresh", refresh.getPath());
        assertNull(refresh.getHeader("Authorization"));
        RecordedRequest retry = server.takeRequest();
        assertEquals("Bearer new", retry.getHeader("Authorization"));
        assertEquals("vi", retry.getHeader("Accept-Language"));
        assertEquals("refresh", preferences.getRefreshToken());
        assertEquals("new", preferences.getAccessToken());
    }
    @Test public void tokenRefreshPreservesAdminAuthorityWhenResponseOmitsIt() throws Exception {
        preferences.saveLogin(new LoginData("Bearer", "old", "refresh", "admin", java.util.Collections.singletonList(
                new com.example.myapplication.data.remote.dto.response.Authority("ADMIN"))), "admin@example.com");
        server.enqueue(new MockResponse().setResponseCode(401));
        server.enqueue(json("{\"codeStatus\":200,\"data\":{\"accessToken\":\"new\"}}"));
        server.enqueue(json("{\"codeStatus\":200,\"data\":{\"id\":\"admin\"}}"));
        assertTrue(api.getCurrentUser().execute().isSuccessful()); assertTrue(preferences.isAdmin());
        assertEquals(com.example.myapplication.ui.main.UiScreen.ADMIN_USERS, preferences.homeScreen());
    }

    @Test public void refreshServerFailurePreservesSession() throws Exception {
        session();
        server.enqueue(new MockResponse().setResponseCode(401));
        server.enqueue(new MockResponse().setResponseCode(503));
        assertEquals(401, api.getCurrentUser().execute().code());
        assertTrue(preferences.isLogin());
        assertEquals(2, server.getRequestCount());
    }

    @Test public void invalidRefreshClearsSession() throws Exception {
        session();
        server.enqueue(new MockResponse().setResponseCode(401));
        server.enqueue(json("{\"codeStatus\":401,\"message\":\"Expired\"}"));
        assertEquals(401, api.getCurrentUser().execute().code());
        assertFalse(preferences.isLogin());
    }

    @Test public void loginScreenValidatesThenPersistsSuccessfulLogin() throws Exception {
        try (var controller = Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity activity = controller.get();
            activity.getSupportFragmentManager().executePendingTransactions();
            activity.findViewById(R.id.tvLogin).performClick();
            assertEquals(android.view.View.VISIBLE, activity.findViewById(R.id.tvPasswordError).getVisibility());
            assertEquals(0, server.getRequestCount());
            ((EditText) activity.findViewById(R.id.edtEmail)).setText("test@example.com");
            ((EditText) activity.findViewById(R.id.edtPassword)).setText("secret");
            server.enqueue(json("{\"codeStatus\":200,\"data\":{\"id\":\"user-1\",\"accessToken\":\"token\",\"refreshToken\":\"refresh\"}}"));
            activity.findViewById(R.id.tvLogin).performClick();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (!preferences.isLogin() && System.nanoTime() < deadline) {
                Shadows.shadowOf(Looper.getMainLooper()).idle();
                Thread.sleep(10);
            }
            Shadows.shadowOf(Looper.getMainLooper()).idle();
            activity.getSupportFragmentManager().executePendingTransactions();
            assertTrue(preferences.isLogin());
            assertEquals("test@example.com", preferences.getEmail());
            assertNotNull(activity.findViewById(R.id.mapView));
        }
    }

    @Test public void rejectedLoginStaysOnFormAndEnablesRetry() throws Exception {
        try (var controller = Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity activity = controller.get();
            activity.getSupportFragmentManager().executePendingTransactions();
            ((EditText) activity.findViewById(R.id.edtEmail)).setText("test@example.com");
            ((EditText) activity.findViewById(R.id.edtPassword)).setText("wrong");
            server.enqueue(json("{\"message\":\"Invalid credentials\"}").setResponseCode(401));
            activity.findViewById(R.id.tvLogin).performClick();
            await(() -> activity.findViewById(R.id.tvLogin).isEnabled());
            assertFalse(preferences.isLogin());
            assertEquals(com.example.myapplication.data.remote.network.ErrorMessageMapper.map("Invalid credentials", 401),
                    ((android.widget.TextView) activity.findViewById(R.id.tvPasswordError)).getText().toString());
            assertEquals(1, server.getRequestCount());
        }
    }

    @Test public void resetOtpErrorsAreInlineAndClearWhenEmailChanges() throws Exception {
        try (var controller = Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity activity = controller.get();
            activity.getSupportFragmentManager().executePendingTransactions();
            activity.showScreen(com.example.myapplication.ui.main.UiScreen.INPUT_EMAIL);
            activity.getSupportFragmentManager().executePendingTransactions();
            EditText email = activity.findViewById(R.id.edtEmail);
            android.widget.TextView error = activity.findViewById(R.id.tvEmailError);
            activity.findViewById(R.id.btnSendEmail).performClick();
            assertEquals(android.view.View.VISIBLE, error.getVisibility());
            assertEquals(activity.getString(R.string.auth_email_required), error.getText().toString());
            assertEquals(0, server.getRequestCount());
            String[][] cases = {
                    {"404", "User not found with this username: test@example.com"},
                    {"400", "OTP code is incorrect or has expired"},
                    {"429", "You are doing this too fast, please wait 60 seconds!"},
                    {"500", "The email sending system is experiencing issues, please try again later!"}
            };
            for (String[] item : cases) {
                email.setText("test@example.com");
                assertEquals(android.view.View.GONE, error.getVisibility());
                server.enqueue(json("{\"message\":\"Request failed\",\"data\":{\"message\":\"" + item[1] + "\"}}")
                        .setResponseCode(Integer.parseInt(item[0])));
                activity.findViewById(R.id.btnSendEmail).performClick();
                await(() -> activity.findViewById(R.id.btnSendEmail).isEnabled());
                assertEquals(android.view.View.VISIBLE, error.getVisibility());
                assertEquals(com.example.myapplication.data.remote.network.ErrorMessageMapper.map(item[1], Integer.parseInt(item[0])),
                        error.getText().toString());
            }
            String lastError = error.getText().toString();
            controller.recreate();
            assertEquals(lastError, ((android.widget.TextView) controller.get().findViewById(R.id.tvEmailError)).getText().toString());
            assertEquals(android.view.View.VISIBLE, controller.get().findViewById(R.id.tvEmailError).getVisibility());
            ((EditText) controller.get().findViewById(R.id.edtEmail)).setText("changed@example.com");
            assertEquals(android.view.View.GONE, controller.get().findViewById(R.id.tvEmailError).getVisibility());
        }
    }

    @Test public void fieldErrorsAreReadFromResponseDataAndTranslatedSeparately() throws Exception {
        server.enqueue(json("{\"message\":\"Validation failed\",\"data\":{\"email\":\"Invalid email format\",\"password\":\"This field is required\"}}")
                .setResponseCode(400));
        java.util.concurrent.atomic.AtomicReference<com.example.myapplication.data.remote.network.ApiError> error = new java.util.concurrent.atomic.AtomicReference<>();
        com.example.myapplication.data.remote.network.ApiRequests.enqueue(api.forgotPasswordSendOtp(
                new com.example.myapplication.data.remote.dto.request.SendOtpRequest("test@example.com")),
                new com.example.myapplication.data.remote.network.ApiCallback<com.example.myapplication.data.remote.dto.response.BaseResponse<com.example.myapplication.data.remote.dto.response.CommonResponse>>() {
                    @Override public void onSuccess(com.example.myapplication.data.remote.dto.response.BaseResponse<com.example.myapplication.data.remote.dto.response.CommonResponse> response) { fail("Expected validation failure"); }
                    @Override public void onError(com.example.myapplication.data.remote.network.ApiError failure) { error.set(failure); }
                });
        await(() -> error.get() != null);
        assertEquals(400, error.get().code);
        assertEquals(com.example.myapplication.data.remote.network.ErrorMessageMapper.map("Invalid email format", 400) + "\n"
                + com.example.myapplication.data.remote.network.ErrorMessageMapper.map("This field is required", 400), error.get().message);
    }

    @Test public void otpRateLimitShowsVietnameseAndBlocksResendUntilCooldownEnds() throws Exception {
        try (var controller = Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity activity = controller.get();
            activity.getSupportFragmentManager().executePendingTransactions();
            android.os.Bundle args = new android.os.Bundle();
            args.putString(com.example.myapplication.ui.auth.BaseAuthFragment.ARG_EMAIL, "otp@example.com");
            args.putBoolean(com.example.myapplication.ui.auth.VerifyOtpFragment.ARG_RESET_PASSWORD, true);
            server.enqueue(json("{\"message\":\"Too many requests. Please try again later.\"}").setResponseCode(429));
            activity.showScreen(com.example.myapplication.ui.main.UiScreen.VERIFY_OTP, args);
            activity.getSupportFragmentManager().executePendingTransactions();
            await(() -> activity.findViewById(R.id.tvOtpError).getVisibility() == android.view.View.VISIBLE);
            assertFalse(activity.findViewById(R.id.btnResend).isEnabled());
            assertEquals(com.example.myapplication.data.remote.network.ErrorMessageMapper.map("Too many requests. Please try again later.", 429),
                    ((android.widget.TextView) activity.findViewById(R.id.tvOtpError)).getText().toString());
            assertFalse(((android.widget.TextView) activity.findViewById(R.id.tvOtpError)).getText().toString().contains("Too many requests"));
            activity.findViewById(R.id.btnResend).performClick();
            Shadows.shadowOf(Looper.getMainLooper()).idle();
            assertEquals(1, server.getRequestCount());
            controller.recreate();
            assertFalse(controller.get().findViewById(R.id.btnResend).isEnabled());
            Shadows.shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofSeconds(61));
            assertTrue(controller.get().findViewById(R.id.btnResend).isEnabled());
            assertEquals(1, server.getRequestCount());
            server.enqueue(json("{\"codeStatus\":200,\"data\":{\"status\":true}}"));
            controller.get().findViewById(R.id.btnResend).performClick();
            await(() -> controller.get().findViewById(R.id.btnVerify).isEnabled());
            assertEquals(2, server.getRequestCount());
            assertFalse(controller.get().findViewById(R.id.btnResend).isEnabled());
        }
    }

    @Test public void otpScreenSendsOnceAndMovesBetweenDigitFields() throws Exception {
        for (boolean reset : new boolean[]{true, false}) {
            try (var controller = Robolectric.buildActivity(MainActivity.class).setup()) {
                MainActivity activity = controller.get();
                activity.getSupportFragmentManager().executePendingTransactions();
                android.os.Bundle args = new android.os.Bundle();
                args.putString(com.example.myapplication.ui.auth.BaseAuthFragment.ARG_EMAIL, "otp@example.com");
                args.putBoolean(com.example.myapplication.ui.auth.VerifyOtpFragment.ARG_RESET_PASSWORD, reset);
                server.enqueue(json("{\"codeStatus\":200,\"data\":{\"status\":true}}"));
                int before = server.getRequestCount();
                activity.showScreen(com.example.myapplication.ui.main.UiScreen.VERIFY_OTP, args);
                activity.getSupportFragmentManager().executePendingTransactions();
                await(() -> activity.findViewById(R.id.btnVerify).isEnabled());
                assertEquals(reset ? "/api/v1/auth/forgot-password/send-otp" : "/api/v1/auth/send-otp",
                        server.takeRequest(2, TimeUnit.SECONDS).getPath());
                assertEquals(before + 1, server.getRequestCount());
                int[] ids = {R.id.edtOtp1, R.id.edtOtp2, R.id.edtOtp3, R.id.edtOtp4, R.id.edtOtp5};
                activity.findViewById(ids[0]).requestFocus();
                for (int i = 0; i < ids.length; i++) {
                    ((EditText) activity.findViewById(ids[i])).setText(String.valueOf(i + 1));
                    assertTrue(activity.findViewById(ids[Math.min(i + 1, ids.length - 1)]).hasFocus());
                }
                EditText last = activity.findViewById(R.id.edtOtp5);
                last.setText("");
                assertTrue(activity.findViewById(R.id.edtOtp4).hasFocus());
                last.requestFocus();
                last.dispatchKeyEvent(new android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DEL));
                assertTrue(activity.findViewById(R.id.edtOtp4).hasFocus());
                controller.recreate();
                Shadows.shadowOf(Looper.getMainLooper()).idle();
                assertEquals(before + 1, server.getRequestCount());
                assertEquals("1", ((EditText) controller.get().findViewById(R.id.edtOtp1)).getText().toString());
            }
        }
    }

    @Test public void passwordResetCarriesEmailThroughVerifiedOtp() throws Exception {
        try (var controller = Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity activity = controller.get();
            activity.getSupportFragmentManager().executePendingTransactions();
            activity.findViewById(R.id.tvForgot).performClick();
            activity.getSupportFragmentManager().executePendingTransactions();
            ((EditText) activity.findViewById(R.id.edtEmail)).setText("reset@example.com");
            server.enqueue(json("{\"codeStatus\":200,\"data\":{\"status\":true}}"));
            activity.findViewById(R.id.btnSendEmail).performClick();
            await(() -> activity.findViewById(R.id.edtOtp1) != null);
            RecordedRequest send = server.takeRequest(2, TimeUnit.SECONDS);
            assertEquals("/api/v1/auth/forgot-password/send-otp", send.getPath());
            assertTrue(send.getBody().readUtf8().contains("reset@example.com"));
            assertEquals(1, server.getRequestCount());
            assertTrue(((android.widget.TextView) activity.findViewById(R.id.tvOtpDescription)).getText().toString().contains("reset@example.com"));
            int[] fields = {R.id.edtOtp1, R.id.edtOtp2, R.id.edtOtp3, R.id.edtOtp4, R.id.edtOtp5};
            for (int i = 0; i < fields.length; i++) ((EditText) activity.findViewById(fields[i])).setText(String.valueOf(i + 1));
            server.enqueue(json("{\"codeStatus\":200,\"data\":{\"status\":true}}"));
            activity.findViewById(R.id.btnVerify).performClick();
            await(() -> activity.findViewById(R.id.edtNewPassword) != null);
            RecordedRequest verify = server.takeRequest(2, TimeUnit.SECONDS);
            assertEquals("/api/v1/auth/forgot-password/verify-otp", verify.getPath());
            assertTrue(verify.getBody().readUtf8().contains("12345"));
            ((EditText) activity.findViewById(R.id.edtNewPassword)).setText("Newpass1!");
            ((EditText) activity.findViewById(R.id.edtConfirmPassword)).setText("Newpass1!");
            server.enqueue(json("{\"codeStatus\":200,\"data\":{\"status\":true}}"));
            activity.findViewById(R.id.tvConfirm).performClick();
            await(() -> activity.findViewById(R.id.tvLogin) != null);
            RecordedRequest reset = server.takeRequest(2, TimeUnit.SECONDS);
            assertEquals("/api/v1/auth/forgot-password/reset", reset.getPath());
            assertTrue(reset.getBody().readUtf8().contains("reset@example.com"));
        }
    }

    @Test public void fragmentReleasesBindingWhenViewIsDestroyed() throws Exception {
        try (var controller = Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity activity = controller.get();
            activity.getSupportFragmentManager().executePendingTransactions();
            var fragment = activity.getSupportFragmentManager().findFragmentById(R.id.screenHost);
            var field = com.example.myapplication.ui.base.BaseFragment.class.getDeclaredField("binding");
            field.setAccessible(true);
            assertNotNull(field.get(fragment));
            activity.showScreen(com.example.myapplication.ui.main.UiScreen.INPUT_EMAIL);
            activity.getSupportFragmentManager().executePendingTransactions();
            assertNull(field.get(fragment));
        }
    }

    private void await(java.util.function.BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
            Shadows.shadowOf(Looper.getMainLooper()).idle();
            Thread.sleep(10);
        }
        assertTrue("Timed out waiting for API callback", condition.getAsBoolean());
    }
}
