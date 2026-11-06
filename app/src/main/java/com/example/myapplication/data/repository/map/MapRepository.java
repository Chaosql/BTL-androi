package com.example.myapplication.data.repository.map;

import android.content.Context;
import com.example.myapplication.data.repository.BaseRepository;
import com.example.myapplication.data.remote.api.ApiService;
import com.example.myapplication.data.remote.dto.request.*;
import com.example.myapplication.data.remote.dto.response.*;
import java.util.List;
import okhttp3.MultipartBody;
import retrofit2.Call;

public final class MapRepository extends BaseRepository {
    public MapRepository(Context context) { super(context); }
    public MapRepository(ApiService api) { super(api); }

    public Call<BaseResponse<CommonResponse>> updateLocation(UpdateLocationRequest request) {
        return api.updateLocation(request);
    }

    public Call<BaseResponse<CommonResponse>> removeLocation() {
        return api.removeLocation();
    }

    public Call<BaseResponse<List<NearbyUserResponse>>> getNearbyUsers(Double lat, Double lng, Double radius) {
        return api.getNearbyUsers(lat, lng, radius);
    }
}
