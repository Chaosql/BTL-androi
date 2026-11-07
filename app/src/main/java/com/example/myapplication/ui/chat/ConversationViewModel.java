package com.example.myapplication.ui.chat;

import android.app.Application;
import com.example.myapplication.ui.base.BaseViewModel;
import com.example.myapplication.ui.base.UiOperation;
import com.example.myapplication.data.repository.chat.ConversationRepository;
import com.example.myapplication.data.remote.dto.request.*;
import com.example.myapplication.data.remote.dto.response.*;
import java.util.List;
import okhttp3.MultipartBody;

public final class ConversationViewModel extends BaseViewModel {
    private final ConversationRepository repository;

    public ConversationViewModel(Application application) {
        super(application);
        repository = new ConversationRepository(application);
    }

    public UiOperation<BaseResponse<PageResponse<ConversationResponse>>> getConversations(Integer page, Integer size, String sort) {
        return execute(repository.getConversations(page, size, sort));
    }

    public UiOperation<BaseResponse<PageResponse<ConversationResponse>>> searchConversations(String keyword, Integer page, Integer size, String sort) {
        return execute(repository.searchConversations(keyword, page, size, sort));
    }

    public UiOperation<BaseResponse<ConversationResponse>> createOrGetDirectConversation(DirectChatRequest request) {
        return execute(repository.createOrGetDirectConversation(request));
    }
    private com.example.myapplication.ui.base.PagedList<ConversationResponse> conversations;
    public com.example.myapplication.ui.base.PagedList<ConversationResponse> conversations() {
        if (conversations == null) conversations = new com.example.myapplication.ui.base.PagedList<>(this,
                (keyword, page) -> keyword.isEmpty() ? getConversations(page, 20, null) : searchConversations(keyword, page, 20, null), item -> item.id);
        return conversations;
    }
}
