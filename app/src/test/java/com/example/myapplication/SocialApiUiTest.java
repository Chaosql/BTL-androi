package com.example.myapplication;

import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.response.LoginData;
import com.example.myapplication.data.remote.network.RetrofitClient;
import com.example.myapplication.ui.main.MainActivity;
import com.example.myapplication.ui.main.UiScreen;
import com.google.gson.*;
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

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class SocialApiUiTest {
    private MockWebServer server;
    private PreferenceManager preferences;
    private ActivityController<MainActivity> controller;
    private MainActivity activity;
    private final List<String> mutations = Collections.synchronizedList(new ArrayList<>());
    private final Set<String> sent = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> answered = Collections.synchronizedSet(new HashSet<>());
    private volatile boolean failAction;
    private volatile boolean failLoad;
    private volatile boolean removed;
    private volatile boolean blocked;
    private volatile boolean many;
    private volatile boolean secondPage;

    private JsonObject user(String id, String name, String status) {
        JsonObject value = new JsonObject();
        value.addProperty("id", id); value.addProperty("userId", id);
        value.addProperty("firstName", name); value.addProperty("lastName", "Nguyen");
        value.addProperty("phone", "0901234567"); value.addProperty("friendshipStatus", status);
        value.addProperty("requestId", "request-" + id);
        return value;
    }
    private MockResponse response(JsonElement data) {
        JsonObject body = new JsonObject(); body.addProperty("codeStatus", 200); body.add("data", data);
        return new MockResponse().setHeader("Content-Type", "application/json").setBody(body.toString());
    }
    private MockResponse page(JsonArray content, boolean last) {
        JsonObject body = new JsonObject(); body.add("content", content); body.addProperty("last", last);
        body.addProperty("totalPages", last ? 1 : 2);
        return response(body);
    }
    @Before public void setup() throws Exception {
        server = new MockWebServer();
        server.setDispatcher(new Dispatcher() {
            @Override public MockResponse dispatch(RecordedRequest request) {
                String path = request.getRequestUrl().encodedPath();
                if ("GET".equals(request.getMethod())) {
                    if (failLoad) return new MockResponse().setResponseCode(503).setBody("{\"message\":\"Unavailable\"}");
                    if (path.equals("/api/v1/users/search")) {
                        JsonArray users = new JsonArray();
                        users.add(user("self", "Self", "NONE"));
                        users.add(user("new", "New", sent.contains("new") ? "PENDING" : "NONE"));
                        users.add(user("pending", "Pending", "PENDING"));
                        users.add(user("friend", "Friend", "ACCEPTED"));
                        String keyword = request.getRequestUrl().queryParameter("keyword");
                        if ("old".equals(keyword)) return response(users).setBodyDelay(250, TimeUnit.MILLISECONDS);
                        if ("latest".equals(keyword)) { users = new JsonArray(); users.add(user("latest", "Latest", "NONE")); }
                        return response(users);
                    }
                    if (path.equals("/api/v1/friends/requests/pending")) {
                        JsonArray requests = new JsonArray();
                        if (!answered.contains("request-a")) requests.add(user("a", "A", "PENDING"));
                        if (!answered.contains("request-b")) requests.add(user("b", "B", "PENDING"));
                        return page(requests, true);
                    }
                    if (path.equals("/api/v1/friends") || path.equals("/api/v1/friends/search")) {
                        JsonArray friends = new JsonArray();
                        boolean next = "1".equals(request.getRequestUrl().queryParameter("page"));
                        if (next) secondPage = true;
                        if (!removed) {
                            if (many) {
                                for (int i = next ? 20 : 0; i < (next ? 21 : 20); i++) friends.add(user("f" + i, "Friend" + i, "FRIEND"));
                            } else friends.add(user("target", "Target", "FRIEND"));
                        }
                        return page(friends, !many || next);
                    }
                    if (path.equals("/api/v1/users/blocked")) {
                        JsonArray users = new JsonArray();
                        if (blocked) users.add(user("target", "Target", "NONE"));
                        return response(users);
                    }
                    if (path.startsWith("/api/v1/profile/")) {
                        JsonObject target = user(path.substring(path.lastIndexOf('/') + 1), "Target", "FRIEND");
                        target.addProperty("university", "Real school");
                        return response(target);
                    }
                    if (path.endsWith("/online-status")) {
                        JsonObject status = new JsonObject(); status.addProperty("isOnline", true); return response(status);
                    }
                    return new MockResponse().setResponseCode(404);
                }
                mutations.add(request.getMethod() + " " + path);
                if (failAction) {
                    JsonObject failure = new JsonObject(); failure.addProperty("status", false); failure.addProperty("message", "Rejected");
                    return response(failure);
                }
                if (path.startsWith("/api/v1/friends/request/")) {
                    if ("POST".equals(request.getMethod())) sent.add(path.substring(path.lastIndexOf('/') + 1));
                    else answered.add(path.split("/")[5]);
                }
                if (path.equals("/api/v1/friends/target")) removed = true;
                if (path.equals("/api/v1/users/block/target")) blocked = "POST".equals(request.getMethod());
                JsonObject success = new JsonObject(); success.addProperty("status", true); return response(success);
            }
        });
        server.start();
        preferences = new PreferenceManager(RuntimeEnvironment.getApplication());
        preferences.logout();
        preferences.saveLogin(new LoginData("Bearer", "token", "refresh", "self", null), "test@example.com");
        RetrofitClient.setApiServiceForTests(RetrofitClient.create(server.url("/").toString(), preferences));
        controller = Robolectric.buildActivity(MainActivity.class).setup();
        activity = controller.get();
        activity.getSupportFragmentManager().executePendingTransactions();
    }
    @After public void cleanup() throws Exception {
        controller.close(); RetrofitClient.setApiServiceForTests(null); preferences.logout(); server.shutdown();
    }
    private void open(UiScreen screen) {
        activity.showScreen(screen); activity.getSupportFragmentManager().executePendingTransactions();
    }
    private RecyclerView list(int id) { return activity.findViewById(id); }
    private void await(BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
            Shadows.shadowOf(Looper.getMainLooper()).idle(); Thread.sleep(10);
        }
        assertTrue("Timed out waiting for social API", condition.getAsBoolean());
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    private View row(int list, int position) {
        RecyclerView view = list(list);
        RecyclerView.Adapter adapter = view.getAdapter();
        RecyclerView.ViewHolder holder = adapter.createViewHolder(view, adapter.getItemViewType(position));
        adapter.bindViewHolder(holder, position);
        return holder.itemView;
    }
    private void search(String keyword) {
        ((EditText) activity.findViewById(R.id.edtKeyword)).setText(keyword);
        activity.findViewById(R.id.btnSearch).performClick();
    }

    @Test public void friendsUseRealDataAndShowCorrectUserProfile() throws Exception {
        open(UiScreen.FRIENDS);
        await(() -> list(R.id.rvFriendsList).getAdapter().getItemCount() == 1);
        View row = row(R.id.rvFriendsList, 0);
        assertEquals("Nguyen Target", ((TextView) row.findViewById(R.id.tvFriendName)).getText().toString());
        row.performClick();
        android.app.Dialog dialog = ShadowDialog.getLatestDialog();
        await(() -> ((TextView) dialog.findViewById(R.id.tvSchool)).getText().toString().equals("Real school"));
        assertEquals(activity.getString(R.string.social_friend), ((TextView) dialog.findViewById(R.id.btnAddFriend)).getText().toString());
        assertFalse(dialog.findViewById(R.id.btnAddFriend).isEnabled());
        dialog.dismiss();
    }
    @Test public void searchFiltersSelfAndDisablesExistingRelationships() throws Exception {
        open(UiScreen.SEARCH_USER); search("someone");
        await(() -> list(R.id.rvSearchResults).getAdapter().getItemCount() == 3);
        assertTrue(row(R.id.rvSearchResults, 0).findViewById(R.id.btnAction).isEnabled());
        assertFalse(row(R.id.rvSearchResults, 1).findViewById(R.id.btnAction).isEnabled());
        assertFalse(row(R.id.rvSearchResults, 2).findViewById(R.id.btnAction).isEnabled());
    }
    @Test public void sendRequestIsNotDuplicatedAndUpdatesButton() throws Exception {
        open(UiScreen.SEARCH_USER); search("someone");
        await(() -> list(R.id.rvSearchResults).getAdapter().getItemCount() == 3);
        View button = row(R.id.rvSearchResults, 0).findViewById(R.id.btnAction);
        button.performClick(); button.performClick();
        await(() -> activity.getString(R.string.social_sent).equals(
                ((TextView) row(R.id.rvSearchResults, 0).findViewById(R.id.btnAction)).getText().toString()));
        assertEquals(Collections.singletonList("POST /api/v1/friends/request/new"), mutations);
    }
    @Test public void rejectedSendKeepsButtonAvailable() throws Exception {
        failAction = true;
        open(UiScreen.SEARCH_USER); search("someone");
        await(() -> list(R.id.rvSearchResults).getAdapter().getItemCount() == 3);
        row(R.id.rvSearchResults, 0).findViewById(R.id.btnAction).performClick();
        await(() -> "Rejected".equals(org.robolectric.shadows.ShadowToast.getTextOfLatestToast()));
        assertTrue(row(R.id.rvSearchResults, 0).findViewById(R.id.btnAction).isEnabled());
    }
    @Test public void acceptAndRejectUseRequestIds() throws Exception {
        open(UiScreen.PENDING_REQUESTS);
        await(() -> list(R.id.rvPendingRequests).getAdapter().getItemCount() == 2);
        row(R.id.rvPendingRequests, 0).findViewById(R.id.btnAccept).performClick();
        await(() -> list(R.id.rvPendingRequests).getAdapter().getItemCount() == 1
                && activity.findViewById(R.id.progressBar).getVisibility() == View.GONE);
        row(R.id.rvPendingRequests, 0).findViewById(R.id.btnReject).performClick();
        await(() -> list(R.id.rvPendingRequests).getAdapter().getItemCount() == 0);
        assertTrue(mutations.contains("PUT /api/v1/friends/request/request-a/accept"));
        assertTrue(mutations.contains("PUT /api/v1/friends/request/request-b/reject"));
    }
    @Test public void unfriendRequiresConfirmationAndRefreshesList() throws Exception {
        open(UiScreen.FRIENDS);
        await(() -> list(R.id.rvFriendsList).getAdapter().getItemCount() == 1);
        row(R.id.rvFriendsList, 0).findViewById(R.id.btnMore).performClick();
        ShadowDialog.getLatestDialog().findViewById(R.id.tvOptionDelete).performClick();
        android.app.Dialog confirmation = ShadowDialog.getLatestDialog();
        assertTrue(mutations.isEmpty());
        confirmation.findViewById(R.id.btnConfirm).performClick();
        await(() -> list(R.id.rvFriendsList).getAdapter().getItemCount() == 0);
        assertEquals("DELETE /api/v1/friends/target", mutations.get(0));
    }
    @Test public void blockAndUnblockUseSelectedUserId() throws Exception {
        open(UiScreen.FRIENDS);
        await(() -> list(R.id.rvFriendsList).getAdapter().getItemCount() == 1);
        row(R.id.rvFriendsList, 0).findViewById(R.id.btnMore).performClick();
        ShadowDialog.getLatestDialog().findViewById(R.id.tvOptionBlock).performClick();
        android.app.Dialog confirmation = ShadowDialog.getLatestDialog();
        confirmation.findViewById(R.id.btnConfirm).performClick();
        await(() -> !confirmation.isShowing());
        open(UiScreen.BLOCKED_USERS);
        await(() -> list(R.id.rvBlockedUsers).getAdapter().getItemCount() == 1);
        row(R.id.rvBlockedUsers, 0).findViewById(R.id.btnUnblock).performClick();
        ShadowDialog.getLatestDialog().findViewById(R.id.btnConfirm).performClick();
        await(() -> list(R.id.rvBlockedUsers).getAdapter().getItemCount() == 0);
        assertTrue(mutations.contains("POST /api/v1/users/block/target"));
        assertTrue(mutations.contains("DELETE /api/v1/users/block/target"));
    }
    @Test public void olderSearchCannotReplaceLatestResults() throws Exception {
        open(UiScreen.SEARCH_USER); search("old");
        await(() -> server.getRequestCount() > 0);
        search("latest");
        await(() -> list(R.id.rvSearchResults).getAdapter().getItemCount() == 1);
        Thread.sleep(300);
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        assertEquals("Nguyen Latest", ((TextView) row(R.id.rvSearchResults, 0).findViewById(R.id.tvUserName)).getText().toString());
    }
    @Test public void loadingFailureSupportsRetry() throws Exception {
        failLoad = true; open(UiScreen.FRIENDS);
        await(() -> activity.findViewById(R.id.tvEmptyState).getVisibility() == View.VISIBLE);
        failLoad = false;
        activity.findViewById(R.id.tvEmptyState).performClick();
        await(() -> list(R.id.rvFriendsList).getAdapter().getItemCount() == 1);
    }
    @Test public void scrollLoadsNextPage() throws Exception {
        many = true; open(UiScreen.FRIENDS);
        await(() -> list(R.id.rvFriendsList).getAdapter().getItemCount() == 20);
        RecyclerView list = list(R.id.rvFriendsList);
        list.measure(View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY));
        list.layout(0, 0, 800, 600);
        list.scrollBy(0, 100000);
        await(() -> list.getAdapter().getItemCount() == 21);
        assertTrue(secondPage);
    }
}
