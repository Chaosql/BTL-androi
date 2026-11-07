package com.example.myapplication.ui.chat;

import android.app.Application;
import com.example.myapplication.ui.base.BaseViewModel;
import com.example.myapplication.ui.base.UiOperation;
import com.example.myapplication.data.repository.chat.MessageRepository;
import com.example.myapplication.data.remote.dto.request.*;
import com.example.myapplication.data.remote.dto.response.*;
import java.util.List;
import okhttp3.MultipartBody;

public final class MessageViewModel extends BaseViewModel {
    private final MessageRepository repository;

    public MessageViewModel(Application application) {
        super(application);
        repository = new MessageRepository(application);
    }

    public UiOperation<BaseResponse<PageResponse<MessageResponse>>> getMessages(String id, Integer page, Integer size, String sort) {
        return execute(repository.getMessages(id, page, size, sort));
    }

    public UiOperation<BaseResponse<Object>> recallMessage(String msgId) {
        return execute(repository.recallMessage(msgId));
    }

    public UiOperation<BaseResponse<Object>> deleteMessage(String msgId) {
        return execute(repository.deleteMessage(msgId));
    }

    public UiOperation<BaseResponse<Object>> uploadImage(String conversationId, MultipartBody.Part file) {
        return execute(repository.uploadImage(conversationId, file));
    }

    public UiOperation<BaseResponse<Object>> scheduleInvite(String conversationId, ScheduleInviteRequest request) {
        return execute(repository.scheduleInvite(conversationId, request));
    }
}
