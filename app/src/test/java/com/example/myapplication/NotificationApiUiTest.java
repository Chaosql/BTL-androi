package com.example.myapplication;

import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.response.LoginData;
import com.example.myapplication.data.remote.network.RetrofitClient;
import com.example.myapplication.ui.main.*;
import com.example.myapplication.ui.chat.ChatUi;
import com.example.myapplication.ui.chat.ChatFragment;
import com.example.myapplication.ui.chat.ConversationsFragment;
import com.example.myapplication.ui.friends.PendingRequestsFragment;
import com.google.gson.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import okhttp3.mockwebserver.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class NotificationApiUiTest {
    private MockWebServer server;
    private PreferenceManager preferences;
    private ActivityController<MainActivity> controller;
    private MainActivity activity;
    private final List<String> mutations = Collections.synchronizedList(new ArrayList<>());
    private volatile boolean failLoad, failAction, empty, secondPage;
    private String type = "UNKNOWN", target = "";
    private MockResponse response(JsonElement data) {
        JsonObject body = new JsonObject(); body.addProperty("codeStatus", 200); body.add("data", data);
        return new MockResponse().setHeader("Content-Type", "application/json").setBody(body.toString());
    }
    private JsonObject item(String id, boolean old) {
        JsonObject value = new JsonObject(); value.addProperty("id", id); value.addProperty("content", "Real " + id);
        value.addProperty("type", type); value.addProperty("targetId", target); value.addProperty("isRead", false);
        value.addProperty("createdDate", old ? "2025-01-01T12:00:00Z" : Instant.now().toString()); return value;
    }
    @Before public void setup() throws Exception {
        server = new MockWebServer();
        server.setDispatcher(new Dispatcher() {
            @Override public MockResponse dispatch(RecordedRequest request) {
                String path = request.getRequestUrl().encodedPath();
                if (path.equals("/api/v1/notifications/unread-count")) return response(new JsonPrimitive(empty ? 0 : 3));
                if (path.equals("/api/v1/notifications")) {
                    if (failLoad) return new MockResponse().setResponseCode(503);
                    boolean next = "1".equals(request.getRequestUrl().queryParameter("page"));
                    if (next) secondPage = true;
                    JsonArray rows = new JsonArray();
                    if (!empty) { rows.add(item("n1", false)); rows.add(item(next ? "n3" : "n2", true)); }
                    JsonObject page = new JsonObject(); page.add("content", rows); page.addProperty("last", empty || next);
                    return response(page);
                }
                if ("PATCH".equals(request.getMethod())) {
                    mutations.add(path);
                    JsonObject status = new JsonObject(); status.addProperty("status", !failAction); return response(status);
                }
                return new MockResponse().setResponseCode(404);
            }
        });
        server.start(); preferences = new PreferenceManager(RuntimeEnvironment.getApplication()); preferences.logout();
        preferences.saveLogin(new LoginData("Bearer", "token", "refresh", "self", null), "test@example.com");
        RetrofitClient.setApiServiceForTests(RetrofitClient.create(server.url("/").toString(), preferences));
        controller = Robolectric.buildActivity(MainActivity.class).setup(); activity = controller.get();
        activity.getSupportFragmentManager().executePendingTransactions();
    }
    @After public void cleanup() throws Exception {
        controller.close(); RetrofitClient.setApiServiceForTests(null); preferences.logout(); server.shutdown();
    }
    private void open() { activity.showScreen(UiScreen.NOTIFICATIONS); activity.getSupportFragmentManager().executePendingTransactions(); }
    private RecyclerView list(int id) { return activity.findViewById(id); }
    private void await(BooleanSupplier check) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(8);
        while (!check.getAsBoolean() && System.nanoTime() < deadline) {
            Shadows.shadowOf(Looper.getMainLooper()).idle(); Thread.sleep(10);
        }
        assertTrue("Notification API timeout", check.getAsBoolean());
    }
    private void loaded() throws Exception {
        await(() -> list(R.id.rvRecentNotifications).getAdapter().getItemCount() == 1
                && activity.findViewById(R.id.tvNotificationStatus).getVisibility() == View.GONE);
    }
    @SuppressWarnings({"rawtypes", "unchecked"}) private View row(int id) {
        RecyclerView list = list(id); RecyclerView.Adapter adapter = list.getAdapter();
        RecyclerView.ViewHolder holder = adapter.createViewHolder(list, 0); adapter.bindViewHolder(holder, 0); return holder.itemView;
    }
    @Test public void loadsRealRowsAndDeduplicatesPages() throws Exception {
        open(); loaded();
        assertEquals("Real n1", ((TextView) row(R.id.rvRecentNotifications).findViewById(R.id.tvContent)).getText().toString());
        activity.findViewById(R.id.tvNotificationMore).performClick();
        await(() -> list(R.id.rvEarlierNotifications).getAdapter().getItemCount() == 2);
        assertTrue(secondPage); assertEquals(1, list(R.id.rvRecentNotifications).getAdapter().getItemCount());
        assertEquals(View.GONE, activity.findViewById(R.id.tvNotificationMore).getVisibility());
    }
    @Test public void marksOneReadOnlyAfterServerSuccess() throws Exception {
        open(); loaded(); row(R.id.rvRecentNotifications).performClick();
        await(() -> row(R.id.rvRecentNotifications).findViewById(R.id.viewUnreadDot).getVisibility() == View.GONE);
        assertEquals(Collections.singletonList("/api/v1/notifications/n1/read"), mutations);
    }
    @Test public void markAllUpdatesBothSectionsAndBadge() throws Exception {
        open(); loaded(); await(() -> activity.findViewById(R.id.tvMarkAllRead).isEnabled());
        activity.findViewById(R.id.tvMarkAllRead).performClick();
        await(() -> row(R.id.rvEarlierNotifications).findViewById(R.id.viewUnreadDot).getVisibility() == View.GONE);
        assertEquals(View.GONE, row(R.id.rvRecentNotifications).findViewById(R.id.viewUnreadDot).getVisibility());
        assertFalse(activity.findViewById(R.id.tvMarkAllRead).isEnabled());
        assertEquals(Collections.singletonList("/api/v1/notifications/read-all"), mutations);
    }
    @Test public void rejectedMutationKeepsUnreadAndCanRetry() throws Exception {
        failAction = true; open(); loaded(); row(R.id.rvRecentNotifications).performClick();
        await(() -> mutations.size() == 1 && activity.findViewById(R.id.tvMarkAllRead).isEnabled());
        assertEquals(View.VISIBLE, row(R.id.rvRecentNotifications).findViewById(R.id.viewUnreadDot).getVisibility());
        failAction = false; row(R.id.rvRecentNotifications).performClick();
        await(() -> row(R.id.rvRecentNotifications).findViewById(R.id.viewUnreadDot).getVisibility() == View.GONE);
    }
    @Test public void failedLoadRetriesAndEmptyStateIsReal() throws Exception {
        failLoad = true; open();
        await(() -> ((TextView) activity.findViewById(R.id.tvNotificationStatus)).getText().toString().contains("Bấm"));
        failLoad = false; empty = true; activity.findViewById(R.id.tvNotificationStatus).performClick();
        await(() -> activity.findViewById(R.id.layoutEmptyState).getVisibility() == View.VISIBLE);
        assertEquals(0, list(R.id.rvRecentNotifications).getAdapter().getItemCount());
    }
    @Test public void friendRequestOpensPendingRequests() throws Exception {
        type = "FRIEND_REQUEST"; target = "request-id"; open(); loaded(); row(R.id.rvRecentNotifications).performClick();
        await(() -> { activity.getSupportFragmentManager().executePendingTransactions();
            return activity.getSupportFragmentManager().findFragmentById(R.id.screenHost) instanceof PendingRequestsFragment; });
    }
    @Test public void conversationUsesActualTargetId() throws Exception {
        type = "CONVERSATION"; target = "conversation-42"; open(); loaded(); row(R.id.rvRecentNotifications).performClick();
        await(() -> { activity.getSupportFragmentManager().executePendingTransactions();
            return activity.getSupportFragmentManager().findFragmentById(R.id.screenHost) instanceof ChatFragment; });
        assertEquals(target, activity.getSupportFragmentManager().findFragmentById(R.id.screenHost).getArguments().getString(ChatUi.CONVERSATION));
    }
    @Test public void mapShowsUnreadCount() throws Exception {
        await(() -> activity.findViewById(R.id.tvNotificationBadge).getVisibility() == View.VISIBLE);
        assertEquals("3", ((TextView) activity.findViewById(R.id.tvNotificationBadge)).getText().toString());
    }
    @Test public void messageIdIsNeverUsedAsConversationId() throws Exception {
        type = "MESSAGE"; target = "message-42"; open(); loaded(); row(R.id.rvRecentNotifications).performClick();
        await(() -> { activity.getSupportFragmentManager().executePendingTransactions();
            return activity.getSupportFragmentManager().findFragmentById(R.id.screenHost) instanceof ConversationsFragment; });
    }
    @Test public void failedMarkAllDoesNotClearUnreadDots() throws Exception {
        failAction = true; open(); loaded(); await(() -> activity.findViewById(R.id.tvMarkAllRead).isEnabled());
        activity.findViewById(R.id.tvMarkAllRead).performClick();
        await(() -> mutations.size() == 1 && activity.findViewById(R.id.tvMarkAllRead).isEnabled());
        assertEquals(View.VISIBLE, row(R.id.rvRecentNotifications).findViewById(R.id.viewUnreadDot).getVisibility());
        assertEquals(View.VISIBLE, row(R.id.rvEarlierNotifications).findViewById(R.id.viewUnreadDot).getVisibility());
    }
}
