package com.example.myapplication.data.repository.profile;

import android.content.Context;
import com.example.myapplication.data.repository.BaseRepository;
import com.example.myapplication.data.remote.api.ApiService;
import com.example.myapplication.data.remote.dto.request.*;
import com.example.myapplication.data.remote.dto.response.*;
import java.util.List;
import okhttp3.MultipartBody;
import retrofit2.Call;

public final class ProfileRepository extends BaseRepository {
    public ProfileRepository(Context context) { super(context); }
    public ProfileRepository(ApiService api) { super(api); }

    public Call<BaseResponse<UserDto>> getCurrentUser() {
        return api.getCurrentUser();
    }

    public Call<BaseResponse<UserDto>> getProfile(String userId) {
        return api.getProfile(userId);
    }

    public Call<BaseResponse<UserDto>> updateStatusTag(String userId, UpdateStatusTagRequest request) {
        return api.updateStatusTag(userId, request);
    }

    public Call<BaseResponse<UserDto>> updatePersonalInfo(String userId, UpdatePersonalInfoRequest request) {
        return api.updatePersonalInfo(userId, request);
    }

    public Call<BaseResponse<UserDto>> updateAcademicInfo(String userId, UpdateAcademicInfoRequest request) {
        return api.updateAcademicInfo(userId, request);
    }

    public Call<BaseResponse<UserDto>> updateProfileLocation(String userId, UpdateProfileLocationRequest request) {
        return api.updateProfileLocation(userId, request);
    }

    public Call<BaseResponse<UserDto>> uploadAvatar(String userId, MultipartBody.Part file) {
        return api.uploadAvatar(userId, file);
    }

    public Call<BaseResponse<UserDto>> toggleBuddyStatus(String userId, ToggleBuddyStatusRequest request) {
        return api.toggleBuddyStatus(userId, request);
    }

    public Call<BaseResponse<CommonResponse>> changePassword(String userId, ChangePasswordRequest request) {
        return api.changePassword(userId, request);
    }

    public Call<BaseResponse<List<SubjectDto>>> getSubjects(String userId, String type) {
        return api.getSubjects(userId, type);
    }

    public Call<BaseResponse<SubjectDto>> addSubject(String userId, AddSubjectRequest request) {
        return api.addSubject(userId, request);
    }

    public Call<BaseResponse<CommonResponse>> deleteSubject(String userId, String subjectId) {
        return api.deleteSubject(userId, subjectId);
    }

    public Call<BaseResponse<CommonResponse>> deleteAccount(String userId) {
        return api.deleteAccount(userId);
    }
}
