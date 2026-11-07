package com.example.myapplication.ui.profile;

import android.app.Application;
import com.example.myapplication.ui.base.BaseViewModel;
import com.example.myapplication.ui.base.UiOperation;
import com.example.myapplication.data.repository.profile.ProfileRepository;
import com.example.myapplication.data.remote.dto.request.*;
import com.example.myapplication.data.remote.dto.response.*;
import java.util.List;
import okhttp3.MultipartBody;

public final class ProfileViewModel extends BaseViewModel {
    private final ProfileRepository repository;

    public ProfileViewModel(Application application) {
        super(application);
        repository = new ProfileRepository(application);
    }

    public UiOperation<BaseResponse<UserDto>> getCurrentUser() {
        return execute(repository.getCurrentUser());
    }

    public UiOperation<BaseResponse<UserDto>> getProfile(String userId) {
        return execute(repository.getProfile(userId));
    }

    public UiOperation<BaseResponse<UserDto>> updateStatusTag(String userId, UpdateStatusTagRequest request) {
        return execute(repository.updateStatusTag(userId, request));
    }

    public UiOperation<BaseResponse<UserDto>> updatePersonalInfo(String userId, UpdatePersonalInfoRequest request) {
        return execute(repository.updatePersonalInfo(userId, request));
    }

    public UiOperation<BaseResponse<UserDto>> updateAcademicInfo(String userId, UpdateAcademicInfoRequest request) {
        return execute(repository.updateAcademicInfo(userId, request));
    }

    public UiOperation<BaseResponse<UserDto>> updateProfileLocation(String userId, UpdateProfileLocationRequest request) {
        return execute(repository.updateProfileLocation(userId, request));
    }

    public UiOperation<BaseResponse<UserDto>> uploadAvatar(String userId, MultipartBody.Part file) {
        return execute(repository.uploadAvatar(userId, file));
    }

    public UiOperation<BaseResponse<UserDto>> toggleBuddyStatus(String userId, ToggleBuddyStatusRequest request) {
        return execute(repository.toggleBuddyStatus(userId, request), response -> {
            if (response.data == null || !java.util.Objects.equals(request.buddyActive, response.data.buddyActive))
                throw new IllegalArgumentException(getApplication().getString(com.example.myapplication.R.string.feature_invalid));
            return response;
        });
    }

    public UiOperation<BaseResponse<CommonResponse>> changePassword(String userId, ChangePasswordRequest request) {
        return execute(repository.changePassword(userId, request));
    }

    public UiOperation<BaseResponse<List<SubjectDto>>> getSubjects(String userId, String type) {
        return execute(repository.getSubjects(userId, type), response -> {
            if (response.data != null) {
                subjectNames.clear();
                for (SubjectDto subject : response.data) if (subject != null)
                    subjectNames.add(ProfileUi.value(subject.name).trim().toLowerCase(java.util.Locale.ROOT));
            }
            return response;
        });
    }

    public UiOperation<BaseResponse<SubjectDto>> addSubject(String userId, AddSubjectRequest request) {
        return execute(repository.addSubject(userId, request));
    }

    public UiOperation<BaseResponse<CommonResponse>> deleteSubject(String userId, String subjectId) {
        return execute(repository.deleteSubject(userId, subjectId));
    }

    public UiOperation<BaseResponse<CommonResponse>> deleteAccount(String userId) {
        return execute(repository.deleteAccount(userId));
    }
    public static final class Snapshot {
        public final UserDto user;
        public final Integer subjects, bookings;
        Snapshot(UserDto user, Integer subjects, Integer bookings) { this.user = user; this.subjects = subjects; this.bookings = bookings; }
    }
    private final androidx.lifecycle.MutableLiveData<com.example.myapplication.ui.base.UiState<Snapshot>> summary = new androidx.lifecycle.MutableLiveData<>();
    private Snapshot snapshot;
    private boolean summaryLoading, reloadPending;
    private int summaryVersion;
    public androidx.lifecycle.LiveData<com.example.myapplication.ui.base.UiState<Snapshot>> summary() { return summary; }
    public void loadSummary(boolean stats, boolean refresh) {
        if (!new com.example.myapplication.data.local.PreferenceManager(getApplication()).isLogin()) return;
        if (summaryLoading) { if (refresh) reloadPending = true; return; }
        if (!refresh && snapshot != null) { summary.setValue(com.example.myapplication.ui.base.UiState.success(snapshot)); return; }
        summaryLoading = true; int version = ++summaryVersion;
        summary.setValue(com.example.myapplication.ui.base.UiState.loading());
        watch(getCurrentUser(), response -> {
            summaryLoading = false;
            if (reloadPending) { reloadPending = false; loadSummary(stats, true); return; }
            if (response.data == null) { summary.setValue(com.example.myapplication.ui.base.UiState.error(
                    new com.example.myapplication.data.remote.network.ApiError(422, getApplication().getString(com.example.myapplication.R.string.profile_empty_response)))); return; }
            snapshot = new Snapshot(response.data, response.data.subjects == null ? null : response.data.subjects.size(), null);
            summary.setValue(com.example.myapplication.ui.base.UiState.success(snapshot));
            if (!stats) return;
            String id = new com.example.myapplication.data.local.PreferenceManager(getApplication()).getUserId();
            watch(getSubjects(id, null), subjects -> {
                if (version != summaryVersion) return;
                snapshot = new Snapshot(snapshot.user, subjects.data == null ? null : subjects.data.size(), snapshot.bookings);
                summary.setValue(com.example.myapplication.ui.base.UiState.success(snapshot));
            }, error -> { });
            watch(execute(new com.example.myapplication.data.repository.schedule.BookingRepository((android.content.Context) getApplication()).getMyBookings()), bookings -> {
                if (version != summaryVersion) return;
                snapshot = new Snapshot(snapshot.user, snapshot.subjects, bookings.data == null ? null : bookings.data.size());
                summary.setValue(com.example.myapplication.ui.base.UiState.success(snapshot));
            }, error -> { });
        }, error -> {
            summaryLoading = false;
            if (reloadPending) { reloadPending = false; loadSummary(stats, true); return; }
            summary.setValue(com.example.myapplication.ui.base.UiState.error(error));
        });
    }
    private final androidx.lifecycle.MutableLiveData<com.example.myapplication.ui.base.UiState<UserDto>> avatarState = new androidx.lifecycle.MutableLiveData<>();
    private final java.util.concurrent.ExecutorService imageWorker = java.util.concurrent.Executors.newSingleThreadExecutor();
    public androidx.lifecycle.LiveData<com.example.myapplication.ui.base.UiState<UserDto>> avatarState() { return avatarState; }
    public void acknowledgeAvatar() { avatarState.setValue(null); }
    public void prepareAvatar(android.net.Uri uri) {
        if (avatarState.getValue() != null && avatarState.getValue().status == com.example.myapplication.ui.base.UiState.Status.LOADING) return;
        avatarState.setValue(com.example.myapplication.ui.base.UiState.loading());
        String owner = new com.example.myapplication.data.local.PreferenceManager(getApplication()).getUserId();
        imageWorker.execute(() -> {
            try {
                MultipartBody.Part part = AvatarImage.read(getApplication().getContentResolver(), uri);
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    if (imageWorker.isShutdown()) return;
                    if (!owner.equals(new com.example.myapplication.data.local.PreferenceManager(getApplication()).getUserId())) {
                        avatarState.setValue(null); return;
                    }
                    watch(uploadAvatar(owner, part), response -> avatarState.setValue(com.example.myapplication.ui.base.UiState.success(response.data)),
                            error -> avatarState.setValue(com.example.myapplication.ui.base.UiState.error(error)));
                });
            } catch (Exception error) {
                avatarState.postValue(com.example.myapplication.ui.base.UiState.error(new com.example.myapplication.data.remote.network.ApiError(0,
                        getApplication().getString(com.example.myapplication.R.string.profile_avatar_error))));
            }
        });
    }
    @Override protected void onCleared() { imageWorker.shutdownNow(); super.onCleared(); }
    private final androidx.lifecycle.MutableLiveData<com.example.myapplication.ui.base.UiState<Boolean>> personalSave = new androidx.lifecycle.MutableLiveData<>();
    public androidx.lifecycle.LiveData<com.example.myapplication.ui.base.UiState<Boolean>> personalSave() { return personalSave; }
    public void savePersonal(UserDto user, UpdatePersonalInfoRequest personal, UpdateAcademicInfoRequest academic, String location) {
        if (personalSave.getValue() != null && personalSave.getValue().status == com.example.myapplication.ui.base.UiState.Status.LOADING) return;
        String id = new com.example.myapplication.data.local.PreferenceManager(getApplication()).getUserId();
        java.util.List<java.util.function.Supplier<UiOperation<BaseResponse<UserDto>>>> changes = new java.util.ArrayList<>();
        if (!java.util.Objects.equals(personal.firstName, ProfileUi.value(user.firstName))
                || !java.util.Objects.equals(personal.lastName, ProfileUi.value(user.lastName))
                || !java.util.Objects.equals(personal.gender, user.gender)
                || !java.util.Objects.equals(ProfileUi.date(personal.dateOfBirth), ProfileUi.date(user.dateOfBirth))
                || !java.util.Objects.equals(personal.phone, ProfileUi.value(user.phone)))
            changes.add(() -> updatePersonalInfo(id, personal));
        if (!java.util.Objects.equals(academic.university, ProfileUi.value(user.university)) || !java.util.Objects.equals(academic.majorName, ProfileUi.value(user.majorName)))
            changes.add(() -> updateAcademicInfo(id, academic));
        if (!java.util.Objects.equals(location, ProfileUi.value(user.location))) changes.add(() -> updateProfileLocation(id, new UpdateProfileLocationRequest(location)));
        personalSave.setValue(com.example.myapplication.ui.base.UiState.loading()); saveNext(changes, 0);
    }
    private void saveNext(java.util.List<java.util.function.Supplier<UiOperation<BaseResponse<UserDto>>>> changes, int index) {
        if (index == changes.size()) { personalSave.setValue(com.example.myapplication.ui.base.UiState.success(true)); return; }
        watch(changes.get(index).get(), result -> saveNext(changes, index + 1), error -> {
            String message = index == 0 ? error.message : getApplication().getString(com.example.myapplication.R.string.profile_partial_save) + "\n" + error.message;
            personalSave.setValue(com.example.myapplication.ui.base.UiState.error(new com.example.myapplication.data.remote.network.ApiError(error.code, message)));
        });
    }
    private final java.util.Set<String> subjectNames = new java.util.HashSet<>();
    public boolean hasSubject(String name) { return subjectNames.contains(name.trim().toLowerCase(java.util.Locale.ROOT)); }
    public static boolean validPhone(String phone) { return phone.isEmpty() || phone.matches("[+0-9() .-]{6,20}"); }
    public static String validBirthDate(String raw) {
        java.time.LocalDate date = ProfileUi.date(raw);
        return date != null && date.isBefore(java.time.LocalDate.now()) ? date.toString() : null;
    }
}
