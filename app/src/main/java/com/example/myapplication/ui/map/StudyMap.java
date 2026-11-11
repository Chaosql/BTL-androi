package com.example.myapplication.ui.map;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import com.example.myapplication.BuildConfig;
import com.example.myapplication.R;
import java.util.function.Consumer;
import java.util.*;
import com.example.myapplication.data.remote.dto.response.NearbyUserResponse;
import com.example.myapplication.ui.friends.SocialUi;
import org.maplibre.android.annotations.*;
import org.maplibre.android.MapLibre;
import org.maplibre.android.maps.*;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.camera.CameraUpdateFactory;

final class StudyMap {
    private final FrameLayout host;
    private final Consumer<String> status;
    private MapView view;
    private MapLibreMap map;
    private final RadarRenderer radarRenderer = new RadarRenderer();
    private boolean scanning;
    private boolean started, resumed, restored;
    private int generation, styleVersion;
    private boolean ready, located, fallback;
    private final android.os.Handler loader = new android.os.Handler(android.os.Looper.getMainLooper());
    private double radarLat, radarLng, radius;
    private List<NearbyUserResponse> users = Collections.emptyList();
    private final Map<Marker, String> markers = new HashMap<>();
    private Consumer<String> userClick = id -> { };
    StudyMap(FrameLayout host, Consumer<String> status) {
        this.host = host; this.status = status;
    }
    void create(Bundle state) {
        if (view != null) { loadStyle(); return; }
        int version = ++generation;
        status.accept(host.getContext().getString(R.string.map_loading));
        try {
            MapLibre.getInstance(host.getContext());
            MapHttp.install(host.getContext());
            MapView next = com.example.myapplication.databinding.ViewStudyMapBinding
                    .inflate(android.view.LayoutInflater.from(host.getContext()), host, false).getRoot();
            view = next;
            host.addView(next);
            restored = state != null && !state.isEmpty();
            next.onCreate(state);
            next.addOnDidFailLoadingMapListener(error -> {
                if (version != generation) return;
                if (BuildConfig.DEBUG) android.util.Log.w("StudyMap", "Map loading failed: " + error);
                if (!fallback && !ready) loadStyle(true);
                else status.accept(host.getContext().getString(R.string.map_unavailable));
            });
            next.getMapAsync(result -> {
                if (version != generation) return;
                map = result;
                map.setOnMarkerClickListener(marker -> {
                    String id = markers.get(marker);
                    if (id == null) return false;
                    userClick.accept(id); return true;
                });
                int margin = (int) (host.getResources().getDisplayMetrics().density * 100);
                map.getUiSettings().setAttributionGravity(Gravity.BOTTOM | Gravity.START);
                map.getUiSettings().setAttributionMargins(12, 0, 0, margin);
                map.getUiSettings().setLogoMargins(12, 0, 0, margin + 28);
                if (!restored) map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(21.0285, 105.8542), 12));
                loadStyle();
            });
            if (started) next.onStart();
            if (resumed) next.onResume();
        } catch (RuntimeException | LinkageError error) {
            destroyView();
            status.accept(host.getContext().getString(R.string.map_unavailable));
        }
    }
    private void loadStyle() { loadStyle(false); }
    private void loadStyle(boolean useFallback) {
        if (map == null) return;
        ready = false; fallback = useFallback;
        int version = generation, styleRequest = ++styleVersion;
        loader.removeCallbacksAndMessages(null);
        status.accept(host.getContext().getString(R.string.map_loading));
        map.setStyle(new Style.Builder().fromUri(useFallback ? "asset://map_fallback.json" : BuildConfig.MAP_STYLE_URL), style -> {
            if (version != generation || styleRequest != styleVersion) return;
            loader.removeCallbacksAndMessages(null);
            radarRenderer.setup(style);
            ready = true; status.accept(""); renderRadar();
        });
        loader.postDelayed(() -> {
            if (version != generation || styleRequest != styleVersion || ready) return;
            if (!useFallback) loadStyle(true);
            else status.accept(host.getContext().getString(R.string.map_unavailable));
        }, 15000);
    }
    void onUserClick(Consumer<String> click) { userClick = click; }
    void startRadar(double lat, double lng, double radius) {
        radarLat = lat; radarLng = lng; this.radius = radius;
        located = true; scanning = true; users = Collections.emptyList(); renderRadar();
    }
    void stopRadar() { scanning = false; radarRenderer.stop(); }
    void showRadar(double lat, double lng, double radius, List<NearbyUserResponse> users) {
        radarLat = lat; radarLng = lng; this.radius = radius; this.users = new ArrayList<>(users);
        stopRadar(); located = true; renderRadar();
    }
    void clearRadar() { stopRadar(); located = false; users = Collections.emptyList(); renderRadar(); }
    private void renderRadar() {
        if (map == null || !ready) return;
        map.clear(); markers.clear();
        if (!located) return;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(radarLat, radarLng), 14 - Math.log(radius) / Math.log(2)));
        map.addMarker(new MarkerOptions().position(new LatLng(radarLat, radarLng)).title(host.getContext().getString(R.string.radar_me)));
        if (scanning) radarRenderer.start(radarLat, radarLng, radius);
        for (NearbyUserResponse user : users) {
            if (!RadarData.coordinates(user.latitude, user.longitude)) continue;
            Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(user.latitude, user.longitude))
                    .title(SocialUi.name(user.firstName, user.lastName)));
            markers.put(marker, user.userId);
        }
    }
    void start() { started = true; if (view != null) view.onStart(); if (map != null && !ready) loadStyle(fallback); }
    void resume() { resumed = true; if (view != null) view.onResume(); if (scanning && ready) radarRenderer.start(radarLat, radarLng, radius); }
    void pause() { radarRenderer.stop(); resumed = false; if (view != null) view.onPause(); }
    void stop() { radarRenderer.stop(); started = false; loader.removeCallbacksAndMessages(null); if (view != null) view.onStop(); }
    void save(Bundle state) { if (view != null) view.onSaveInstanceState(state); }
    void lowMemory() { if (view != null) view.onLowMemory(); }
    private void destroyView() {
        radarRenderer.destroy(); scanning = false;
        loader.removeCallbacksAndMessages(null);
        ++generation; map = null; ready = false; markers.clear();
        if (view != null) {
            try { if (resumed) view.onPause(); if (started) view.onStop(); view.onDestroy(); }
            catch (RuntimeException | LinkageError ignored) { }
            host.removeView(view); view = null;
        }
    }
    void destroy() { destroyView(); }
}
