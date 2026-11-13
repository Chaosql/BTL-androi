package com.example.myapplication.ui.profile;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplication.R;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.request.ToggleBuddyStatusRequest;
import com.example.myapplication.data.remote.network.ApiError;
import com.example.myapplication.data.repository.profile.ProfileRepository;
import com.example.myapplication.ui.base.BaseViewModel;
import com.example.myapplication.ui.base.UiState;

public final class AccountSettingsViewModel extends BaseViewModel {
    private final ProfileRepository repository;
    private final MutableLiveData<UiState<Boolean>> radar = new MutableLiveData<>();
    private Boolean enabled;

    public AccountSettingsViewModel(Application application) {
        super(application);
        repository = new ProfileRepository(application);
    }

    public LiveData<UiState<Boolean>> radar() { return radar; }
    public Boolean radarEnabled() { return enabled; }

    public void loadRadar() {
        if (radar.getValue() != null) return;
        radar.setValue(UiState.loading());
        watch(execute(repository.getCurrentUser()), response -> {
            if (response.data == null || response.data.buddyActive == null) {
                invalid(); return;
            }
            enabled = response.data.buddyActive;
            radar.setValue(UiState.success(enabled));
        }, error -> radar.setValue(UiState.error(error)));
    }

    public void setRadarEnabled(boolean value) {
        if (enabled == null || enabled == value || radar.getValue().status == UiState.Status.LOADING) return;
        radar.setValue(UiState.loading());
        String id = new PreferenceManager(getApplication()).getUserId();
        watch(execute(repository.toggleBuddyStatus(id, new ToggleBuddyStatusRequest(value))), response -> {
            if (response.data == null || !Boolean.valueOf(value).equals(response.data.buddyActive)) {
                invalid(); return;
            }
            enabled = value;
            radar.setValue(UiState.success(enabled));
        }, error -> radar.setValue(UiState.error(error)));
    }

    private void invalid() {
        radar.setValue(UiState.error(new ApiError(422, getApplication().getString(R.string.feature_invalid))));
    }
}
