package com.example.myapplication.ui.chat;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.request.ForwardRequest;
import com.example.myapplication.data.remote.dto.response.ConversationResponse;
import com.example.myapplication.data.repository.chat.ConversationRepository;
import com.example.myapplication.data.repository.chat.MessageRepository;
import com.example.myapplication.databinding.DialogForwardSelectBinding;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.chat.adapter.ConversationsAdapter;
import com.example.myapplication.ui.main.MainActivity;
import java.util.*;

public class ForwardMessageDialog extends BasePreviewDialog<DialogForwardSelectBinding> {
    private final String messageId;
    private final List<ConversationResponse> conversations = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ConversationsAdapter adapter;
    private retrofit2.Call<?> query;
    private boolean loading;
    private boolean busy;
    private boolean last;
    private int page;
    public ForwardMessageDialog(MainActivity activity) { this(activity, null); }
    public ForwardMessageDialog(MainActivity activity, String messageId) { super(activity, DialogForwardSelectBinding::inflate); this.messageId = messageId; }
    @Override protected void bind(View content, AlertDialog dialog) {
        binding.tvTitle.setText(R.string.chat_choose_conversation);
        adapter = new ConversationsAdapter(new PreferenceManager(activity).getUserId(), target -> {
            if (busy || messageId == null || Boolean.TRUE.equals(target.hasLeft)) return;
            busy = true; binding.etSearch.setEnabled(false); binding.btnCancel.setEnabled(false); dialog.setCancelable(false);
            request(new MessageRepository(activity).forwardMessage(messageId, new ForwardRequest(target.id)), response -> {
                if (response.data == null || response.data.id == null
                        || (response.data.conversationId != null && !target.id.equals(response.data.conversationId))) {
                    busy = false; binding.etSearch.setEnabled(true); binding.btnCancel.setEnabled(true); dialog.setCancelable(true);
                    Toast.makeText(activity, R.string.chat_missing_data, Toast.LENGTH_LONG).show(); return;
                }
                ChatUi.updated(activity); Toast.makeText(activity, R.string.chat_forwarded, Toast.LENGTH_SHORT).show(); dialog.dismiss();
            }, failure -> {
                busy = false; binding.etSearch.setEnabled(true); binding.btnCancel.setEnabled(true); dialog.setCancelable(true);
                Toast.makeText(activity, failure.message, Toast.LENGTH_LONG).show();
            });
        });
        binding.rvConversations.setLayoutManager(new LinearLayoutManager(activity)); binding.rvConversations.setAdapter(adapter);
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence text, int start, int before, int count) {
                if (query != null) cancelRequest(query);
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> { if (binding != null) load(true); }, 350);
            }
            @Override public void afterTextChanged(Editable text) { }
        });
        binding.tvTitle.setOnClickListener(view -> load(true));
        binding.rvConversations.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(RecyclerView list, int dx, int dy) {
                LinearLayoutManager manager = (LinearLayoutManager) list.getLayoutManager();
                if (dy > 0 && manager.findLastVisibleItemPosition() >= adapter.getItemCount() - 4) load(false);
            }
        });
        load(true);
    }
    private void load(boolean reset) {
        if (messageId == null || busy || binding == null || !new PreferenceManager(activity).isLogin()) return;
        if (!reset && (last || loading)) return;
        if (query != null) cancelRequest(query);
        int next = reset ? 0 : page + 1;
        loading = true;
        String keyword = binding.etSearch.getText().toString().trim();
        ConversationRepository repository = new ConversationRepository(activity);
        retrofit2.Call<com.example.myapplication.data.remote.dto.response.BaseResponse<com.example.myapplication.data.remote.dto.response.PageResponse<ConversationResponse>>> call =
                keyword.isEmpty() ? repository.getConversations(next, 20, null) : repository.searchConversations(keyword, next, 20, null);
        query = call;
        request(call, response -> {
            if (query != call) return;
            loading = false;
            if (response.data == null || response.data.content == null) { binding.tvTitle.setText(R.string.chat_retry); return; }
            if (reset) conversations.clear();
            for (ConversationResponse item : response.data.content) {
                if (item == null || item.id == null || Boolean.TRUE.equals(item.hasLeft)) continue;
                conversations.removeIf(old -> item.id.equals(old.id)); conversations.add(item);
            }
            adapter.submit(conversations); page = next;
            last = response.data.totalPages != null && response.data.totalPages > 0
                    ? next + 1 >= response.data.totalPages : Boolean.TRUE.equals(response.data.last);
            binding.tvTitle.setText(conversations.isEmpty() ? R.string.chat_empty_conversations : R.string.chat_choose_conversation);
        }, failure -> {
            if (query != call) return;
            loading = false; binding.tvTitle.setText(R.string.chat_retry); Toast.makeText(activity, failure.message, Toast.LENGTH_LONG).show();
        });
    }
    @Override protected void onDismissed() { handler.removeCallbacksAndMessages(null); }
}
