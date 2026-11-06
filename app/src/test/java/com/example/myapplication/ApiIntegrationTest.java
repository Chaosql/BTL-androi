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
            assertNotNull(((EditText) activity.findViewById(R.id.edtEmail)).getError());
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
            assertEquals("Invalid credentials", org.robolectric.shadows.ShadowToast.getTextOfLatestToast());
            assertEquals(1, server.getRequestCount());
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
