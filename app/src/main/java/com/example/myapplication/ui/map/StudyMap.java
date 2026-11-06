package com.example.myapplication.ui.map;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import com.example.myapplication.BuildConfig;
import com.example.myapplication.R;
import java.util.function.Consumer;
import org.maplibre.android.MapLibre;
import org.maplibre.android.maps.*;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.camera.CameraUpdateFactory;

final class StudyMap {
    private final FrameLayout host;
    private final Consumer<String> status;
    private MapView view;
    private MapLibreMap map;
    private boolean started, resumed, restored;
    private int generation, styleVersion;
    StudyMap(FrameLayout host, Consumer<String> status) {
        this.host = host; this.status = status;
    }
    void create(Bundle state) {
        if (view != null) { loadStyle(); return; }
        int version = ++generation;
        status.accept(host.getContext().getString(R.string.map_loading));
        try {
            MapLibre.getInstance(host.getContext());
            MapView next = new MapView(host.getContext());
            view = next;
            host.addView(next, new FrameLayout.LayoutParams(-1, -1));
            restored = state != null && !state.isEmpty();
            next.onCreate(state);
            next.addOnDidFailLoadingMapListener(error -> {
                if (version == generation) status.accept(host.getContext().getString(R.string.map_unavailable));
            });
            next.getMapAsync(result -> {
                if (version != generation) return;
                map = result;
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
    private void loadStyle() {
        if (map == null) return;
        int version = generation, styleRequest = ++styleVersion;
        status.accept(host.getContext().getString(R.string.map_loading));
        map.setStyle(new Style.Builder().fromUri(BuildConfig.MAP_STYLE_URL), style -> {
            if (version == generation && styleRequest == styleVersion) status.accept("");
        });
    }
    void start() { started = true; if (view != null) view.onStart(); }
    void resume() { resumed = true; if (view != null) view.onResume(); }
    void pause() { resumed = false; if (view != null) view.onPause(); }
    void stop() { started = false; if (view != null) view.onStop(); }
    void save(Bundle state) { if (view != null) view.onSaveInstanceState(state); }
    void lowMemory() { if (view != null) view.onLowMemory(); }
    private void destroyView() {
        ++generation; map = null;
        if (view != null) {
            try { if (resumed) view.onPause(); if (started) view.onStop(); view.onDestroy(); }
            catch (RuntimeException | LinkageError ignored) { }
            host.removeView(view); view = null;
        }
    }
    void destroy() { destroyView(); }
}
