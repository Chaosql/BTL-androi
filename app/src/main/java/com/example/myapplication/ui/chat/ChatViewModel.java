package com.example.myapplication.ui.chat;

import android.app.Application;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplication.R;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.response.*;
import com.example.myapplication.data.remote.websocket.ChatSocket;
import com.example.myapplication.data.repository.chat.MessageRepository;
import com.example.myapplication.data.repository.social.FriendRepository;
import com.example.myapplication.ui.base.*;
import com.google.gson.JsonObject;
import java.util.*;
import java.util.concurrent.*;

public final class ChatViewModel extends BaseViewModel implements ChatSocket.Listener {
    public static final class State {
        public final List<MessageResponse> messages;
        public final boolean connected, disabled, loading, loaded, last, pending, preparingImage, forceRefresh, older;
        public final int notice, revision;
        State(ChatViewModel model) {
            messages = Collections.unmodifiableList(new ArrayList<>(model.items));
            connected = model.connected; disabled = model.disabled; loading = model.historyLoading; loaded = model.loaded;
            last = model.last; pending = model.pendingText != null; preparingImage = model.preparingImage;
            forceRefresh = model.forceRefresh; older = model.older; notice = model.notice; revision = model.revision;
        }
    }
    private final MessageRepository repository;
    private final MutableLiveData<State> state = new MutableLiveData<>();
    private final MutableLiveData<UiEvent<String>> errors = new MutableLiveData<>(), confirmed = new MutableLiveData<>();
    private final MutableLiveData<UiEvent<Boolean>> changes = new MutableLiveData<>();
    private final List<MessageResponse> items = new ArrayList<>();
    private final Set<String> deleted = new HashSet<>(), recalled = new HashSet<>(), knownBeforeSend = new HashSet<>(), mutations = new HashSet<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Handler imageResults = new Handler(Looper.getMainLooper());
    private final ExecutorService imageWorker = Executors.newSingleThreadExecutor();
    private ChatSocket socket;
    private UiOperation<?> historyCall;
    private String conversation = "", self = "", target = "", pendingText;
    private long pendingSince;
    private boolean connected, disabled, historyLoading, loaded, last, timedOut, preparingImage, forceRefresh, older, cleared;
    private int page = -1, notice, revision, imageGeneration;
    private ChatImage pendingImage;
    private final Runnable timeout = () -> {
        if (pendingText != null) { timedOut = true; notice(R.string.chat_uncertain); load(0); }
    };
    private final Runnable refresh = () -> load(0);
    public ChatViewModel(Application application) { super(application); repository = new MessageRepository(application); }
    public LiveData<State> state() { return state; }
    public LiveData<UiEvent<String>> errors() { return errors; }
    public LiveData<UiEvent<String>> confirmed() { return confirmed; }
    public LiveData<UiEvent<Boolean>> changes() { return changes; }
    public State snapshot() { return new State(this); }
    public String pendingText() { return pendingText; }
    public long pendingSince() { return pendingSince; }
    public ArrayList<String> knownIds() { return new ArrayList<>(knownBeforeSend); }
    public void initialize(String conversation, String target, boolean disabled, String pending, long since, List<String> known) {
        String owner = new PreferenceManager(getApplication()).getUserId();
        if (this.conversation.equals(conversation) && self.equals(owner) && state.getValue() != null) { publish(); return; }
        this.conversation = conversation; this.target = target; self = owner; this.disabled = disabled;
        pendingText = pending; pendingSince = since;
        knownBeforeSend.clear(); if (known != null) knownBeforeSend.addAll(known);
        items.clear(); deleted.clear(); recalled.clear(); mutations.clear();
        loaded = false; historyLoading = false; last = false; page = -1;
        if (!valid()) { forceRefresh = true; notice = R.string.chat_invalid_conversation; }
        publish();
    }
    public boolean valid() { return !conversation.isEmpty() && !self.isEmpty(); }
    public void start() {
        if (!valid() || socket != null) return;
        socket = ChatSocket.create(getApplication(), conversation, this); socket.start(); load(0);
        if (pendingText != null) handler.postDelayed(timeout, 20000);
    }
    public void stop() {
        if (socket != null) socket.close(); socket = null;
        connected = false; handler.removeCallbacksAndMessages(null); publish();
    }
    public void retryOrLoadOlder() { load(forceRefresh ? 0 : loaded && !last ? page + 1 : 0); }
    public void loadOlder() { if (!last && !historyLoading) load(page + 1); }
    public void load(int next) {
        if (!valid() || cleared || (historyLoading && next > 0)) return;
        if (historyCall != null) historyCall.cancel();
        historyLoading = true; publish();
        UiOperation<BaseResponse<PageResponse<MessageResponse>>> operation = execute(repository.getMessages(conversation, next, 30, "createdDate,desc"));
        historyCall = operation;
        watch(operation, response -> {
            if (historyCall != operation) return;
            historyLoading = false;
            if (response.data == null || response.data.content == null) { historyError(getApplication().getString(R.string.chat_missing_data)); return; }
            List<MessageResponse> batch = new ArrayList<>();
            for (MessageResponse item : response.data.content) {
                if (item == null || (item.conversationId != null && !conversation.equals(item.conversationId))) continue;
                batch.add(item);
                if (pendingText != null && !knownBeforeSend.contains(item.id) && self.equals(item.senderId)
                        && pendingText.equals(item.content) && ChatUi.instant(item.createdDate).toEpochMilli() >= pendingSince - 2000) confirmPending();
            }
            merge(batch); loaded = true; page = next; older = next > 0;
            last = response.data.totalPages != null && response.data.totalPages > 0
                    ? next + 1 >= response.data.totalPages : Boolean.TRUE.equals(response.data.last);
            if (timedOut && pendingText != null) {
                pendingText = null; timedOut = false; notice(R.string.chat_uncertain);
                errors.setValue(new UiEvent<>(getApplication().getString(R.string.chat_uncertain)));
            } else { forceRefresh = false; notice = 0; }
            publish();
        }, error -> { if (historyCall == operation) { historyLoading = false; historyError(error.message); } });
    }
    private void historyError(String message) { notice(R.string.chat_retry); errors.setValue(new UiEvent<>(message)); }
    private void notice(int resource) { forceRefresh = true; notice = resource; publish(); }
    public void send(String text) {
        text = text.trim();
        if (!valid() || !loaded || !connected || disabled || pendingText != null || text.isEmpty()) return;
        pendingText = text; pendingSince = System.currentTimeMillis(); timedOut = false;
        knownBeforeSend.clear(); for (MessageResponse item : items) knownBeforeSend.add(item.id);
        publish();
        if (socket == null || !socket.sendText(text)) { pendingText = null; notice(R.string.chat_send_error); return; }
        handler.removeCallbacks(timeout); handler.postDelayed(timeout, 20000);
    }
    private void confirmPending() {
        if (pendingText == null) return;
        confirmed.setValue(new UiEvent<>(pendingText)); pendingText = null; timedOut = false;
        handler.removeCallbacks(timeout); changes.setValue(new UiEvent<>(true));
    }
    private boolean contains(String id) { for (MessageResponse item : items) if (id.equals(item.id)) return true; return false; }
    private void merge(List<MessageResponse> data) {
        for (MessageResponse item : data) {
            if (item == null || item.id == null || item.id.isEmpty() || deleted.contains(item.id)) continue;
            if (recalled.contains(item.id)) item.isRecalled = true;
            if (Boolean.TRUE.equals(item.isRecalled)) recalled.add(item.id);
            items.removeIf(old -> item.id.equals(old.id)); items.add(item);
        }
        items.sort(Comparator.comparing((MessageResponse item) -> ChatUi.instant(item.createdDate)).thenComparing(item -> item.id)); revision++;
    }
    private void recall(String id) {
        recalled.add(id); for (MessageResponse item : items) if (id.equals(item.id)) item.isRecalled = true; revision++;
    }
    @Override public void onState(boolean ready) { connected = ready; publish(); if (ready) load(0); }
    @Override public void onError() { notice(R.string.chat_send_error); }
    @Override public void onMessage(String destination, String body) {
        if (cleared) return;
        try {
            JsonObject data = ChatUi.data(body);
            if (destination.endsWith("/queue/friendship")) { checkFriendship(0); return; }
            String id = ChatUi.field(data, "conversationId"); if (!id.isEmpty() && !conversation.equals(id)) return;
            String type = ChatUi.field(data, "type").toUpperCase(Locale.ROOT);
            if ("UNFRIEND".equals(type) || "UNFRIEND".equals(ChatUi.field(data, "content"))) { disabled = true; publish(); return; }
            if ("READ".equals(type) || "TYPING".equals(type) || data.has("isTyping")) return;
            MessageResponse incoming = ChatUi.message(data);
            if (incoming.id == null || incoming.id.isEmpty() || incoming.senderId == null || incoming.content == null) {
                if (!ChatUi.field(data, "messageId").isEmpty() && "RECALL".equals(type)) { recall(ChatUi.field(data, "messageId")); publish(); }
                handler.removeCallbacks(refresh); handler.postDelayed(refresh, 250); return;
            }
            boolean isNew = !contains(incoming.id);
            if (pendingText != null && isNew && self.equals(incoming.senderId) && pendingText.equals(incoming.content)) confirmPending();
            merge(Collections.singletonList(incoming)); older = false; forceRefresh = false; notice = 0; publish();
            if (isNew && socket != null) socket.signal("/app/chat.read", new JsonObject()); changes.setValue(new UiEvent<>(true));
        } catch (RuntimeException invalid) { handler.removeCallbacks(refresh); handler.postDelayed(refresh, 250); }
    }
    private void checkFriendship(int page) {
        if (target.isEmpty()) return;
        watch(execute(new FriendRepository((android.content.Context) getApplication()).getFriends(page, 50, null)), response -> {
            if (response.data == null || response.data.content == null) return;
            for (FriendDto friend : response.data.content) if (friend != null && target.equals(friend.userId)) { disabled = false; publish(); return; }
            boolean last = response.data.totalPages != null && response.data.totalPages > 0
                    ? page + 1 >= response.data.totalPages : Boolean.TRUE.equals(response.data.last);
            if (last) { disabled = true; publish(); } else checkFriendship(page + 1);
        }, error -> { });
    }
    public void mutate(MessageResponse message, boolean recall) {
        if (message.id == null || !mutations.add(message.id)) return;
        if (recall && (!self.equals(message.senderId) || Boolean.TRUE.equals(message.isRecalled))) { mutations.remove(message.id); return; }
        watch(execute(recall ? repository.recallMessage(message.id) : repository.deleteMessage(message.id)), response -> {
            mutations.remove(message.id);
            if (recall) recall(message.id); else { deleted.add(message.id); items.removeIf(item -> message.id.equals(item.id)); revision++; }
            forceRefresh = false; notice = 0; publish(); changes.setValue(new UiEvent<>(true));
        }, error -> { mutations.remove(message.id); errors.setValue(new UiEvent<>(error.message)); });
    }
    public void prepareImage(Uri uri) {
        if (preparingImage || cleared || !valid() || disabled || pendingText != null) return;
        preparingImage = true; notice(R.string.media_preparing); int generation = ++imageGeneration;
        String owner = self, destination = conversation;
        imageWorker.execute(() -> {
            try {
                ChatImage image = ChatImage.prepare(getApplication().getContentResolver(), uri, getApplication().getCacheDir());
                imageResults.post(() -> {
                    if (cleared || generation != imageGeneration) { image.close(); return; }
                    if (disabled || !owner.equals(self) || !destination.equals(conversation)) {
                        image.close(); preparingImage = false; publish(); return;
                    }
                    pendingImage = image;
                    notice(R.string.media_uploading);
                    watch(execute(repository.uploadImage(destination, image.part())), response -> {
                        finishImage(image);
                        forceRefresh = false; notice = 0; publish();
                        changes.setValue(new UiEvent<>(true));
                        imageUploaded();
                    }, error -> {
                        finishImage(image);
                        forceRefresh = false; notice = 0; publish();
                        errors.setValue(new UiEvent<>(error.code == 0
                                ? getApplication().getString(R.string.media_uncertain) : error.message));
                    });
                });
            } catch (Exception error) {
                imageResults.post(() -> { if (!cleared && generation == imageGeneration) { preparingImage = false; notice(R.string.media_image_error); } });
            }
        });
    }
    private void finishImage(ChatImage image) {
        image.close();
        if (pendingImage == image) pendingImage = null;
        preparingImage = false;
    }
    public void imageUploaded() { load(0); handler.postDelayed(refresh, 1000); handler.postDelayed(refresh, 3000); }
    private void publish() { if (!cleared) state.setValue(new State(this)); }
    @Override protected void onCleared() {
        cleared = true; imageGeneration++; imageWorker.shutdownNow(); handler.removeCallbacksAndMessages(null);
        if (socket != null) socket.close(); socket = null;
        super.onCleared();
        if (pendingImage != null) pendingImage.close(); pendingImage = null;
    }
}
