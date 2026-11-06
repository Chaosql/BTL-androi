package com.example.myapplication.ui.chat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.response.*;
import com.example.myapplication.data.remote.websocket.ChatSocket;
import com.example.myapplication.data.repository.chat.MessageRepository;
import com.example.myapplication.data.repository.social.FriendRepository;
import com.example.myapplication.databinding.FragmentChatBinding;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.chat.adapter.MessageAdapter;
import com.example.myapplication.ui.chat.component.MessageActionsDialog;
import com.example.myapplication.ui.friends.UserInfoDialog;
import com.example.myapplication.ui.main.MainActivity;
import com.example.myapplication.ui.main.UiScreen;
import com.example.myapplication.ui.schedule.BookingDetailDialog;
import com.example.myapplication.ui.schedule.BookingUi;
import com.example.myapplication.ui.schedule.CreateAppointmentFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonObject;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import retrofit2.Call;

public class ChatFragment extends BaseFragment<FragmentChatBinding> implements ChatSocket.Listener {
    private MessageAdapter adapter;
    private ChatSocket socket;
    private String conversation;
    private String self;
    private String target;
    private String name;
    private boolean connected;
    private boolean disabled;
    private boolean loading;
    private boolean loaded;
    private boolean last;
    private boolean sendingReply;
    private boolean forceRefresh;
    private boolean preparingImage;
    private boolean pickingImage;
    private boolean imageDialogVisible;
    private java.util.concurrent.ExecutorService imageWorker;
    private final ActivityResultLauncher<String> imagePicker = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                pickingImage = false;
                if (uri != null && binding != null && valid() && !disabled) prepareImage(uri);
                else updateControls();
            });
    private int page;
    private Call<?> historyCall;
    private MessageResponse reply;
    private String pendingText;
    private long pendingSince;
    private boolean timedOut;
    private final Set<String> knownBeforeSend = new HashSet<>();
    private final Set<String> mutations = new HashSet<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable confirmationTimeout = () -> {
        if (binding != null && pendingText != null) {
            timedOut = true; notice(R.string.chat_uncertain); load(0);
        }
    };
    private final Runnable refresh = () -> { if (binding != null) load(0); };

    public ChatFragment() { super(FragmentChatBinding::inflate); }
    private MessageRepository messages() { return new MessageRepository(requireContext()); }
    @Override protected void onScreenCreated(View root, Bundle state) {
        Bundle arguments = getArguments() == null ? new Bundle() : getArguments();
        conversation = arguments.getString(ChatUi.CONVERSATION, "");
        target = arguments.getString(ChatUi.TARGET, "");
        name = arguments.getString(ChatUi.NAME, "");
        self = new PreferenceManager(requireContext()).getUserId();
        disabled = arguments.getBoolean("is_disabled", false);
        connected = false; loaded = false; loading = false; last = false; page = -1; sendingReply = false;
        preparingImage = false; pickingImage = false; imageDialogVisible = false;
        imageWorker = java.util.concurrent.Executors.newSingleThreadExecutor();
        mutations.clear();
        if (state != null) {
            pendingText = state.getString("pending_text");
            pendingSince = state.getLong("pending_since", 0);
            ArrayList<String> ids = state.getStringArrayList("known_ids");
            knownBeforeSend.clear(); if (ids != null) knownBeforeSend.addAll(ids);
        }
        adapter = new MessageAdapter(self, this::actions, id -> new BookingDetailDialog((MainActivity) requireActivity(), id).show(),
                message -> new ImageViewerDialog((MainActivity) requireActivity(), ChatUi.imageUrl(message)).show(getViewLifecycleOwner()));
        LinearLayoutManager manager = new LinearLayoutManager(requireContext()); manager.setStackFromEnd(true);
        binding.rvChatMessages.setLayoutManager(manager); binding.rvChatMessages.setAdapter(adapter);
        binding.layoutReplyPreview.setVisibility(View.GONE); reply = null;
        binding.tvChatTitle.setText(name.isEmpty() ? getString(R.string.chat_conversations) : name);
        binding.layoutOnlineStatus.setVisibility(View.VISIBLE);
        binding.btnSend.setOnClickListener(view -> send());
        binding.tvEmptyState.setOnClickListener(view -> {
            if (forceRefresh) load(0);
            else if (!loading) load(loaded && !last ? page + 1 : 0);
        });
        binding.tvOnlineStatus.setOnClickListener(view -> load(0));
        binding.btnCancelReply.setOnClickListener(view -> clearReply());
        binding.tvChatTitle.setOnClickListener(view -> {
            if (!target.isEmpty()) new UserInfoDialog((MainActivity) requireActivity(), target, "FRIEND").show();
        });
        binding.btnCreateSchedule.setOnClickListener(view -> {
            if (!valid() || disabled) return;
            Bundle args = new Bundle(); args.putString(CreateAppointmentFragment.CONVERSATION_ID, conversation);
            args.putString(CreateAppointmentFragment.TARGET_NAME, name); go(UiScreen.CREATE_APPOINTMENT, args);
        });
        binding.btnSticker.setOnClickListener(view -> new MaterialAlertDialogBuilder(requireContext())
                .setItems(new String[]{"👍", "❤", "😂", "😮", "😢", "🙏"}, (dialog, which) ->
                        binding.etMessage.append(new String[]{"👍", "❤", "😂", "😮", "😢", "🙏"}[which])).show());
        binding.btnGallery.setOnClickListener(view -> {
            if (!binding.btnGallery.isEnabled()) return;
            pickingImage = true; updateControls(); imagePicker.launch("image/*");
        });
        binding.btnFolder.setEnabled(false); binding.btnMic.setEnabled(false);
        binding.rvChatMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(RecyclerView list, int dx, int dy) {
                if (dy < 0 && manager.findFirstVisibleItemPosition() <= 3 && !last && !loading) load(page + 1);
            }
        });
        getParentFragmentManager().setFragmentResultListener(BookingUi.UPDATED, getViewLifecycleOwner(), (key, data) -> load(0));
        updateControls();
        if (!valid()) notice(R.string.chat_invalid_conversation);
    }
    private boolean valid() { return !conversation.isEmpty() && !self.isEmpty(); }
    private void updateControls() {
        if (binding == null) return;
        binding.btnSend.setEnabled(valid() && loaded && connected && !disabled && pendingText == null && !sendingReply
                && !preparingImage && !imageDialogVisible && !pickingImage);
        binding.btnGallery.setEnabled(valid() && loaded && !disabled && pendingText == null && !sendingReply
                && !preparingImage && !imageDialogVisible && !pickingImage);
        binding.etMessage.setEnabled(valid() && !disabled && pendingText == null && !sendingReply);
        binding.btnCreateSchedule.setEnabled(valid() && !disabled);
        binding.tvOnlineStatus.setText(disabled ? R.string.chat_disabled : connected ? R.string.chat_connected : R.string.chat_disconnected);
        binding.viewOnlineIndicator.setVisibility(connected ? View.VISIBLE : View.GONE);
    }
    @Override public void onStart() {
        super.onStart();
        if (binding != null && valid()) {
            socket = ChatSocket.create(requireContext(), conversation, this);
            socket.start(); load(0);
            if (pendingText != null) handler.postDelayed(confirmationTimeout, 20000);
        }
    }
    @Override public void onStop() {
        if (socket != null) { socket.close(); socket = null; }
        connected = false; handler.removeCallbacksAndMessages(null); updateControls();
        super.onStop();
    }
    private boolean bottom() {
        LinearLayoutManager manager = (LinearLayoutManager) binding.rvChatMessages.getLayoutManager();
        return adapter.getItemCount() == 0 || manager.findLastVisibleItemPosition() >= adapter.getItemCount() - 3;
    }
    private void load(int next) {
        if (binding == null || !valid()) return;
        if (loading && next > 0) return;
        if (historyCall != null) cancelRequest(historyCall);
        loading = true; binding.progressBar.setVisibility(View.VISIBLE); binding.tvEmptyState.setVisibility(View.GONE);
        Call<BaseResponse<PageResponse<MessageResponse>>> call = messages().getMessages(conversation, next, 30, "createdDate,desc");
        historyCall = call;
        request(call, response -> {
            if (historyCall != call) return;
            loading = false; binding.progressBar.setVisibility(View.GONE);
            if (response.data == null || response.data.content == null) { historyError(getString(R.string.chat_missing_data)); return; }
            boolean stick = !loaded || bottom();
            int before = adapter.getItemCount();
            LinearLayoutManager manager = (LinearLayoutManager) binding.rvChatMessages.getLayoutManager();
            int first = manager.findFirstVisibleItemPosition();
            View top = manager.findViewByPosition(first);
            int offset = top == null ? 0 : top.getTop();
            List<MessageResponse> batch = new ArrayList<>();
            for (MessageResponse item : response.data.content) {
                if (item == null || (item.conversationId != null && !conversation.equals(item.conversationId))) continue;
                batch.add(item);
                if (pendingText != null && !knownBeforeSend.contains(item.id) && self.equals(item.senderId)
                        && pendingText.equals(item.content) && ChatUi.instant(item.createdDate).toEpochMilli() >= pendingSince - 2000) confirmed();
            }
            adapter.merge(batch); loaded = true; page = next;
            last = response.data.totalPages != null && response.data.totalPages > 0
                    ? next + 1 >= response.data.totalPages : Boolean.TRUE.equals(response.data.last);
            if (next > 0 && first >= 0) manager.scrollToPositionWithOffset(first + adapter.getItemCount() - before, offset);
            else if (stick && adapter.getItemCount() > 0) binding.rvChatMessages.scrollToPosition(adapter.getItemCount() - 1);
            if (timedOut && pendingText != null) {
                pendingText = null; timedOut = false; notice(R.string.chat_uncertain);
                Toast.makeText(requireContext(), R.string.chat_uncertain, Toast.LENGTH_LONG).show();
            } else empty();
            updateControls();
        }, failure -> { if (historyCall == call) { loading = false; historyError(failure.message); } });
    }
    private void historyError(String message) {
        binding.progressBar.setVisibility(View.GONE); notice(R.string.chat_retry);
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show(); updateControls();
    }
    private void empty() {
        forceRefresh = false;
        binding.tvEmptyState.setText(adapter.getItemCount() == 0 ? R.string.chat_empty : R.string.chat_load_older);
        binding.tvEmptyState.setVisibility(adapter.getItemCount() == 0 || !last ? View.VISIBLE : View.GONE);
    }
    private void notice(int resource) { forceRefresh = true; binding.tvEmptyState.setText(resource); binding.tvEmptyState.setVisibility(View.VISIBLE); }
    private void send() {
        if (!binding.btnSend.isEnabled()) return;
        String text = binding.etMessage.getText().toString().trim();
        if (text.isEmpty()) return;
        if (reply != null) {
            String id = reply.id; sendingReply = true; updateControls();
            request(messages().replyToMessage(id, text), response -> {
                sendingReply = false;
                if (response.data == null || response.data.id == null
                        || (response.data.conversationId != null && !conversation.equals(response.data.conversationId))) {
                    notice(R.string.chat_missing_data); updateControls(); return;
                }
                adapter.merge(Collections.singletonList(response.data)); binding.etMessage.setText(""); clearReply();
                binding.rvChatMessages.scrollToPosition(adapter.getItemCount() - 1);
                updateControls(); empty(); ChatUi.updated(requireActivity());
            }, failure -> { sendingReply = false; updateControls(); Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show(); });
            return;
        }
        pendingText = text; pendingSince = System.currentTimeMillis(); timedOut = false;
        knownBeforeSend.clear(); for (MessageResponse item : adapter.messages()) knownBeforeSend.add(item.id);
        updateControls();
        if (socket == null || !socket.sendText(text)) {
            pendingText = null; updateControls(); notice(R.string.chat_send_error); return;
        }
        handler.removeCallbacks(confirmationTimeout); handler.postDelayed(confirmationTimeout, 20000);
    }
    private void confirmed() {
        if (pendingText == null) return;
        if (binding.etMessage.getText().toString().trim().equals(pendingText)) binding.etMessage.setText("");
        pendingText = null; timedOut = false; handler.removeCallbacks(confirmationTimeout); updateControls();
        ChatUi.updated(requireActivity());
    }
    private void prepareImage(Uri uri) {
        if (preparingImage || imageDialogVisible || imageWorker == null || imageWorker.isShutdown()) return;
        preparingImage = true; notice(R.string.media_preparing); updateControls();
        View original = binding.getRoot();
        android.content.Context context = requireContext().getApplicationContext();
        String selectedConversation = conversation;
        imageWorker.execute(() -> {
            try {
                ChatImage image = ChatImage.prepare(context.getContentResolver(), uri, context.getCacheDir());
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (binding == null || binding.getRoot() != original || !isAdded() || disabled) { image.close(); return; }
                    preparingImage = false; imageDialogVisible = true; empty(); updateControls();
                    new SendImageDialog((MainActivity) requireActivity(), selectedConversation, image, () -> {
                        if (binding == null || binding.getRoot() != original) return;
                        load(0);
                        handler.postDelayed(refresh, 1000);
                        handler.postDelayed(refresh, 3000);
                    }, () -> {
                        if (binding == null || binding.getRoot() != original) return;
                        imageDialogVisible = false; updateControls();
                    }).show(getViewLifecycleOwner());
                });
            } catch (Exception error) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (binding == null || binding.getRoot() != original) return;
                    preparingImage = false; updateControls(); notice(R.string.media_image_error);
                });
            }
        });
    }
    @Override public void onState(boolean ready) {
        if (binding == null) return;
        connected = ready; updateControls();
        if (ready) load(0);
    }
    @Override public void onError() { if (binding != null) notice(R.string.chat_send_error); }
    @Override public void onMessage(String destination, String body) {
        if (binding == null) return;
        try {
            JsonObject data = ChatUi.data(body);
            if (destination.endsWith("/queue/friendship")) { checkFriendship(0); return; }
            String id = ChatUi.field(data, "conversationId");
            if (!id.isEmpty() && !conversation.equals(id)) return;
            String type = ChatUi.field(data, "type").toUpperCase(Locale.ROOT);
            if ("UNFRIEND".equals(type) || "UNFRIEND".equals(ChatUi.field(data, "content"))) { disabled = true; updateControls(); return; }
            if ("READ".equals(type) || "TYPING".equals(type) || data.has("isTyping")) return;
            MessageResponse incoming = ChatUi.message(data);
            if (incoming.id == null || incoming.id.isEmpty() || incoming.senderId == null || incoming.content == null) {
                if (!ChatUi.field(data, "messageId").isEmpty() && "RECALL".equals(type)) adapter.recall(ChatUi.field(data, "messageId"));
                handler.removeCallbacks(refresh); handler.postDelayed(refresh, 250); return;
            }
            boolean stick = bottom();
            boolean newMessage = !adapter.contains(incoming.id);
            if (pendingText != null && newMessage && self.equals(incoming.senderId) && pendingText.equals(incoming.content)) confirmed();
            adapter.merge(Collections.singletonList(incoming));
            if (reply != null && reply.id.equals(incoming.id) && Boolean.TRUE.equals(incoming.isRecalled)) clearReply();
            if (stick && adapter.getItemCount() > 0) binding.rvChatMessages.scrollToPosition(adapter.getItemCount() - 1);
            empty();
            if (newMessage && socket != null) socket.signal("/app/chat.read", new JsonObject());
            ChatUi.updated(requireActivity());
        } catch (RuntimeException invalid) { handler.removeCallbacks(refresh); handler.postDelayed(refresh, 250); }
    }
    private void checkFriendship(int page) {
        if (target.isEmpty()) return;
        request(new FriendRepository(requireContext()).getFriends(page, 50, null), response -> {
            if (response.data == null || response.data.content == null) return;
            for (FriendDto friend : response.data.content) if (friend != null && target.equals(friend.userId)) { disabled = false; updateControls(); return; }
            boolean last = response.data.totalPages != null && response.data.totalPages > 0
                    ? page + 1 >= response.data.totalPages : Boolean.TRUE.equals(response.data.last);
            if (last) { disabled = true; updateControls(); }
            else checkFriendship(page + 1);
        }, failure -> { });
    }
    private void actions(MessageResponse message) {
        if (message.id == null) return;
        new MessageActionsDialog((MainActivity) requireActivity(), message, self, (action, emoji) -> {
            if (binding == null) return;
            if ("reply".equals(action)) {
                reply = message; binding.layoutReplyPreview.setVisibility(View.VISIBLE);
                binding.tvReplySenderName.setText(ChatUi.value(message.senderName));
                binding.tvReplyMessageContent.setText(ChatUi.value(message.content)); binding.etMessage.requestFocus();
            } else if ("forward".equals(action)) new ForwardMessageDialog((MainActivity) requireActivity(), message.id).show();
            else if ("recall".equals(action) || "delete".equals(action))
                new MaterialAlertDialogBuilder(requireContext()).setMessage("recall".equals(action) ? R.string.chat_recall_confirm : R.string.chat_delete_confirm)
                        .setNegativeButton(R.string.cancel, null).setPositiveButton(R.string.confirm, (dialog, which) -> {
                            if ("recall".equals(action)) mutate(message, messages().recallMessage(message.id), result -> adapter.recall(message.id));
                            else mutate(message, messages().deleteMessage(message.id), result -> adapter.remove(message.id));
                        }).show();
            else if ("reaction".equals(action)) {
                boolean mine = false;
                if (message.reactions != null) for (ReactionResponse reaction : message.reactions)
                    if (reaction != null && emoji.equals(reaction.emoji) && reaction.userIds != null && reaction.userIds.contains(self)) mine = true;
                mutate(message, mine ? messages().removeReaction(message.id) : messages().addReaction(message.id, emoji), result -> load(0));
            }
        }).show();
    }
    private <T> void mutate(MessageResponse message, Call<T> call, Consumer<T> success) {
        if (!mutations.add(message.id)) { call.cancel(); return; }
        request(call, response -> { mutations.remove(message.id); success.accept(response); empty(); ChatUi.updated(requireActivity()); },
                failure -> { mutations.remove(message.id); Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show(); });
    }
    private void clearReply() { reply = null; binding.layoutReplyPreview.setVisibility(View.GONE); }
    @Override public void onSaveInstanceState(@NonNull Bundle state) {
        state.putString("pending_text", pendingText); state.putLong("pending_since", pendingSince);
        state.putStringArrayList("known_ids", new ArrayList<>(knownBeforeSend)); super.onSaveInstanceState(state);
    }
    @Override public void onDestroyView() {
        if (imageWorker != null) imageWorker.shutdownNow();
        handler.removeCallbacksAndMessages(null); if (socket != null) socket.close(); socket = null;
        super.onDestroyView();
    }
}
