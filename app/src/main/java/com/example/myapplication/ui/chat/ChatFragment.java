package com.example.myapplication.ui.chat;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.response.MessageResponse;
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

public class ChatFragment extends BaseFragment<FragmentChatBinding> {
    private ChatViewModel model;
    private MessageAdapter adapter;
    private String conversation, target, name, self;
    private boolean pickingImage, imageDialogVisible;
    private int revision = -1;
    private final ActivityResultLauncher<String> imagePicker = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                pickingImage = false;
                if (uri != null && binding != null && model.valid() && !model.snapshot().disabled) model.prepareImage(uri);
                else updateControls();
            });
    public ChatFragment() { super(FragmentChatBinding::inflate); }
    @Override protected void onScreenCreated(View root, Bundle saved) {
        model = viewModel(ChatViewModel.class);
        binding.btnBack.setOnClickListener(view -> back());
        Bundle args = getArguments() == null ? new Bundle() : getArguments();
        conversation = args.getString(ChatUi.CONVERSATION, ""); target = args.getString(ChatUi.TARGET, ""); name = args.getString(ChatUi.NAME, "");
        self = new PreferenceManager(requireContext()).getUserId();
        revision = -1; pickingImage = false; imageDialogVisible = false;
        model.initialize(conversation, target, args.getBoolean("is_disabled", false), saved == null ? null : saved.getString("pending_text"),
                saved == null ? 0 : saved.getLong("pending_since", 0), saved == null ? null : saved.getStringArrayList("known_ids"));
        adapter = new MessageAdapter(self, this::actions, id -> new BookingDetailDialog((MainActivity) requireActivity(), id).show(),
                message -> new ImageViewerDialog((MainActivity) requireActivity(), ChatUi.imageUrl(message)).show(getViewLifecycleOwner()));
        LinearLayoutManager manager = new LinearLayoutManager(requireContext()); manager.setStackFromEnd(true);
        binding.rvChatMessages.setLayoutManager(manager); binding.rvChatMessages.setAdapter(adapter);
        binding.tvChatTitle.setText(name.isEmpty() ? getString(R.string.chat_conversations) : name);
        binding.layoutOnlineStatus.setVisibility(View.VISIBLE);
        binding.btnSend.setOnClickListener(view -> { if (binding.btnSend.isEnabled()) model.send(binding.etMessage.getText().toString()); });
        binding.tvEmptyState.setOnClickListener(view -> model.retryOrLoadOlder());
        binding.tvOnlineStatus.setOnClickListener(view -> model.load(0));
        binding.tvChatTitle.setOnClickListener(view -> { if (!target.isEmpty()) new UserInfoDialog((MainActivity) requireActivity(), target, "FRIEND").show(); });
        binding.btnCreateSchedule.setOnClickListener(view -> {
            if (!model.valid() || model.snapshot().disabled) return;
            Bundle data = new Bundle(); data.putString(CreateAppointmentFragment.CONVERSATION_ID, conversation);
            data.putString(CreateAppointmentFragment.TARGET_NAME, name); go(UiScreen.CREATE_APPOINTMENT, data);
        });
        binding.btnGallery.setOnClickListener(view -> {
            if (!binding.btnGallery.isEnabled()) return;
            pickingImage = true; updateControls(); imagePicker.launch("image/*");
        });
        binding.rvChatMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(RecyclerView list, int dx, int dy) {
                if (dy < 0 && manager.findFirstVisibleItemPosition() <= 3) model.loadOlder();
            }
        });
        getParentFragmentManager().setFragmentResultListener(BookingUi.UPDATED, getViewLifecycleOwner(), (key, data) -> model.load(0));
        model.state().observe(getViewLifecycleOwner(), this::render);
        model.errors().observe(getViewLifecycleOwner(), event -> {
            String message = event.consume(); if (message != null) Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        });
        model.confirmed().observe(getViewLifecycleOwner(), event -> {
            String text = event.consume();
            if (text != null && binding.etMessage.getText().toString().trim().equals(text)) binding.etMessage.setText("");
        });
        model.changes().observe(getViewLifecycleOwner(), event -> { if (event.consume() != null) ChatUi.updated(requireActivity()); });
        model.images().observe(getViewLifecycleOwner(), event -> {
            ChatImage image = event.consume(); if (image == null) return;
            model.imageTaken(image);
            if (model.snapshot().disabled) { image.close(); return; }
            imageDialogVisible = true; updateControls();
            new SendImageDialog((MainActivity) requireActivity(), conversation, image, model::imageUploaded, () -> {
                imageDialogVisible = false; updateControls();
            }).show(getViewLifecycleOwner());
        });
    }
    private void render(ChatViewModel.State state) {
        if (state.revision != revision) {
            LinearLayoutManager manager = (LinearLayoutManager) binding.rvChatMessages.getLayoutManager();
            boolean stick = adapter.getItemCount() == 0 || manager.findLastVisibleItemPosition() >= adapter.getItemCount() - 3;
            int before = adapter.getItemCount(), first = manager.findFirstVisibleItemPosition();
            View top = manager.findViewByPosition(first); int offset = top == null ? 0 : top.getTop();
            adapter.submit(state.messages); revision = state.revision;
            if (state.older && first >= 0) manager.scrollToPositionWithOffset(first + adapter.getItemCount() - before, offset);
            else if (stick && adapter.getItemCount() > 0) binding.rvChatMessages.scrollToPosition(adapter.getItemCount() - 1);
        }
        binding.progressBar.setVisibility(state.loading ? View.VISIBLE : View.GONE);
        binding.tvEmptyState.setText(state.forceRefresh && state.notice != 0 ? state.notice : state.messages.isEmpty() ? R.string.chat_empty : R.string.chat_load_older);
        binding.tvEmptyState.setVisibility(!state.loading && (state.forceRefresh || state.messages.isEmpty() || !state.last) ? View.VISIBLE : View.GONE);
        updateControls();
    }
    private void updateControls() {
        if (binding == null || model == null) return;
        ChatViewModel.State state = model.snapshot();
        binding.btnSend.setEnabled(model.valid() && state.loaded && state.connected && !state.disabled && !state.pending
                && !state.preparingImage && !imageDialogVisible && !pickingImage);
        binding.btnGallery.setEnabled(model.valid() && state.loaded && !state.disabled && !state.pending
                && !state.preparingImage && !imageDialogVisible && !pickingImage);
        binding.etMessage.setEnabled(model.valid() && !state.disabled && !state.pending);
        binding.btnCreateSchedule.setEnabled(model.valid() && !state.disabled);
        binding.tvOnlineStatus.setText(state.disabled ? R.string.chat_disabled : state.connected ? R.string.chat_connected : R.string.chat_disconnected);
        binding.viewOnlineIndicator.setVisibility(state.connected ? View.VISIBLE : View.GONE);
    }
    private void actions(MessageResponse message) {
        if (message.id == null) return;
        new MessageActionsDialog((MainActivity) requireActivity(), message, self, action -> {
            if (binding == null) return;
            boolean recall = "recall".equals(action);
            new MaterialAlertDialogBuilder(requireContext()).setMessage(recall ? R.string.chat_recall_confirm : R.string.chat_delete_confirm)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> { if (binding != null) model.mutate(message, recall); }).show();
        }).show(getViewLifecycleOwner());
    }
    @Override public void onStart() { super.onStart(); if (model != null) model.start(); }
    @Override public void onStop() { if (model != null) model.stop(); super.onStop(); }
    @Override public void onSaveInstanceState(@NonNull Bundle state) {
        if (model != null) {
            state.putString("pending_text", model.pendingText()); state.putLong("pending_since", model.pendingSince());
            state.putStringArrayList("known_ids", model.knownIds());
        }
        super.onSaveInstanceState(state);
    }
}
