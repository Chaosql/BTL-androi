package com.example.myapplication.data.repository.chat;

import android.content.Context;
import com.example.myapplication.data.repository.BaseRepository;
import com.example.myapplication.data.remote.api.ApiService;
import com.example.myapplication.data.remote.dto.request.*;
import com.example.myapplication.data.remote.dto.response.*;
import java.util.List;
import okhttp3.MultipartBody;
import retrofit2.Call;

public final class ConversationRepository extends BaseRepository {
    public ConversationRepository(Context context) { super(context); }
    public ConversationRepository(ApiService api) { super(api); }

    public Call<BaseResponse<PageResponse<ConversationResponse>>> getConversations(Integer page, Integer size, String sort) {
        return api.getConversations(page, size, sort);
    }

    public Call<BaseResponse<PageResponse<ConversationResponse>>> searchConversations(String keyword, Integer page, Integer size, String sort) {
        return api.searchConversations(keyword, page, size, sort);
    }

    public Call<BaseResponse<ConversationResponse>> createOrGetDirectConversation(DirectChatRequest request) {
        return api.createOrGetDirectConversation(request);
    }
}
