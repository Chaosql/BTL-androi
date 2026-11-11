package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.location.*;
import android.os.*;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.response.*;
import com.example.myapplication.data.remote.network.RetrofitClient;
import com.example.myapplication.ui.main.*;
import com.example.myapplication.ui.map.RadarData;
import com.google.gson.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import okhttp3.mockwebserver.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk = 34)
public class RadarUiTest {
    private MockWebServer server;
    private PreferenceManager preferences;
    private ActivityController<MainActivity> controller;
    private MainActivity activity;
    private LocationManager locations;
    private volatile boolean buddy = true, failUpdate, expiredSession;
    private final List<RecordedRequest> radarRequests = Collections.synchronizedList(new ArrayList<>());
    private MockResponse response(String data) { return new MockResponse().setHeader("Content-Type", "application/json").setBody("{\"codeStatus\":200,\"data\":" + data + "}"); }
    @Before public void setup() throws Exception {
        ShadowSystemClock.advanceBy(Duration.ofMinutes(5));
        Context app = RuntimeEnvironment.getApplication();
        Shadows.shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
        locations = (LocationManager) app.getSystemService(Context.LOCATION_SERVICE);
        Shadows.shadowOf(locations).setLocationEnabled(true);
        Shadows.shadowOf(locations).setProviderEnabled(LocationManager.GPS_PROVIDER, true);
        Shadows.shadowOf(locations).setProviderEnabled(LocationManager.NETWORK_PROVIDER, false);
        cache(false);
        server = new MockWebServer(); server.setDispatcher(new Dispatcher() {
            @Override public MockResponse dispatch(RecordedRequest request) {
                String path = request.getRequestUrl().encodedPath();
                if (expiredSession && (path.equals("/api/v1/user/current") || path.equals("/api/v1/auth/refresh"))) return new MockResponse().setResponseCode(401);
                if (path.equals("/api/v1/user/current")) return response("{\"id\":\"self\",\"buddyActive\":" + buddy + "}");
                if (path.equals("/api/v1/notifications/unread-count")) return response("0");
                if (path.startsWith("/api/v1/location/")) {
                    radarRequests.add(request);
                    if (path.endsWith("/update")) return response("{\"status\":" + !failUpdate + "}");
                    if (path.endsWith("/radar")) return response("[{\"userId\":\"self\",\"distanceKm\":0},{\"userId\":\"near\",\"firstName\":\"Near\",\"latitude\":21.01,\"longitude\":105,\"distanceKm\":0.5},{\"userId\":\"near\",\"distanceKm\":0.6},{\"userId\":\"far\",\"distanceKm\":4}]");
                }
                if (path.equals("/api/v1/profile/near")) return response("{\"id\":\"near\",\"firstName\":\"Near\",\"university\":\"Real school\"}");
                if (path.endsWith("/online-status")) return response("{\"isOnline\":true}");
                return new MockResponse().setResponseCode(404);
            }
        }); server.start();
        preferences = new PreferenceManager(app); preferences.logout();
        preferences.saveLogin(new LoginData("Bearer", "access", "refresh", "self", null), "test@example.com");
        RetrofitClient.setApiServiceForTests(RetrofitClient.create(server.url("/").toString(), preferences));
        controller = Robolectric.buildActivity(MainActivity.class).setup(); activity = controller.get();
        activity.getSupportFragmentManager().executePendingTransactions();
    }
    private Location point(boolean stale) {
        Location point = new Location(LocationManager.GPS_PROVIDER); point.setLatitude(21); point.setLongitude(105);
        point.setAccuracy(10); point.setTime(System.currentTimeMillis());
        point.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos() - (stale ? TimeUnit.MINUTES.toNanos(3) : 0)); return point;
    }
    private void cache(boolean stale) { Shadows.shadowOf(locations).setLastKnownLocation(LocationManager.GPS_PROVIDER, point(stale)); }
    @After public void cleanup() throws Exception { controller.close(); preferences.logout(); RetrofitClient.setApiServiceForTests(null); server.shutdown(); }
    private void await(BooleanSupplier check) throws Exception {
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(8);
        while (!check.getAsBoolean() && System.nanoTime() < end) { Shadows.shadowOf(Looper.getMainLooper()).idle(); Thread.sleep(10); }
        assertTrue("Radar timed out", check.getAsBoolean());
    }
    private void assertToastError(int message) {
        assertEquals(activity.getString(message), ShadowToast.getTextOfLatestToast());
        assertEquals(View.GONE, activity.findViewById(R.id.tvRadarStatus).getVisibility());
        assertTrue(status().isEmpty());
    }
    private String status() { return ((TextView) activity.findViewById(R.id.tvRadarStatus)).getText().toString(); }
    private void scan() { ((EditText) activity.findViewById(R.id.etDistance)).setText("2.5"); activity.findViewById(R.id.btnRadar).performClick(); }
    @Test public void openingMapDoesNotTransmitLocationAndInvalidRadiusDoesNotScan() {
        assertTrue(radarRequests.isEmpty());
        ((EditText) activity.findViewById(R.id.etDistance)).setText("6"); activity.findViewById(R.id.btnRadar).performClick();
        assertNull(((EditText) activity.findViewById(R.id.etDistance)).getError());
        assertToastError(R.string.radar_invalid_radius);
        assertTrue(radarRequests.isEmpty());
        ((EditText) activity.findViewById(R.id.etDistance)).setText("3");
        assertEquals(View.GONE, activity.findViewById(R.id.tvRadarStatus).getVisibility());
        assertTrue(radarRequests.isEmpty());
    }
    @Test public void buddyOffPreventsLocationRequests() throws Exception {
        buddy = false; scan(); await(() -> activity.getString(R.string.radar_buddy_off).equals(ShadowToast.getTextOfLatestToast()));
        assertToastError(R.string.radar_buddy_off);
        assertTrue(radarRequests.isEmpty()); assertTrue(activity.findViewById(R.id.btnRadar).isEnabled());
    }
    @Test public void deniedPermissionDoesNotTransmitLocation() throws Exception {
        Shadows.shadowOf(RuntimeEnvironment.getApplication()).denyPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
        scan(); await(() -> Shadows.shadowOf(activity).getLastRequestedPermission() != null);
        ShadowActivity.PermissionsRequest request = Shadows.shadowOf(activity).getLastRequestedPermission();
        int[] denied = new int[request.requestedPermissions.length]; Arrays.fill(denied, android.content.pm.PackageManager.PERMISSION_DENIED);
        activity.onRequestPermissionsResult(request.requestCode, request.requestedPermissions, denied);
        await(() -> activity.getString(R.string.radar_permission_denied).equals(ShadowToast.getTextOfLatestToast()));
        assertToastError(R.string.radar_permission_denied);
        assertTrue(radarRequests.isEmpty()); assertTrue(activity.findViewById(R.id.btnRadar).isEnabled());
    }
    @Test public void scanUpdatesLocationThenQueriesAndOpensActualUser() throws Exception {
        scan(); await(() -> status().contains("Near") || status().contains("Tìm thấy"));
        assertEquals(2, radarRequests.size()); assertEquals("PUT", radarRequests.get(0).getMethod());
        JsonObject body = JsonParser.parseString(radarRequests.get(0).getBody().readUtf8()).getAsJsonObject();
        assertEquals(21, body.get("latitude").getAsDouble(), 0); assertEquals(105, body.get("longitude").getAsDouble(), 0);
        assertEquals("2.5", radarRequests.get(1).getRequestUrl().queryParameter("radius"));
        activity.findViewById(R.id.tvRadarStatus).performClick(); AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        assertEquals(1, dialog.getListView().getAdapter().getCount());
        dialog.getListView().performItemClick(null, 0, 0);
        await(() -> ShadowDialog.getLatestDialog().findViewById(R.id.tvSchool) != null
                && ((TextView) ShadowDialog.getLatestDialog().findViewById(R.id.tvSchool)).getText().toString().equals("Real school"));
        assertEquals(View.VISIBLE, ShadowDialog.getLatestDialog().findViewById(R.id.tvDistance).getVisibility());
    }
    @Test public void failedUpdateNeverQueriesRadarOrShowsSuccess() throws Exception {
        failUpdate = true; scan(); await(() -> radarRequests.size() == 1 && activity.findViewById(R.id.btnRadar).isEnabled());
        assertEquals(View.GONE, activity.findViewById(R.id.tvRadarStatus).getVisibility());
        assertNotNull(ShadowToast.getTextOfLatestToast());
        assertFalse(ShadowToast.getTextOfLatestToast().isEmpty());
        assertEquals(1, radarRequests.size()); assertTrue(radarRequests.get(0).getPath().endsWith("/update"));
    }
    @Test public void expiredSessionDoesNotLeaveRadarBusy() throws Exception {
        expiredSession = true; scan(); await(() -> !preferences.isLogin() && activity.findViewById(R.id.btnRadar).isEnabled());
        assertTrue(radarRequests.isEmpty());
    }
    @Test public void staleLocationWaitsForFreshFix() throws Exception {
        cache(true); scan(); await(() -> status().equals(activity.getString(R.string.radar_locating)));
        assertTrue(radarRequests.isEmpty()); Shadows.shadowOf(locations).simulateLocation(point(false));
        await(() -> radarRequests.size() == 2 && activity.findViewById(R.id.btnRadar).isEnabled());
    }
    @Test public void disabledLocationShowsToastWithoutSendingCoordinates() throws Exception {
        Shadows.shadowOf(locations).setLocationEnabled(false); scan();
        await(() -> activity.getString(R.string.radar_gps_disabled).equals(ShadowToast.getTextOfLatestToast()));
        assertToastError(R.string.radar_gps_disabled);
        assertTrue(radarRequests.isEmpty()); assertTrue(activity.findViewById(R.id.btnRadar).isEnabled());
    }
    @Test public void locationTimeoutRestoresScanButton() throws Exception {
        cache(true); scan(); await(() -> status().equals(activity.getString(R.string.radar_locating)));
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(26));
        assertToastError(R.string.radar_location_timeout);
        assertTrue(activity.findViewById(R.id.btnRadar).isEnabled()); assertTrue(radarRequests.isEmpty());
    }
    @Test public void approximatePermissionWorksWithNetworkProvider() throws Exception {
        Shadows.shadowOf(RuntimeEnvironment.getApplication()).denyPermissions(Manifest.permission.ACCESS_FINE_LOCATION);
        Shadows.shadowOf(locations).setProviderEnabled(LocationManager.NETWORK_PROVIDER, true);
        Location network = point(false); network.setProvider(LocationManager.NETWORK_PROVIDER);
        Shadows.shadowOf(locations).setLastKnownLocation(LocationManager.NETWORK_PROVIDER, network);
        scan(); await(() -> radarRequests.size() == 2 && activity.findViewById(R.id.btnRadar).isEnabled());
    }
    @Test public void leavingMapCancelsLocationListener() throws Exception {
        cache(true); scan(); await(() -> status().equals(activity.getString(R.string.radar_locating)));
        activity.showScreen(UiScreen.PROFILE); activity.getSupportFragmentManager().executePendingTransactions();
        Shadows.shadowOf(locations).simulateLocation(point(false)); Shadows.shadowOf(Looper.getMainLooper()).idle();
        assertTrue(radarRequests.isEmpty());
    }
    @Test public void filteringUsesServerDistanceAndKeepsUsersWithoutCoordinatesInList() {
        NearbyUserResponse a = new NearbyUserResponse(); a.userId = "a"; a.distanceKm = 1.0; a.latitude = 30.0; a.longitude = 100.0;
        NearbyUserResponse b = new NearbyUserResponse(); b.userId = "b"; b.distanceKm = 0.5;
        NearbyUserResponse self = new NearbyUserResponse(); self.userId = "self"; self.distanceKm = 0.0;
        assertEquals(Arrays.asList("b", "a"), RadarData.filter(Arrays.asList(a, b, a, self), "self", 21, 105, 2)
                .stream().map(user -> user.userId).collect(java.util.stream.Collectors.toList()));
        assertEquals(3, RadarData.radius(""), 0); assertEquals(1.5, RadarData.radius("1,5"), 0);
        for (String invalid : new String[]{"NaN", "Infinity", "0", "6", "abc"}) {
            try { RadarData.radius(invalid); fail(invalid); } catch (IllegalArgumentException expected) { }
        }
    }
}
