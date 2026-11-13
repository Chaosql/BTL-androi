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
    private volatile boolean locked, missingList, emptyList, forbidden;
    private volatile boolean failChange, failDetailAfterChange, unchangedStatus;
    private final List<com.google.gson.JsonObject> changeBodies = Collections.synchronizedList(new ArrayList<>());
    private final List<String> reads = Collections.synchronizedList(new ArrayList<>());
    private final List<String> writes = Collections.synchronizedList(new ArrayList<>());
    private String user(String id) {
        return "{\"id\":\"" + id + "\",\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"test@example.com\",\"roleName\":\"USER\",\"userstatus\":\""
                + (locked && id.equals("target") ? "LOCKED" : "ACTIVE") + "\"}";
    }
    private MockResponse json(String data) {
        return new MockResponse().setHeader("Content-Type", "application/json").setBody("{\"codeStatus\":200,\"data\":" + data + "}");
    }
    @Before public void setup() throws Exception {
        server = new MockWebServer(); server.setDispatcher(new Dispatcher() {
            @Override public MockResponse dispatch(RecordedRequest request) {
                String path = request.getRequestUrl().encodedPath();
                if (!"GET".equals(request.getMethod())) {
                    writes.add(path);
                    if (!"PATCH".equals(request.getMethod()) || !(path.equals("/api/v1/user/target/lock") || path.equals("/api/v1/user/target/unlock")))
                        return new MockResponse().setResponseCode(404);
                    changeBodies.add(com.google.gson.JsonParser.parseString(request.getBody().readUtf8()).getAsJsonObject());
                    if (failChange) return new MockResponse().setResponseCode(403).setBody("{\"message\":\"Denied\"}");
                    if (!unchangedStatus) locked = path.endsWith("/lock");
                    return new MockResponse().setHeader("Content-Type", "application/json").setBody("{}");
                }
                reads.add(request.getPath());
                if (path.equals("/api/v1/user")) {
                    if (forbidden) return new MockResponse().setResponseCode(403).setBody("{\"message\":\"Denied\"}");
                    if (missingList) return json("{\"items\":null,\"meta\":null}");
                    if (emptyList) return json("{\"items\":[],\"meta\":{\"pageNum\":1,\"pageSize\":20,\"totalPages\":0,\"totalElements\":0}}");
                    String page = request.getRequestUrl().queryParameter("pageNum");
                    if (!"20".equals(request.getRequestUrl().queryParameter("pageSize")) || !("1".equals(page) || "2".equals(page)))
                        return new MockResponse().setResponseCode(400);
                    return json("{\"items\":[" + user("2".equals(page) ? "third" : "self") + "," + user("target")
                            + "],\"meta\":{\"pageNum\":" + page + ",\"pageSize\":20,\"totalPages\":2,\"totalElements\":3}}");
                }
                if (path.startsWith("/api/v1/user/")) {
                    if (failDetailAfterChange && !writes.isEmpty())
                        return new MockResponse().setResponseCode(503).setBody("{\"message\":\"Unavailable\"}");
                    return json(user(path.substring(path.lastIndexOf('/') + 1)));
                }
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
    private RecyclerView list() { return activity.findViewById(R.id.rvUsers); }
    @SuppressWarnings({"rawtypes", "unchecked"}) private View row(int index) {
        RecyclerView.Adapter adapter = list().getAdapter();
        RecyclerView.ViewHolder holder = adapter.createViewHolder(list(), 0); adapter.bindViewHolder(holder, index); return holder.itemView;
    }
    private void loaded() throws Exception { await(() -> list().getAdapter().getItemCount() == 2 && activity.findViewById(R.id.btnReload).isEnabled()); }
    private AlertDialog dialog() { return (AlertDialog) ShadowDialog.getLatestDialog(); }
    private String status() { return ((TextView) activity.findViewById(R.id.tvStatus)).getText().toString(); }

    @Test public void adminHomeUsesBackendItemsMetaAndOneBasedPagination() throws Exception {
        loaded(); assertTrue(preferences.isAdmin()); assertEquals(UiScreen.ADMIN_USERS, preferences.homeScreen());
        assertTrue(reads.contains("/api/v1/user?pageNum=1&pageSize=20"));
        activity.findViewById(R.id.btnMore).performClick();
        await(() -> list().getAdapter().getItemCount() == 3 && activity.findViewById(R.id.btnMore).getVisibility() == View.GONE);
        assertTrue(reads.contains("/api/v1/user?pageNum=2&pageSize=20"));
        assertTrue(writes.isEmpty());
    }
    @Test public void detailUsesUserEndpointRoleNameAndXmlLockAction() throws Exception {
        loaded(); row(1).performClick(); await(() -> dialog() != null && dialog().isShowing());
        assertTrue(reads.contains("/api/v1/user/target"));
        assertTrue(((TextView) dialog().findViewById(R.id.tvUserDetails)).getText().toString().contains("USER"));
        assertEquals(View.GONE, dialog().getButton(AlertDialog.BUTTON_POSITIVE).getVisibility());
        assertTrue(writes.isEmpty());
        assertEquals("User Test", ((TextView) dialog().findViewById(R.id.tvUserName)).getText().toString());
        assertEquals(View.VISIBLE, dialog().findViewById(R.id.btnAccountStatus).getVisibility());
        assertEquals(activity.getString(R.string.admin_lock), ((TextView) dialog().findViewById(R.id.btnAccountStatus)).getText().toString());
        dialog().findViewById(R.id.btnClose).performClick();
        assertFalse(dialog().isShowing());
    }
    @Test public void lockedAccountOffersXmlUnlockAction() throws Exception {
        locked = true; loaded(); row(1).performClick(); await(() -> dialog() != null && dialog().isShowing());
        assertEquals(View.GONE, dialog().getButton(AlertDialog.BUTTON_POSITIVE).getVisibility());
        assertEquals(activity.getString(R.string.admin_unlock), ((TextView) dialog().findViewById(R.id.btnAccountStatus)).getText().toString());
        assertTrue(writes.isEmpty());
    }
    private AlertDialog openStatusForm() throws Exception {
        loaded(); row(1).performClick(); await(() -> dialog() != null && dialog().isShowing());
        AlertDialog detail = dialog();
        detail.findViewById(R.id.btnAccountStatus).performClick();
        return detail;
    }
    private void confirmStatus(AlertDialog detail, String reason, String reportId) {
        ((EditText) detail.findViewById(R.id.etAccountReason)).setText(reason);
        ((EditText) detail.findViewById(R.id.etReportId)).setText(reportId);
        detail.findViewById(R.id.btnConfirmAccountStatus).performClick();
    }
    private String rowState() { return ((TextView) row(1).findViewById(R.id.tvState)).getText().toString(); }

    @Test public void lockRequiresReasonSendsBodyOnceAndReadsAuthoritativeState() throws Exception {
        AlertDialog detail = openStatusForm();
        detail.findViewById(R.id.btnConfirmAccountStatus).performClick();
        assertTrue(writes.isEmpty());
        assertEquals(View.VISIBLE, detail.findViewById(R.id.tvAccountStatusError).getVisibility());
        confirmStatus(detail, "  Policy violation  ", "  report-1  ");
        detail.findViewById(R.id.btnConfirmAccountStatus).performClick();
        await(() -> activity.getString(R.string.admin_locked).equals(rowState()));
        assertEquals(Collections.singletonList("/api/v1/user/target/lock"), writes);
        assertEquals("Policy violation", changeBodies.get(0).get("reason").getAsString());
        assertEquals("report-1", changeBodies.get(0).get("reportId").getAsString());
        assertEquals(View.GONE, detail.findViewById(R.id.layoutAccountStatus).getVisibility());
        assertEquals(activity.getString(R.string.admin_unlock), ((TextView) detail.findViewById(R.id.btnAccountStatus)).getText().toString());
    }
    @Test public void unlockUsesNewEndpointAndOmitsMissingReportId() throws Exception {
        locked = true;
        AlertDialog detail = openStatusForm();
        confirmStatus(detail, "Reviewed", "");
        await(() -> activity.getString(R.string.admin_active).equals(rowState()) && detail.findViewById(R.id.layoutAccountStatus).getVisibility() == View.GONE);
        assertEquals(Collections.singletonList("/api/v1/user/target/unlock"), writes);
        assertEquals("Reviewed", changeBodies.get(0).get("reason").getAsString());
        assertFalse(changeBodies.get(0).has("reportId"));
    }
    @Test public void failedStatusChangeShowsInlineErrorKeepsInputAndCanRetry() throws Exception {
        AlertDialog detail = openStatusForm(); failChange = true;
        confirmStatus(detail, "Keep reason", "report-1");
        await(() -> detail.findViewById(R.id.btnConfirmAccountStatus).isEnabled());
        assertEquals("Denied", ((TextView) detail.findViewById(R.id.tvAccountStatusError)).getText().toString());
        assertEquals("Keep reason", ((EditText) detail.findViewById(R.id.etAccountReason)).getText().toString());
        assertEquals(activity.getString(R.string.admin_active), rowState());
        failChange = false; detail.findViewById(R.id.btnConfirmAccountStatus).performClick();
        await(() -> activity.getString(R.string.admin_locked).equals(rowState()));
    }
    @Test public void failedRefreshRetriesOnlyGetAndDoesNotRepeatMutation() throws Exception {
        AlertDialog detail = openStatusForm(); failDetailAfterChange = true;
        confirmStatus(detail, "Review", "");
        await(() -> detail.findViewById(R.id.btnConfirmAccountStatus).isEnabled());
        assertEquals("Unavailable", ((TextView) detail.findViewById(R.id.tvAccountStatusError)).getText().toString());
        assertEquals(activity.getString(R.string.admin_reload), ((TextView) detail.findViewById(R.id.btnConfirmAccountStatus)).getText().toString());
        failDetailAfterChange = false; detail.findViewById(R.id.btnConfirmAccountStatus).performClick();
        await(() -> activity.getString(R.string.admin_locked).equals(rowState()));
        assertEquals(1, writes.size());
    }
    @Test public void successResponseWithoutChangedServerStateDoesNotPretendLocked() throws Exception {
        AlertDialog detail = openStatusForm(); unchangedStatus = true;
        confirmStatus(detail, "Review", "");
        await(() -> detail.findViewById(R.id.btnConfirmAccountStatus).isEnabled());
        assertEquals(activity.getString(R.string.admin_status_unconfirmed), ((TextView) detail.findViewById(R.id.tvAccountStatusError)).getText().toString());
        assertEquals(activity.getString(R.string.admin_active), rowState());
    }
    @Test public void ownAccountHasNoStatusChangeAction() throws Exception {
        loaded(); row(0).performClick(); await(() -> dialog() != null && dialog().isShowing());
        assertEquals(View.GONE, dialog().findViewById(R.id.btnAccountStatus).getVisibility());
        dialog().findViewById(R.id.btnAccountStatus).performClick();
        assertEquals(View.GONE, dialog().findViewById(R.id.layoutAccountStatus).getVisibility());
        assertTrue(writes.isEmpty());
    }

    @Test public void leavingAdminScreenClosesXmlDetailDialog() throws Exception {
        loaded(); row(1).performClick(); await(() -> dialog() != null && dialog().isShowing());
        AlertDialog detail = dialog();
        activity.showScreen(UiScreen.LOGIN);
        activity.getSupportFragmentManager().executePendingTransactions();
        assertFalse(detail.isShowing());
    }
    @Test public void nullBackendListShowsErrorAndKeepsPreviouslyLoadedUsers() throws Exception {
        loaded(); missingList = true; activity.findViewById(R.id.btnReload).performClick();
        await(() -> activity.findViewById(R.id.btnReload).isEnabled() && status().contains(activity.getString(R.string.admin_list_unavailable)));
        assertEquals(2, list().getAdapter().getItemCount());
        missingList = false; activity.findViewById(R.id.btnReload).performClick();
        await(() -> activity.findViewById(R.id.btnReload).isEnabled() && status().isEmpty());
    }
    @Test public void validEmptyBackendListShowsEmptyState() throws Exception {
        loaded(); emptyList = true; activity.findViewById(R.id.btnReload).performClick();
        await(() -> activity.getString(R.string.admin_empty).equals(status()));
        assertEquals(0, list().getAdapter().getItemCount());
        assertEquals(View.GONE, activity.findViewById(R.id.btnMore).getVisibility());
    }
    @Test public void forbiddenResponseShowsErrorAndKeepsExistingUsers() throws Exception {
        loaded(); forbidden = true; activity.findViewById(R.id.btnReload).performClick();
        await(() -> status().contains("Denied")); assertEquals(2, list().getAdapter().getItemCount());
    }
    @Test public void nonAdminCannotLoadAdminScreen() throws Exception {
        loaded(); int before = reads.size();
        preferences.saveLogin(new LoginData("Bearer", "other", "refresh", "self", null), "user@example.com");
        activity.showScreen(UiScreen.ADMIN_USERS); activity.getSupportFragmentManager().executePendingTransactions();
        assertEquals(activity.getString(R.string.admin_denied), status());
        assertEquals(0, list().getAdapter().getItemCount()); assertEquals(before, reads.size());
    }
}
