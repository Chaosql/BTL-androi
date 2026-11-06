package com.example.myapplication.ui.map;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.*;
import android.os.*;
import androidx.core.content.ContextCompat;
import com.example.myapplication.R;
import java.util.*;
import java.util.function.Consumer;

final class DeviceLocation {
    private final Context context;
    private final LocationManager manager;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private LocationListener listener;
    private Runnable timeout;
    DeviceLocation(Context context) {
        this.context = context.getApplicationContext(); manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }
    boolean permitted() { return coarse() || fine(); }
    private boolean coarse() { return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED; }
    private boolean fine() { return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED; }
    @android.annotation.SuppressLint("MissingPermission")
    void get(Consumer<Location> success, java.util.function.IntConsumer failure) {
        cancel();
        if (!permitted()) { failure.accept(R.string.radar_permission_denied); return; }
        if (manager == null) { failure.accept(R.string.radar_gps_disabled); return; }
        List<String> providers = new ArrayList<>();
        try {
            if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) providers.add(LocationManager.NETWORK_PROVIDER);
            if (fine() && manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) providers.add(LocationManager.GPS_PROVIDER);
            if (manager.getAllProviders().contains("fused") && manager.isProviderEnabled("fused")) providers.add("fused");
            if (providers.isEmpty()) { failure.accept(fine() ? R.string.radar_gps_disabled : R.string.radar_approximate_unavailable); return; }
            Location best = null;
            for (String provider : providers) {
                Location cached = manager.getLastKnownLocation(provider);
                if (fresh(cached) && (best == null || cached.getElapsedRealtimeNanos() > best.getElapsedRealtimeNanos())) best = cached;
            }
            if (best != null) { success.accept(best); return; }
            listener = new LocationListener() {
                @Override public void onLocationChanged(Location point) {
                    if (listener != this || !fresh(point)) return;
                    cancel(); success.accept(point);
                }
                @Override public void onProviderEnabled(String provider) { }
                @Override public void onProviderDisabled(String provider) { }
                @Override public void onStatusChanged(String provider, int status, Bundle extras) { }
            };
            int registered = 0;
            for (String provider : providers) {
                try { manager.requestLocationUpdates(provider, 1000L, 0, listener, Looper.getMainLooper()); registered++; }
                catch (IllegalArgumentException | SecurityException ignored) { }
            }
            if (registered == 0) { cancel(); failure.accept(R.string.radar_location_timeout); return; }
            timeout = () -> { cancel(); failure.accept(R.string.radar_location_timeout); }; handler.postDelayed(timeout, 25000);
        } catch (SecurityException error) { cancel(); failure.accept(R.string.radar_permission_denied); }
    }
    static boolean fresh(Location point) {
        if (point == null || !RadarData.coordinates(point.getLatitude(), point.getLongitude())) return false;
        long age = SystemClock.elapsedRealtimeNanos() - point.getElapsedRealtimeNanos();
        return point.getElapsedRealtimeNanos() > 0 && age >= 0 && age <= 120_000_000_000L;
    }
    void cancel() {
        if (timeout != null) handler.removeCallbacks(timeout); timeout = null;
        if (listener != null && manager != null) {
            try { manager.removeUpdates(listener); } catch (SecurityException ignored) { }
        }
        listener = null;
    }
}
