package com.example.myapplication.data.remote.websocket;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import com.example.myapplication.BuildConfig;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.interceptor.TokenAuthenticator;
import com.example.myapplication.data.remote.network.RetrofitClient;
import com.google.gson.JsonObject;
import java.util.*;
import java.util.concurrent.TimeUnit;
import okhttp3.*;

public final class ChatSocket {
    public interface Listener {
        void onState(boolean connected);
        void onMessage(String destination, String body);
        void onError();
    }
    public interface Factory { ChatSocket create(Context context, String conversation, Listener listener); }
    private static volatile Factory testFactory;
    public static ChatSocket create(Context context, String conversation, Listener listener) {
        if (testFactory != null) return testFactory.create(context, conversation, listener);
        PreferenceManager preferences = new PreferenceManager(context);
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).connectTimeout(15, TimeUnit.SECONDS)
                .pingInterval(15, TimeUnit.SECONDS)
                .authenticator(new TokenAuthenticator(preferences, RetrofitClient.getApiService(context))).build();
        String url = BuildConfig.API_BASE_URL.replaceFirst("^http", "ws") + "ws/websocket";
        return new ChatSocket(url, client, preferences, conversation, listener);
    }
    @androidx.annotation.VisibleForTesting public static void setFactoryForTests(Factory factory) {
        if (!BuildConfig.DEBUG) throw new IllegalStateException("Debug only");
        testFactory = factory;
    }
    private final String url;
    private final OkHttpClient client;
    private final PreferenceManager preferences;
    private final String conversation;
    private final Listener listener;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private WebSocket socket;
    private volatile int generation;
    private boolean stopped = true;
    private boolean connected;
    private int retries;
    private long received;
    private long sent;
    private long sendInterval;
    private long receiveInterval;
    public ChatSocket(String url, OkHttpClient client, PreferenceManager preferences, String conversation, Listener listener) {
        this.url = url; this.client = client; this.preferences = preferences; this.conversation = conversation; this.listener = listener;
    }
    public void start() { if (!stopped) return; stopped = false; connect(); }
    public boolean isConnected() { return connected; }
    private void connect() {
        if (stopped || !preferences.isLogin()) return;
        int current = ++generation;
        StompCodec codec = new StompCodec();
        Request request = new Request.Builder().url(url).header("Authorization", "Bearer " + preferences.getAccessToken()).build();
        socket = client.newWebSocket(request, new WebSocketListener() {
            @Override public void onOpen(WebSocket webSocket, Response response) {
                handler.post(() -> {
                    if (current != generation || stopped) { webSocket.cancel(); return; }
                    Map<String, String> headers = new LinkedHashMap<>();
                    headers.put("accept-version", "1.1,1.2"); headers.put("heart-beat", "10000,10000");
                    headers.put("Authorization", "Bearer " + preferences.getAccessToken());
                    webSocket.send(StompCodec.build("CONNECT", headers, ""));
                    received = SystemClock.elapsedRealtime();
                    handler.postDelayed(() -> { if (current == generation && !connected) lost(current); }, 15000);
                });
            }
            @Override public void onMessage(WebSocket webSocket, String text) {
                handler.post(() -> {
                    if (current != generation || stopped) return;
                    received = SystemClock.elapsedRealtime();
                    try { for (StompCodec.Frame frame : codec.feed(text)) frame(current, frame); }
                    catch (RuntimeException error) { listener.onError(); lost(current); }
                });
            }
            @Override public void onClosing(WebSocket webSocket, int code, String reason) { webSocket.close(code, null); }
            @Override public void onClosed(WebSocket webSocket, int code, String reason) { handler.post(() -> lost(current)); }
            @Override public void onFailure(WebSocket webSocket, Throwable error, Response response) { handler.post(() -> lost(current)); }
        });
    }
    private void frame(int current, StompCodec.Frame frame) {
        if (current != generation || stopped) return;
        if ("CONNECTED".equals(frame.command)) {
            if (connected) return;
            Map<String, String> headers = new LinkedHashMap<>();
            headers.put("id", "conversation"); headers.put("destination", "/topic/conversation." + conversation); headers.put("ack", "auto");
            socket.send(StompCodec.build("SUBSCRIBE", headers, ""));
            headers.put("id", "friendship"); headers.put("destination", "/user/queue/friendship");
            socket.send(StompCodec.build("SUBSCRIBE", headers, ""));
            String[] heartbeat = frame.headers.getOrDefault("heart-beat", "0,0").split(",");
            long outgoing = Long.parseLong(heartbeat[0]), incoming = Long.parseLong(heartbeat[1]);
            if (outgoing < 0 || incoming < 0) throw new IllegalArgumentException("Invalid heartbeat");
            sendInterval = incoming == 0 ? 0 : Math.max(10000, incoming);
            receiveInterval = outgoing == 0 ? 0 : Math.max(10000, outgoing) * 3;
            connected = true; retries = 0; sent = SystemClock.elapsedRealtime();
            listener.onState(true); signal("/app/chat.read", new JsonObject());
            heartbeat(current);
        } else if ("MESSAGE".equals(frame.command)) {
            String destination = frame.headers.getOrDefault("destination", "");
            if (destination.equals("/topic/conversation." + conversation) || destination.endsWith("/queue/friendship"))
                listener.onMessage(destination, frame.body);
        } else if ("ERROR".equals(frame.command)) { listener.onError(); lost(current); }
    }
    private void heartbeat(int current) {
        handler.postDelayed(() -> {
            if (current != generation || stopped || !connected) return;
            long now = SystemClock.elapsedRealtime();
            if (receiveInterval > 0 && now - received > receiveInterval) { lost(current); return; }
            if (sendInterval > 0 && now - sent >= sendInterval) { socket.send("\n"); sent = now; }
            heartbeat(current);
        }, 1000);
    }
    private void lost(int current) {
        if (stopped || current != generation) return;
        generation++; connected = false;
        if (socket != null) socket.cancel();
        handler.removeCallbacksAndMessages(null);
        listener.onState(false);
        long wait = Math.min(30000, 1000L << Math.min(retries++, 5));
        handler.postDelayed(this::connect, wait);
    }
    public boolean sendText(String content) {
        JsonObject body = new JsonObject();
        body.addProperty("senderId", preferences.getUserId()); body.addProperty("content", content);
        body.addProperty("type", "TEXT"); body.addProperty("messageType", "TEXT");
        return signal("/app/chat.send", body);
    }
    public boolean signal(String destination, JsonObject body) {
        if (!connected || stopped || socket == null) return false;
        body.addProperty("conversationId", conversation);
        Map<String, String> headers = new LinkedHashMap<>(); headers.put("destination", destination); headers.put("content-type", "application/json");
        boolean result = socket.send(StompCodec.build("SEND", headers, body.toString()));
        if (result) sent = SystemClock.elapsedRealtime();
        return result;
    }
    public void close() {
        stopped = true; generation++; connected = false; handler.removeCallbacksAndMessages(null);
        if (socket != null) { socket.send(StompCodec.build("DISCONNECT", Collections.emptyMap(), "")); socket.close(1000, null); socket = null; }
    }
}
