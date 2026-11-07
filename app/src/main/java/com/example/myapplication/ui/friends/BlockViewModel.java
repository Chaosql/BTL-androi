package com.example.myapplication.ui.friends;

import android.app.Application;
import com.example.myapplication.ui.base.BaseViewModel;
import com.example.myapplication.ui.base.UiOperation;
import com.example.myapplication.data.repository.social.BlockRepository;
import com.example.myapplication.data.remote.dto.request.*;
import com.example.myapplication.data.remote.dto.response.*;
import java.util.List;
import okhttp3.MultipartBody;

public final class BlockViewModel extends BaseViewModel {
    private final BlockRepository repository;

    public BlockViewModel(Application application) {
        super(application);
        repository = new BlockRepository(application);
    }

    public UiOperation<BaseResponse<CommonResponse>> blockUser(String userId) {
        return execute(repository.blockUser(userId));
    }

    public UiOperation<BaseResponse<CommonResponse>> unblockUser(String userId) {
        return execute(repository.unblockUser(userId));
    }

    public UiOperation<BaseResponse<List<BlockedUserDto>>> getBlockedUsers() {
        return execute(repository.getBlockedUsers());
    }
}
