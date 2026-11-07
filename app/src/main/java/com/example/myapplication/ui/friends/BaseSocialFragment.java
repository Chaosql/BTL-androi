package com.example.myapplication.ui.friends;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.network.ApiError;
import com.example.myapplication.ui.friends.BlockViewModel;
import com.example.myapplication.ui.friends.FriendViewModel;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.base.BindingInflater;
import com.example.myapplication.ui.main.MainActivity;
import java.util.function.Consumer;
import com.example.myapplication.ui.base.UiOperation;

public abstract class BaseSocialFragment<B extends ViewBinding> extends BaseFragment<B> {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private UiOperation<?> activeQuery;
    private int version;
    protected BaseSocialFragment(BindingInflater<B> inflater) { super(inflater); }
    protected boolean signedIn() { return new PreferenceManager(requireContext()).isLogin(); }
    protected FriendViewModel friends() { return viewModel(com.example.myapplication.ui.friends.FriendViewModel.class); }
    protected BlockViewModel blocks() { return viewModel(com.example.myapplication.ui.friends.BlockViewModel.class); }
    protected MainActivity host() { return (MainActivity) requireActivity(); }
    private boolean openingChat;
    protected void openChat(String id, String name) {
        if (openingChat || id == null || id.isEmpty()) return;
        openingChat = true;
        request(viewModel(com.example.myapplication.ui.chat.ConversationViewModel.class)
                .createOrGetDirectConversation(new com.example.myapplication.data.remote.dto.request.DirectChatRequest(id)), response -> {
            openingChat = false;
            if (response.data == null || response.data.id == null) { error(getString(com.example.myapplication.R.string.chat_missing_data)); return; }
            go(com.example.myapplication.ui.main.UiScreen.CHAT, com.example.myapplication.ui.chat.ChatUi.args(response.data.id, name, id));
        }, failure -> { openingChat = false; error(failure.message); });
    }
    protected void error(String message) { Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show(); }
    protected void user(String id, String status) { if (id != null && !id.isEmpty()) new UserInfoDialog(host(), id, status).show(); }
    protected void changed(Runnable reload) {
        getParentFragmentManager().setFragmentResultListener(SocialUi.UPDATED, getViewLifecycleOwner(),
                (key, data) -> reload.run());
    }
    protected void onQueryInvalidated() { }
    protected void invalidateQuery() {
        onQueryInvalidated();
        version++;
        if (activeQuery != null) cancelRequest(activeQuery);
        activeQuery = null;
    }
    protected <T> void query(UiOperation<T> call, Consumer<T> success, Consumer<ApiError> failure) {
        invalidateQuery();
        int current = version;
        activeQuery = call;
        request(call, response -> {
            if (current != version) return;
            activeQuery = null;
            success.accept(response);
        }, error -> {
            if (current != version) return;
            activeQuery = null;
            failure.accept(error);
        });
    }
    protected void search(EditText input, Runnable action) {
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence value, int start, int before, int count) {
                invalidateQuery();
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> { if (binding != null) action.run(); }, 350);
            }
            @Override public void afterTextChanged(Editable value) { }
        });
    }
    protected void cancelSearchDelay() { handler.removeCallbacksAndMessages(null); }
    protected void pageScroll(RecyclerView list, Runnable loadNext) {
        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(RecyclerView view, int dx, int dy) {
                if (dy <= 0) return;
                LinearLayoutManager manager = (LinearLayoutManager) view.getLayoutManager();
                if (manager != null && manager.findLastVisibleItemPosition() >= manager.getItemCount() - 4) loadNext.run();
            }
        });
    }
    @Override public void onDestroyView() {
        openingChat = false;
        handler.removeCallbacksAndMessages(null);
        invalidateQuery();
        super.onDestroyView();
    }
}
