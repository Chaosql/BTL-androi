package com.example.myapplication.ui.map;

import android.app.Application;
import com.example.myapplication.ui.base.BaseViewModel;
import com.example.myapplication.ui.base.UiOperation;
import com.example.myapplication.data.repository.map.MapRepository;
import com.example.myapplication.data.remote.dto.request.*;
import com.example.myapplication.data.remote.dto.response.*;
import java.util.List;
import okhttp3.MultipartBody;

public final class MapViewModel extends BaseViewModel {
    private final MapRepository repository;

    public MapViewModel(Application application) {
        super(application);
        repository = new MapRepository(application);
    }

    public UiOperation<BaseResponse<CommonResponse>> updateLocation(UpdateLocationRequest request) {
        return execute(repository.updateLocation(request));
    }

    public UiOperation<BaseResponse<CommonResponse>> removeLocation() {
        return execute(repository.removeLocation());
    }

    public UiOperation<BaseResponse<List<NearbyUserResponse>>> getNearbyUsers(Double lat, Double lng, Double radius) {
        return execute(repository.getNearbyUsers(lat, lng, radius));
    }
    public static final class RadarResult {
        public final double latitude, longitude, radius;
        public final List<NearbyUserResponse> users;
        RadarResult(double lat, double lng, double radius, List<NearbyUserResponse> users) {
            latitude = lat; longitude = lng; this.radius = radius;
            this.users = java.util.Collections.unmodifiableList(new java.util.ArrayList<>(users));
        }
    }
    private final androidx.lifecycle.MutableLiveData<com.example.myapplication.ui.base.UiState<RadarResult>> radar = new androidx.lifecycle.MutableLiveData<>();
    private UiOperation<?> activeRadar;
    private int radarVersion;
    private final android.os.Handler scanTimer = new android.os.Handler(android.os.Looper.getMainLooper());
    public androidx.lifecycle.LiveData<com.example.myapplication.ui.base.UiState<RadarResult>> radar() { return radar; }
    public void cancelRadar() {
        scanTimer.removeCallbacksAndMessages(null);
        radarVersion++; if (activeRadar != null) activeRadar.cancel(); activeRadar = null; radar.setValue(null);
    }
    public void scan(double lat, double lng, double radius) {
        cancelRadar();
        int current = radarVersion;
        long started = android.os.SystemClock.elapsedRealtime();
        String owner = new com.example.myapplication.data.local.PreferenceManager(getApplication()).getUserId();
        radar.setValue(com.example.myapplication.ui.base.UiState.loading());
        UiOperation<BaseResponse<CommonResponse>> update = updateLocation(new UpdateLocationRequest(lat, lng)); activeRadar = update;
        watch(update, response -> {
            if (!radarCurrent(current, owner)) return;
            UiOperation<BaseResponse<List<NearbyUserResponse>>> query = getNearbyUsers(lat, lng, radius); activeRadar = query;
            watch(query, data -> {
                if (!radarCurrent(current, owner)) return;
                if (data.data == null) {
                    completeScan(current, owner, started, com.example.myapplication.ui.base.UiState.error(
                            new com.example.myapplication.data.remote.network.ApiError(422, null))); return;
                }
                completeScan(current, owner, started, com.example.myapplication.ui.base.UiState.success(
                        new RadarResult(lat, lng, radius, RadarData.filter(data.data, owner, lat, lng, radius))));
            }, error -> { if (radarCurrent(current, owner)) completeScan(current, owner, started, com.example.myapplication.ui.base.UiState.error(error)); });
        }, error -> { if (radarCurrent(current, owner)) completeScan(current, owner, started, com.example.myapplication.ui.base.UiState.error(error)); });
    }
    private void completeScan(int version, String owner, long started,
            com.example.myapplication.ui.base.UiState<RadarResult> result) {
        activeRadar = null;
        long remaining = Math.max(0, 3000 - (android.os.SystemClock.elapsedRealtime() - started));
        Runnable publish = () -> { if (radarCurrent(version, owner)) radar.setValue(result); };
        if (remaining == 0) publish.run();
        else scanTimer.postDelayed(publish, remaining);
    }
    @Override protected void onCleared() {
        cancelRadar();
        super.onCleared();
    }
    private boolean radarCurrent(int version, String owner) {
        if (version != radarVersion) return false;
        if (owner.equals(new com.example.myapplication.data.local.PreferenceManager(getApplication()).getUserId())) return true;
        cancelRadar(); return false;
    }
}
