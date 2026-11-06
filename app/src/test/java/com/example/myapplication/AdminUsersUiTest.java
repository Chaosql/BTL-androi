package com.example.myapplication;

import android.os.*;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.response.*;
import com.example.myapplication.data.remote.network.RetrofitClient;
import com.example.myapplication.ui.main.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import okhttp3.mockwebserver.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowDialog;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk = 34)
public class AdminUsersUiTest {
    private MockWebServer server;
    private PreferenceManager preferences;
    private ActivityController<MainActivity> controller;
    private MainActivity activity;
    private volatile boolean locked, failAction, secondPage;
    private final List<String> writes = Collections.synchronizedList(new ArrayList<>());
    private String user(String id) { return "{\"id\":\"" + id + "\",\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"test@example.com\",\"role\":\"USER\",\"userstatus\":\"" + (locked && id.equals("target") ? "LOCKED" : "ACTIVE") + "\"}"; }
    private MockResponse json(String data) { return new MockResponse().setHeader("Content-Type", "application/json").setBody("{\"codeStatus\":200,\"data\":" + data + "}"); }
    @Before public void setup() throws Exception {
        server = new MockWebServer(); server.setDispatcher(new Dispatcher() {
            @Override public MockResponse dispatch(RecordedRequest request) {
                String path = request.getRequestUrl().encodedPath();
                if (path.equals("/api/v1/admin/users")) {
                    boolean next = "1".equals(request.getRequestUrl().queryParameter("page")); if (next) secondPage = true;
                    return json("{\"content\":[" + user(next ? "third" : "self") + "," + user("target") + "],\"last\":" + next + "}");
                }
                if ("PATCH".equals(request.getMethod())) {
                    writes.add(path); if (failAction) return new MockResponse().setResponseCode(403).setBody("{\"message\":\"Denied\"}");
                    locked = path.endsWith("/lock"); return json(user("target"));
                }
                if (path.startsWith("/api/v1/admin/users/")) return json(user(path.substring(path.lastIndexOf('/') + 1)));

                return new MockResponse().setResponseCode(404);
            }
        }); server.start();
        preferences = new PreferenceManager(RuntimeEnvironment.getApplication()); preferences.logout();
        preferences.saveLogin(new LoginData("Bearer", "access", "refresh", "self", Collections.singletonList(new Authority("ADMIN"))), "admin@example.com");
        RetrofitClient.setApiServiceForTests(RetrofitClient.create(server.url("/").toString(), preferences));
        controller = Robolectric.buildActivity(MainActivity.class).setup(); activity = controller.get();
        activity.getSupportFragmentManager().executePendingTransactions();
    }
    @After public void cleanup() throws Exception { controller.close(); preferences.logout(); RetrofitClient.setApiServiceForTests(null); server.shutdown(); }
    private void await(BooleanSupplier check) throws Exception {
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(8);
        while (!check.getAsBoolean() && System.nanoTime() < end) { Shadows.shadowOf(Looper.getMainLooper()).idle(); Thread.sleep(10); }
        assertTrue(check.getAsBoolean());
    }
    private RecyclerView list(int id) { return activity.findViewById(id); }
    @SuppressWarnings({"rawtypes", "unchecked"}) private View row(int index) {
        RecyclerView list = list(R.id.rvUsers); RecyclerView.Adapter adapter = list.getAdapter();
        RecyclerView.ViewHolder holder = adapter.createViewHolder(list, 0); adapter.bindViewHolder(holder, index); return holder.itemView;
    }
    private void loaded() throws Exception { await(() -> list(R.id.rvUsers).getAdapter().getItemCount() == 2 && activity.findViewById(R.id.btnReload).isEnabled()); }
    private AlertDialog dialog() { return (AlertDialog) ShadowDialog.getLatestDialog(); }
    private void openTarget() throws Exception { row(1).performClick(); await(() -> dialog() != null && dialog().isShowing()); }
    @Test public void adminHomeLoadsAndPaginates() throws Exception {
        loaded(); assertTrue(preferences.isAdmin()); assertEquals(UiScreen.ADMIN_USERS, preferences.homeScreen());
        activity.findViewById(R.id.btnMore).performClick(); await(() -> list(R.id.rvUsers).getAdapter().getItemCount() == 3); assertTrue(secondPage);
    }
    @Test public void lockNeedsConfirmationThenDisplaysServerState() throws Exception {
        loaded(); openTarget(); dialog().getButton(-1).performClick(); assertTrue(writes.isEmpty());
        await(() -> dialog().isShowing() && dialog().getButton(-1).getText().toString().equals(activity.getString(android.R.string.ok)));
        dialog().getButton(-1).performClick();
        await(() -> ((TextView) row(1).findViewById(R.id.tvState)).getText().toString().equals(activity.getString(R.string.admin_locked)));
        assertEquals(Collections.singletonList("/api/v1/admin/users/target/lock"), writes);
    }
    @Test public void failedLockPreservesActiveState() throws Exception {
        failAction = true; loaded(); openTarget(); dialog().getButton(-1).performClick();
        await(() -> dialog().isShowing() && dialog().getButton(-1).getText().toString().equals(activity.getString(android.R.string.ok)));
        dialog().getButton(-1).performClick();
        await(() -> writes.size() == 1 && activity.findViewById(R.id.btnReload).isEnabled());
        assertEquals(activity.getString(R.string.admin_active), ((TextView) row(1).findViewById(R.id.tvState)).getText().toString());
    }
    @Test public void selfHasNoLockAction() throws Exception {
        loaded(); row(0).performClick(); await(() -> dialog() != null && dialog().isShowing());
        assertEquals(View.GONE, dialog().getButton(-1).getVisibility()); assertTrue(writes.isEmpty());
    }
    @Test public void nonAdminCannotLoadAdminScreen() throws Exception {
        loaded(); preferences.saveLogin(new LoginData("Bearer", "other", "refresh", "self", null), "user@example.com");
        activity.showScreen(UiScreen.ADMIN_USERS); activity.getSupportFragmentManager().executePendingTransactions();
        assertEquals(activity.getString(R.string.admin_denied), ((TextView) activity.findViewById(R.id.tvStatus)).getText().toString());
        assertEquals(0, list(R.id.rvUsers).getAdapter().getItemCount());
    }
    @Test public void unlockNeedsConfirmationAndUsesUnlockEndpoint() throws Exception {
        locked = true; loaded(); openTarget(); dialog().getButton(-1).performClick();
        await(() -> dialog().isShowing() && dialog().getButton(-1).getText().toString().equals(activity.getString(android.R.string.ok)));
        dialog().getButton(-1).performClick();
        await(() -> writes.size() == 1 && activity.findViewById(R.id.btnReload).isEnabled());
        assertEquals(Collections.singletonList("/api/v1/admin/users/target/unlock"), writes);
        assertEquals(activity.getString(R.string.admin_active), ((TextView) row(1).findViewById(R.id.tvState)).getText().toString());
    }

}
