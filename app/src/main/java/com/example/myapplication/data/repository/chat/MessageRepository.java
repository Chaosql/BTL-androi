package com.example.myapplication.data.repository.chat;

import android.content.Context;
import com.example.myapplication.data.repository.BaseRepository;
import com.example.myapplication.data.remote.api.ApiService;
import com.example.myapplication.data.remote.dto.request.*;
import com.example.myapplication.data.remote.dto.response.*;
import java.util.List;
import okhttp3.MultipartBody;
import retrofit2.Call;

public final class MessageRepository extends BaseRepository {
    public MessageRepository(Context context) { super(context); }
    public MessageRepository(ApiService api) { super(api); }

    public Call<BaseResponse<PageResponse<MessageResponse>>> getMessages(String id, Integer page, Integer size, String sort) {
        return api.getMessages(id, page, size, sort);
    }



    public Call<BaseResponse<Object>> recallMessage(String msgId) {
        return api.recallMessage(msgId);
    }

    public Call<BaseResponse<Object>> deleteMessage(String msgId) {
        return api.deleteMessage(msgId);
    }









    public Call<BaseResponse<Object>> uploadImage(String conversationId, MultipartBody.Part file) {
        return api.uploadImage(conversationId, file);
    }

    public Call<BaseResponse<Object>> scheduleInvite(String conversationId, ScheduleInviteRequest request) {
        return api.scheduleInvite(conversationId, request);
    }
}
