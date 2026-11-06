package com.example.myapplication;

import android.os.Looper;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.response.*;
import com.example.myapplication.data.remote.network.RetrofitClient;
import com.example.myapplication.data.remote.websocket.ChatSocket;
import com.example.myapplication.ui.chat.ChatUi;
import com.example.myapplication.ui.chat.adapter.MessageAdapter;
import com.example.myapplication.ui.main.MainActivity;
import com.example.myapplication.ui.main.UiScreen;
import com.google.gson.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import okhttp3.*;
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
public class ChatApiUiTest {
    private MockWebServer server;
    private PreferenceManager preferences;
    private ActivityController<MainActivity> controller;
    private MainActivity activity;
    private OkHttpClient wsClient;
    private volatile WebSocket serverSocket;
    private final List<JsonObject> history = Collections.synchronizedList(new ArrayList<>());
    private final List<String> mutations = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final List<String> frames = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final List<String> requests = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final List<String> forwardedTargets = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final List<String> uploads = new java.util.concurrent.CopyOnWriteArrayList<>();
    private volatile boolean autoEcho = true;
    private volatile boolean failAction;
    private volatile boolean failHistory;
    private volatile boolean many;
    private volatile String handshakeAuth;
    private volatile String handshakePath;

    private JsonObject message(String id, String sender, String content) {
        JsonObject value = new JsonObject(); value.addProperty("id", id); value.addProperty("conversationId", "c1");
        value.addProperty("senderId", sender); value.addProperty("senderName", sender.equals("self") ? "Me" : "Peer");
        value.addProperty("content", content); value.addProperty("messageType", "TEXT");
        value.addProperty("createdDate", id.equals("m1") ? "2026-01-01T00:00:00Z" : id.equals("m2") ? "2026-01-01T00:01:00Z" : Instant.now().toString());
        return value;
    }
    private MockResponse response(JsonElement data) {
        JsonObject wrapper = new JsonObject(); wrapper.addProperty("codeStatus", 200); wrapper.add("data", data);
        return new MockResponse().setHeader("Content-Type", "application/json").setBody(wrapper.toString());
    }
    private MockResponse page(JsonArray content, boolean last) {
        JsonObject page = new JsonObject(); page.add("content", content); page.addProperty("last", last); page.addProperty("totalPages", last ? 1 : 2);
        return response(page);
    }
    private JsonObject conversation(String id) {
        JsonObject value = new JsonObject(); value.addProperty("id", id); value.addProperty("groupName", "Peer");
        value.addProperty("type", "DIRECT"); value.addProperty("memberCount", 2); return value;
    }
    private void push(JsonObject value) {
        serverSocket.send("MESSAGE\ndestination:/topic/conversation.c1\n\n" + value + "\0");
    }
    @Before public void setup() throws Exception {
        history.add(message("m1", "peer", "Hello")); history.add(message("m2", "self", "Old message"));
        server = new MockWebServer();
        server.setDispatcher(new okhttp3.mockwebserver.Dispatcher() {
            @Override public MockResponse dispatch(RecordedRequest request) {
                String path = request.getRequestUrl().encodedPath(); requests.add(request.getPath());
                if (path.equals("/ws/websocket")) {
                    handshakeAuth = request.getHeader("Authorization"); handshakePath = request.getPath();
                    return new MockResponse().withWebSocketUpgrade(new WebSocketListener() {
                        @Override public void onOpen(WebSocket socket, Response response) { serverSocket = socket; }
                        @Override public void onMessage(WebSocket socket, String text) {
                            frames.add(text);
                            if (text.startsWith("CONNECT\n")) socket.send("CONNECTED\nversion:1.2\nheart-beat:0,0\n\n\0");
                            if (text.startsWith("SEND\n") && text.contains("destination:/app/chat.send\n") && autoEcho) {
                                String body = text.substring(text.indexOf("\n\n") + 2, text.lastIndexOf('\0'));
                                JsonObject input = JsonParser.parseString(body).getAsJsonObject();
                                JsonObject sent = message("sent-1", "self", input.get("content").getAsString());
                                history.add(sent); push(sent); push(sent);
                            }
                        }
                        @Override public void onClosing(WebSocket socket, int code, String reason) { socket.close(code, reason); }
                    });
                }
                if ("GET".equals(request.getMethod())) {
                    if (path.equals("/photo.png")) return new MockResponse().setHeader("Content-Type", "image/png")
                            .setBody(new okio.Buffer().write(photo()));
                    if (path.equals("/api/v1/chat/conversations/c1/messages")) {
                        if (failHistory) return new MockResponse().setResponseCode(503).setBody("{\"message\":\"Unavailable\"}");
                        JsonArray content = new JsonArray();
                        boolean older = "1".equals(request.getRequestUrl().queryParameter("page"));
                        if (many && older) {
                            JsonObject old = message("m0", "peer", "Older"); old.addProperty("createdDate", "2025-01-01T00:00:00Z"); content.add(old);
                        } else synchronized (history) { for (JsonObject value : history) content.add(value.deepCopy()); }
                        return page(content, !many || older);
                    }
                    if (path.equals("/api/v1/chat/conversations") || path.equals("/api/v1/chat/conversations/search")) {
                        JsonArray content = new JsonArray(); content.add(conversation("c2")); return page(content, true);
                    }
                    if (path.equals("/api/v1/profile/peer")) {
                        JsonObject user = new JsonObject(); user.addProperty("id", "peer"); user.addProperty("firstName", "Peer"); return response(user);
                    }
                    if (path.endsWith("/online-status")) {
                        JsonObject online = new JsonObject(); online.addProperty("isOnline", false); return response(online);
                    }
                    return new MockResponse().setResponseCode(404);
                }
                mutations.add(request.getMethod() + " " + path);
                if (path.equals("/api/v1/chat/conversations/c1/upload")) uploads.add(request.getBody().readUtf8());
                if (failAction) return response(JsonParser.parseString("{\"status\":false,\"message\":\"Rejected\"}"));
                if (path.equals("/api/v1/chat/conversations/c1/upload")) {
                    JsonObject image = message("image-1", "self", server.url("/photo.png").toString());
                    image.addProperty("messageType", "IMAGE");
                    history.add(image);
                    if (serverSocket != null) { push(image); push(image); }
                    return response(JsonParser.parseString("{\"status\":true}"));
                }
                if (path.equals("/api/v1/chat/conversations/direct")) return response(conversation("c1"));
                if (path.endsWith("/reply")) {
                    JsonObject reply = message("reply-1", "self", request.getRequestUrl().queryParameter("content"));
                    JsonObject quote = new JsonObject(); quote.addProperty("id", "m1"); quote.addProperty("senderName", "Peer"); quote.addProperty("content", "Hello");
                    reply.add("replyToMessage", quote); history.add(reply); return response(reply);
                }
                if (path.endsWith("/forward")) {
                    forwardedTargets.add(JsonParser.parseString(request.getBody().readUtf8()).getAsJsonObject().get("targetConversationId").getAsString());
                    JsonObject forwarded = message("forward-1", "self", "Hello"); forwarded.addProperty("conversationId", "c2"); return response(forwarded);
                }
                if (path.endsWith("/recall")) synchronized (history) {
                    for (JsonObject value : history) if (value.get("id").getAsString().equals("m2")) value.addProperty("isRecalled", true);
                }
                if (path.equals("/api/v1/chat/messages/m1") && request.getMethod().equals("DELETE"))
                    history.removeIf(value -> value.get("id").getAsString().equals("m1"));
                if (path.endsWith("/reactions")) synchronized (history) {
                    for (JsonObject value : history) if (value.get("id").getAsString().equals("m1")) {
                        JsonArray reactions = new JsonArray();
                        if (request.getMethod().equals("POST")) {
                            JsonObject reaction = new JsonObject(); reaction.addProperty("emoji", request.getRequestUrl().queryParameter("emoji")); reaction.addProperty("count", 1);
                            JsonArray ids = new JsonArray(); ids.add("self"); reaction.add("userIds", ids); reactions.add(reaction);
                        }
                        value.add("reactions", reactions);
                    }
                }
                return response(JsonParser.parseString("{\"status\":true}"));
            }
        });
        server.start();
        preferences = new PreferenceManager(RuntimeEnvironment.getApplication()); preferences.logout();
        preferences.saveLogin(new LoginData("Bearer", "token", "refresh", "self", null), "test@example.com");
        RetrofitClient.setApiServiceForTests(RetrofitClient.create(server.url("/").toString(), preferences));
        wsClient = new OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).build();
        ChatSocket.setFactoryForTests((context, conversation, listener) ->
                new ChatSocket(server.url("/ws/websocket").toString(), wsClient, preferences, conversation, listener));
        controller = Robolectric.buildActivity(MainActivity.class).setup(); activity = controller.get();
        activity.getSupportFragmentManager().executePendingTransactions();
    }
    @After public void cleanup() throws Exception {
        controller.close(); ChatSocket.setFactoryForTests(null); RetrofitClient.setApiServiceForTests(null); preferences.logout();
        if (serverSocket != null) serverSocket.close(1000, null);
        server.shutdown(); wsClient.connectionPool().evictAll(); wsClient.dispatcher().executorService().shutdown();
    }
    private void await(BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
            Shadows.shadowOf(Looper.getMainLooper()).idle(); Thread.sleep(10);
        }
        assertTrue("Timed out waiting for chat API", condition.getAsBoolean());
    }
    private MessageAdapter adapter() { return (MessageAdapter) ((RecyclerView) activity.findViewById(R.id.rvChatMessages)).getAdapter(); }
    private void open() throws Exception {
        activity.showScreen(UiScreen.CHAT, ChatUi.args("c1", "Peer", "peer"));
        activity.getSupportFragmentManager().executePendingTransactions();
        await(() -> activity.findViewById(R.id.btnSend).isEnabled());
    }
    private View row(String id) {
        MessageAdapter adapter = adapter(); List<MessageResponse> values = adapter.messages();
        int position = -1; for (int i = 0; i < values.size(); i++) if (id.equals(values.get(i).id)) position = i;
        assertTrue(position >= 0);
        RecyclerView list = activity.findViewById(R.id.rvChatMessages);
        MessageAdapter.Holder holder = adapter.createViewHolder(list, adapter.getItemViewType(position));
        adapter.bindViewHolder(holder, position); return holder.itemView;
    }
    private void action(String id, int view) {
        row(id).performLongClick(); ShadowDialog.getLatestDialog().findViewById(view).performClick();
    }
    @Test public void connectsUsingHeadersAndLoadsHistory() throws Exception {
        open(); assertEquals(2, adapter().getItemCount());
        await(() -> frames.stream().anyMatch(frame -> frame.contains("destination:/topic/conversation.c1")));
        assertEquals("Bearer token", handshakeAuth); assertFalse(handshakePath.contains("token"));
        assertTrue(frames.stream().anyMatch(frame -> frame.contains("Authorization:Bearer token")));
        assertTrue(frames.stream().anyMatch(frame -> frame.contains("destination:/topic/conversation.c1")));
        assertEquals("Hello", ((TextView) row("m1").findViewById(R.id.tvMessageContent)).getText().toString());
    }
    @Test public void sendsTextOnceAndConfirmsOnlyOnServerEcho() throws Exception {
        open(); ((EditText) activity.findViewById(R.id.etMessage)).setText("Xin chào");
        activity.findViewById(R.id.btnSend).performClick(); activity.findViewById(R.id.btnSend).performClick();
        await(() -> adapter().getItemCount() == 3 && ((EditText) activity.findViewById(R.id.etMessage)).getText().length() == 0);
        assertEquals(1, frames.stream().filter(frame -> frame.contains("destination:/app/chat.send\n")).count());
    }
    @Test public void missingConfirmationKeepsDraftAndDoesNotInventMessage() throws Exception {
        autoEcho = false; open(); ((EditText) activity.findViewById(R.id.etMessage)).setText("Keep draft");
        activity.findViewById(R.id.btnSend).performClick();
        await(() -> frames.stream().anyMatch(frame -> frame.contains("destination:/app/chat.send\n")));
        serverSocket.close(1000, "Offline");
        await(() -> activity.getString(R.string.chat_disconnected).equals(((TextView) activity.findViewById(R.id.tvOnlineStatus)).getText().toString()));
        assertEquals("Keep draft", ((EditText) activity.findViewById(R.id.etMessage)).getText().toString());
        assertEquals(2, adapter().getItemCount());
    }
    @Test public void ignoresMessagesFromOtherConversation() throws Exception {
        open(); JsonObject foreign = message("foreign", "peer", "Wrong conversation"); foreign.addProperty("conversationId", "other"); push(foreign);
        Thread.sleep(50); Shadows.shadowOf(Looper.getMainLooper()).idle(); assertEquals(2, adapter().getItemCount());
    }
    @Test public void replyUsesOriginalMessageIdAndShowsQuote() throws Exception {
        open(); action("m1", R.id.tvReply);
        assertEquals(View.VISIBLE, activity.findViewById(R.id.layoutReplyPreview).getVisibility());
        ((EditText) activity.findViewById(R.id.etMessage)).setText("Reply text"); activity.findViewById(R.id.btnSend).performClick();
        await(() -> adapter().contains("reply-1"));
        assertTrue(mutations.contains("POST /api/v1/chat/messages/m1/reply"));
        assertEquals(View.VISIBLE, row("reply-1").findViewById(R.id.layoutReplyInBubble).getVisibility());
    }
    @Test public void recallHandlesApplicationErrorThenRetries() throws Exception {
        open(); failAction = true; action("m2", R.id.tvRecall);
        ((AlertDialog) ShadowDialog.getLatestDialog()).getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        await(() -> "Rejected".equals(org.robolectric.shadows.ShadowToast.getTextOfLatestToast()));
        assertEquals("Old message", ((TextView) row("m2").findViewById(R.id.tvMessageContent)).getText().toString());
        failAction = false; action("m2", R.id.tvRecall);
        ((AlertDialog) ShadowDialog.getLatestDialog()).getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        await(() -> activity.getString(R.string.chat_recalled).equals(((TextView) row("m2").findViewById(R.id.tvMessageContent)).getText().toString()));
    }
    @Test public void incomingMessageCannotBeRecalledAndCanBeDeletedLocally() throws Exception {
        open(); row("m1").performLongClick(); android.app.Dialog actions = ShadowDialog.getLatestDialog();
        assertEquals(View.GONE, actions.findViewById(R.id.tvRecall).getVisibility());
        actions.findViewById(R.id.tvDelete).performClick();
        ((AlertDialog) ShadowDialog.getLatestDialog()).getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        await(() -> !adapter().contains("m1")); assertTrue(mutations.contains("DELETE /api/v1/chat/messages/m1"));
    }
    @Test public void reactionCanBeAddedAndRemoved() throws Exception {
        open(); action("m1", R.id.tvEmojiLike);
        await(() -> adapter().messages().stream().anyMatch(message -> "m1".equals(message.id) && message.reactions != null && !message.reactions.isEmpty()));
        action("m1", R.id.tvEmojiLike);
        await(() -> mutations.contains("DELETE /api/v1/chat/messages/m1/reactions"));
        assertTrue(mutations.contains("POST /api/v1/chat/messages/m1/reactions"));
    }
    @Test public void forwardingUsesChosenConversation() throws Exception {
        open(); action("m1", R.id.tvForward);
        android.app.Dialog dialog = ShadowDialog.getLatestDialog();
        RecyclerView choices = dialog.findViewById(R.id.rvConversations);
        await(() -> choices.getAdapter().getItemCount() == 1);
        @SuppressWarnings("rawtypes") RecyclerView.Adapter adapter = choices.getAdapter();
        RecyclerView.ViewHolder holder = adapter.createViewHolder(choices, adapter.getItemViewType(0));
        adapter.bindViewHolder(holder, 0); holder.itemView.performClick();
        await(() -> !dialog.isShowing());
        assertTrue(mutations.contains("POST /api/v1/chat/messages/m1/forward"));
        assertEquals(Collections.singletonList("c2"), forwardedTargets);
    }
    @Test public void historyFailureSupportsRetry() throws Exception {
        failHistory = true;
        activity.showScreen(UiScreen.CHAT, ChatUi.args("c1", "Peer", ""));
        activity.getSupportFragmentManager().executePendingTransactions();
        await(() -> activity.findViewById(R.id.tvEmptyState).getVisibility() == View.VISIBLE
                && activity.findViewById(R.id.progressBar).getVisibility() == View.GONE
                && activity.getString(R.string.chat_connected).equals(((TextView) activity.findViewById(R.id.tvOnlineStatus)).getText().toString())
                && activity.getString(R.string.chat_retry).equals(((TextView) activity.findViewById(R.id.tvEmptyState)).getText().toString()));
        failHistory = false; activity.findViewById(R.id.tvEmptyState).performClick();
        await(() -> activity.findViewById(R.id.btnSend).isEnabled()); assertEquals(2, adapter().getItemCount());
    }
    @Test public void conversationListOpensRealConversationAndClosesSocketOnExit() throws Exception {
        activity.showScreen(UiScreen.CONVERSATIONS); activity.getSupportFragmentManager().executePendingTransactions();
        RecyclerView list = activity.findViewById(R.id.rvFriendsList);
        await(() -> list.getAdapter().getItemCount() == 1);
        activity.showScreen(UiScreen.CHAT, ChatUi.args("c1", "Peer", "")); activity.getSupportFragmentManager().executePendingTransactions();
        await(() -> activity.findViewById(R.id.btnSend).isEnabled());
        activity.getOnBackPressedDispatcher().onBackPressed(); activity.getSupportFragmentManager().executePendingTransactions();
        await(() -> frames.stream().anyMatch(frame -> frame.startsWith("DISCONNECT")));
    }
    @Test public void olderHistoryCanBeLoadedForShortPages() throws Exception {
        many = true; open();
        await(() -> activity.findViewById(R.id.progressBar).getVisibility() == View.GONE);
        activity.findViewById(R.id.tvEmptyState).performClick();
        await(() -> adapter().contains("m0")); assertEquals(3, adapter().getItemCount());
    }
    @Test public void timeoutChecksHistoryAndKeepsDraftWithoutAutomaticResend() throws Exception {
        autoEcho = false; open();
        ((EditText) activity.findViewById(R.id.etMessage)).setText("Unconfirmed");
        activity.findViewById(R.id.btnSend).performClick();
        await(() -> frames.stream().anyMatch(frame -> frame.contains("destination:/app/chat.send\n")));
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofSeconds(21));
        await(() -> activity.findViewById(R.id.btnSend).isEnabled());
        assertEquals("Unconfirmed", ((EditText) activity.findViewById(R.id.etMessage)).getText().toString());
        assertEquals(1, frames.stream().filter(frame -> frame.contains("destination:/app/chat.send\n")).count());
        assertEquals(2, adapter().getItemCount());
    }
    @Test public void pendingDraftSurvivesActivityRecreation() throws Exception {
        autoEcho = false; open();
        ((EditText) activity.findViewById(R.id.etMessage)).setText("Keep across recreation");
        activity.findViewById(R.id.btnSend).performClick();
        await(() -> frames.stream().anyMatch(frame -> frame.contains("destination:/app/chat.send\n")));
        controller.recreate(); activity = controller.get();
        activity.getSupportFragmentManager().executePendingTransactions();
        await(() -> adapter().getItemCount() == 2 && activity.getString(R.string.chat_connected)
                .equals(((TextView) activity.findViewById(R.id.tvOnlineStatus)).getText().toString()));
        assertEquals("Keep across recreation", ((EditText) activity.findViewById(R.id.etMessage)).getText().toString());
        assertEquals(1, frames.stream().filter(frame -> frame.contains("destination:/app/chat.send\n")).count());
        assertFalse(activity.findViewById(R.id.btnSend).isEnabled());
    }
    @Test public void friendProfileStartsChatWithRealConversationId() throws Exception {
        new com.example.myapplication.ui.friends.UserInfoDialog(activity, "peer", "FRIEND").show();
        android.app.Dialog profile = ShadowDialog.getLatestDialog();
        await(() -> profile.findViewById(R.id.btnAddFriend).isEnabled());
        profile.findViewById(R.id.btnAddFriend).performClick();
        await(() -> activity.findViewById(R.id.btnSend) != null && activity.findViewById(R.id.btnSend).isEnabled());
        assertTrue(mutations.contains("POST /api/v1/chat/conversations/direct"));
        assertEquals("Peer", ((TextView) activity.findViewById(R.id.tvChatTitle)).getText().toString());
    }
    private byte[] photo() {
        android.graphics.Bitmap image = android.graphics.Bitmap.createBitmap(8, 4, android.graphics.Bitmap.Config.ARGB_8888);
        image.eraseColor(android.graphics.Color.BLUE);
        java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
        image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, bytes); image.recycle();
        return bytes.toByteArray();
    }
    private int imageFiles() {
        java.io.File[] files = activity.getCacheDir().listFiles((directory, name) -> name.startsWith("chat_image_") || name.startsWith("chat_source_"));
        return files == null ? 0 : files.length;
    }
    private void chooseImage(byte[] content, String path) {
        android.net.Uri uri = android.net.Uri.parse("content://chat.image.ui/" + path);
        org.robolectric.shadows.ShadowContentResolver.registerProviderInternal("chat.image.ui", new ChatImageTest.ImageProvider());
        Shadows.shadowOf(RuntimeEnvironment.getApplication().getContentResolver()).registerInputStream(uri,
                new java.io.ByteArrayInputStream(content));
        activity.findViewById(R.id.btnGallery).performClick();
        org.robolectric.shadows.ShadowActivity.IntentForResult picker = Shadows.shadowOf(activity).getNextStartedActivityForResult();
        assertNotNull(picker);
        assertEquals("image/*", picker.intent.getType());
        activity.getActivityResultRegistry().dispatchResult(picker.requestCode, android.app.Activity.RESULT_OK,
                new android.content.Intent().setData(uri));
    }
    @Test public void selectedImageNeedsConfirmationThenUploadsOnce() throws Exception {
        open();
        ((EditText) activity.findViewById(R.id.etMessage)).setText("Keep text draft");
        int before = imageFiles();
        chooseImage(photo(), "photo.png");
        await(() -> ShadowDialog.getLatestDialog() != null && ShadowDialog.getLatestDialog().findViewById(R.id.btnSendImage) != null);
        android.app.Dialog preview = ShadowDialog.getLatestDialog();
        assertTrue(uploads.isEmpty());
        preview.findViewById(R.id.btnSendImage).performClick();
        preview.findViewById(R.id.btnSendImage).performClick();
        await(() -> !preview.isShowing() && adapter().contains("image-1"));
        assertEquals(1, uploads.size());
        assertTrue(uploads.get(0).contains("name=\"file\""));
        assertTrue(uploads.get(0).contains("Content-Type: image/jpeg"));
        assertEquals("Keep text draft", ((EditText) activity.findViewById(R.id.etMessage)).getText().toString());
        assertEquals(3, adapter().getItemCount());
        await(() -> imageFiles() == before);
    }
    @Test public void cancellingPreviewDoesNotUploadAndRemovesTemporaryImage() throws Exception {
        open(); int before = imageFiles();
        chooseImage(photo(), "photo.png");
        await(() -> ShadowDialog.getLatestDialog() != null && ShadowDialog.getLatestDialog().findViewById(R.id.btnSendImage) != null);
        ShadowDialog.getLatestDialog().findViewById(R.id.btnCancel).performClick();
        assertTrue(uploads.isEmpty());
        await(() -> imageFiles() == before);
        assertTrue(activity.findViewById(R.id.btnGallery).isEnabled());
    }
    @Test public void failedUploadKeepsPreviewForExplicitRetry() throws Exception {
        open(); failAction = true;
        chooseImage(photo(), "photo.png");
        await(() -> ShadowDialog.getLatestDialog() != null && ShadowDialog.getLatestDialog().findViewById(R.id.btnSendImage) != null);
        android.app.Dialog preview = ShadowDialog.getLatestDialog();
        preview.findViewById(R.id.btnSendImage).performClick();
        await(() -> preview.findViewById(R.id.btnSendImage).isEnabled());
        assertTrue(preview.isShowing());
        assertEquals("Rejected", ((TextView) preview.findViewById(R.id.tvStatus)).getText().toString());
        assertEquals(2, adapter().getItemCount());
        failAction = false;
        preview.findViewById(R.id.btnSendImage).performClick();
        await(() -> !preview.isShowing() && adapter().contains("image-1"));
        assertEquals(2, uploads.size());
    }
    @Test public void invalidSelectionDoesNotCallUpload() throws Exception {
        open(); int before = imageFiles();
        chooseImage(new byte[]{1, 2, 3}, "file.pdf");
        await(() -> activity.findViewById(R.id.btnGallery).isEnabled());
        assertTrue(uploads.isEmpty()); assertEquals(before, imageFiles());
        assertEquals(activity.getString(R.string.media_image_error),
                ((TextView) activity.findViewById(R.id.tvEmptyState)).getText().toString());
    }
    @Test public void tappingReceivedImageOpensViewer() throws Exception {
        JsonObject image = message("image-1", "peer", server.url("/photo.png").toString());
        image.addProperty("messageType", "IMAGE"); history.add(image);
        open();
        row("image-1").findViewById(R.id.ivImageContent).performClick();
        android.app.Dialog viewer = ShadowDialog.getLatestDialog();
        assertNotNull(viewer.findViewById(R.id.ivImage));
        viewer.findViewById(R.id.btnClose).performClick(); assertFalse(viewer.isShowing());
    }
    @Test public void localImageUrlsFromMessagesAreNotOpened() {
        MessageResponse image = new MessageResponse(); image.content = "file:///data/data/private/photo.jpg";
        assertNull(ChatUi.imageUrl(image));
        image.content = "content://private/photo";
        assertNull(ChatUi.imageUrl(image));
    }
    @Test public void leavingChatClosesImagePreviewAndRemovesTemporaryFile() throws Exception {
        open(); int before = imageFiles();
        chooseImage(photo(), "photo.png");
        await(() -> ShadowDialog.getLatestDialog() != null && ShadowDialog.getLatestDialog().findViewById(R.id.btnSendImage) != null);
        android.app.Dialog preview = ShadowDialog.getLatestDialog();
        activity.showScreen(UiScreen.PROFILE);
        activity.getSupportFragmentManager().executePendingTransactions();
        await(() -> !preview.isShowing() && imageFiles() == before);
        assertTrue(uploads.isEmpty());
    }
    @Test public void cancellingImagePickerKeepsDraftAndDoesNotUpload() throws Exception {
        open();
        ((EditText) activity.findViewById(R.id.etMessage)).setText("Keep draft");
        activity.findViewById(R.id.btnGallery).performClick();
        org.robolectric.shadows.ShadowActivity.IntentForResult picker = Shadows.shadowOf(activity).getNextStartedActivityForResult();
        assertNotNull(picker);
        activity.getActivityResultRegistry().dispatchResult(picker.requestCode, android.app.Activity.RESULT_CANCELED, null);
        assertTrue(uploads.isEmpty());
        assertEquals("Keep draft", ((EditText) activity.findViewById(R.id.etMessage)).getText().toString());
        assertTrue(activity.findViewById(R.id.btnGallery).isEnabled());
    }
}
