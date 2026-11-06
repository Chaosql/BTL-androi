package com.example.myapplication.ui.map;

import com.example.myapplication.databinding.FragmentMapBinding;
import android.os.Bundle;
import android.view.View;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.main.UiScreen;
import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.request.UpdateLocationRequest;
import com.example.myapplication.data.remote.dto.response.*;
import com.example.myapplication.data.repository.map.MapRepository;
import com.example.myapplication.data.repository.profile.ProfileRepository;
import com.example.myapplication.ui.friends.SocialUi;
import com.example.myapplication.ui.friends.UserInfoDialog;
import com.example.myapplication.ui.main.MainActivity;
import java.util.*;
import retrofit2.Call;

public class MapFragment extends BaseFragment<FragmentMapBinding> {
    private StudyMap studyMap;
    private DeviceLocation location;
    private MapRepository repository;
    private Call<?> active;
    private boolean busy, awaitingPermission;
    private int version, link;
    private double radius = 3;
    private String owner;
    private List<NearbyUserResponse> users = Collections.emptyList();
    private androidx.appcompat.app.AlertDialog results;
    private final ActivityResultLauncher<String[]> permissions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), grants -> {
                if (binding == null || !awaitingPermission) return;
                awaitingPermission = false;
                if (location.permitted()) locate();
                else { busy(false); status(R.string.radar_permission_denied, 3); }
            });
    public MapFragment() { super(FragmentMapBinding::inflate); }

    @Override public void onResume() {
        super.onResume();
        if (studyMap != null) studyMap.resume();
        if (binding == null || !new com.example.myapplication.data.local.PreferenceManager(requireContext()).isLogin()) return;
        request(new com.example.myapplication.data.repository.notification.NotificationRepository(requireContext())
                .getUnreadNotificationCount(), response -> {
            int count = response.data == null ? 0 : Math.max(0, response.data);
            binding.tvNotificationBadge.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
            binding.tvNotificationBadge.setText(count > 99 ? "99+" : String.valueOf(count));
            binding.btnNotification.setContentDescription(count > 0 ? getString(R.string.notification_title_count, count)
                    : getString(R.string.title_notification));
        }, error -> { });
    }

    @Override protected void onScreenCreated(View root, Bundle state) {
        repository = new MapRepository(requireContext()); location = new DeviceLocation(requireContext());
        owner = new PreferenceManager(requireContext()).getUserId();
        awaitingPermission = state != null && state.getBoolean("radar_permission");
        radius = state == null ? 3 : state.getDouble("radar_radius", 3);
        users = Collections.emptyList(); link = 0;
        studyMap = new StudyMap(binding.mapView, message -> {
            if (binding == null) return;
            binding.tvMapStatus.setText(message);
            binding.tvMapStatus.setVisibility(message.isEmpty() ? View.GONE : View.VISIBLE);
        });
        studyMap.create(state == null ? null : state.getBundle("map_state"));
        studyMap.onUserClick(this::openUser);
        binding.tvMapStatus.setOnClickListener(view -> studyMap.create(null));
        binding.btnProfile.setOnClickListener(bindingView -> go(UiScreen.PROFILE));
        binding.imgAvatar.setOnClickListener(bindingView -> go(UiScreen.PROFILE));
        binding.btnFriend.setOnClickListener(bindingView -> go(UiScreen.CONVERSATIONS));
        binding.btnCalendar.setOnClickListener(bindingView -> go(UiScreen.SCHEDULE));
        binding.btnNotification.setOnClickListener(bindingView -> go(UiScreen.NOTIFICATIONS));
        binding.btnRadar.setContentDescription(getString(R.string.map_description));
        binding.imgGroupRadar.setContentDescription(getString(R.string.map_description));
        binding.btnRadar.setOnClickListener(view -> scan());
        binding.imgGroupRadar.setOnClickListener(view -> scan());
        binding.layoutRadar.setOnClickListener(view -> scan());
        binding.tvRadarStatus.setOnClickListener(view -> statusAction());
        binding.etDistance.setOnEditorActionListener((view, action, event) -> {
            if (action != android.view.inputmethod.EditorInfo.IME_ACTION_DONE) return false;
            scan(); return true;
        });
        busy(awaitingPermission);
        binding.imgSearch.setOnClickListener(bindingView -> go(UiScreen.SEARCH_USER));
    }
    private void busy(boolean value) {
        busy = value;
        binding.btnRadar.setEnabled(!value); binding.imgGroupRadar.setEnabled(!value);
        binding.layoutRadar.setEnabled(!value); binding.etDistance.setEnabled(!value);
        binding.btnRadar.setImageResource(value ? R.drawable.radaring : R.drawable.radar);
    }
    private void status(int text, int action) { binding.tvRadarStatus.setText(text); link = action; }
    private void scan() {
        if (busy || awaitingPermission) return;
        if (!new PreferenceManager(requireContext()).isLogin()) { go(UiScreen.LOGIN); return; }
        try { radius = RadarData.radius(binding.etDistance.getText().toString()); }
        catch (IllegalArgumentException error) { binding.etDistance.setError(getString(R.string.radar_invalid_radius)); return; }
        binding.etDistance.setError(null);
        android.view.inputmethod.InputMethodManager keyboard = (android.view.inputmethod.InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (keyboard != null) keyboard.hideSoftInputFromWindow(binding.etDistance.getWindowToken(), 0);
        binding.etDistance.clearFocus();
        owner = new PreferenceManager(requireContext()).getUserId();
        busy(true); users = Collections.emptyList(); studyMap.clearRadar(); status(R.string.radar_checking, 0);
        int current = ++version;
        Call<BaseResponse<UserDto>> call = new ProfileRepository(requireContext()).getCurrentUser(); active = call;
        request(call, response -> {
            if (!current(current)) return;
            active = null;
            if (response.data == null || !owner.equals(response.data.id)) { failed(getString(R.string.feature_invalid)); return; }
            SocialUi.avatar(binding.imgAvatar, response.data.avatar);
            if (!Boolean.TRUE.equals(response.data.buddyActive)) { busy(false); status(R.string.radar_buddy_off, 1); return; }
            if (!location.permitted()) {
                awaitingPermission = true;
                permissions.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
            } else locate();
        }, error -> { if (current(current)) failed(error.message); });
    }
    private boolean current(int expected) {
        if (binding == null || version != expected) return false;
        if (owner.equals(new PreferenceManager(requireContext()).getUserId())) return true;
        ++version; active = null; users = Collections.emptyList();
        busy(false); studyMap.clearRadar(); status(R.string.radar_hint, 0); return false;
    }
    private void locate() {
        if (!owner.equals(new PreferenceManager(requireContext()).getUserId())) { busy(false); return; }
        busy(true); status(R.string.radar_locating, 0);
        int current = ++version;
        location.get(point -> { if (current(current)) scan(point, current); }, message -> {
            if (!current(current)) return;
            busy(false); status(message, message == R.string.radar_gps_disabled ? 2
                    : message == R.string.radar_permission_denied || message == R.string.radar_approximate_unavailable ? 3 : 0);
        });
    }
    private void scan(Location point, int current) {
        status(R.string.radar_scanning, 0);
        double lat = point.getLatitude(), lng = point.getLongitude();
        Call<BaseResponse<CommonResponse>> update = repository.updateLocation(new UpdateLocationRequest(lat, lng)); active = update;
        request(update, response -> {
            if (!current(current)) return;
            Call<BaseResponse<List<NearbyUserResponse>>> query = repository.getNearbyUsers(lat, lng, radius); active = query;
            request(query, data -> {
                if (!current(current)) return;
                active = null; busy(false);
                if (data.data == null) { failed(getString(R.string.feature_invalid)); return; }
                users = RadarData.filter(data.data, owner, lat, lng, radius);
                studyMap.showRadar(lat, lng, radius, users);
                binding.tvRadarStatus.setText(users.isEmpty() ? getString(R.string.radar_empty, radius)
                        : getString(R.string.radar_results, users.size(), radius)); link = users.isEmpty() ? 0 : 4;
            }, error -> { if (current(current)) failed(error.message); });
        }, error -> { if (current(current)) failed(error.message); });
    }
    private void failed(String message) {
        active = null; busy(false); link = 0;
        binding.tvRadarStatus.setText(getString(R.string.radar_retry, message));
    }
    private void statusAction() {
        if (busy) return;
        if (link == 1) { go(UiScreen.PROFILE); return; }
        if (link == 2 || link == 3) {
            Intent intent = link == 2 ? new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    : new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + requireContext().getPackageName()));
            try { startActivity(intent); } catch (android.content.ActivityNotFoundException ignored) { }
        } else if (link == 4 && !users.isEmpty()) {
            List<NearbyUserResponse> snapshot = new ArrayList<>(users); String[] names = new String[snapshot.size()];
            for (int i = 0; i < names.length; i++) {
                NearbyUserResponse user = snapshot.get(i);
                names[i] = getString(R.string.radar_user_distance, SocialUi.name(user.firstName, user.lastName), user.distanceKm);
            }
            results = new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.radar_results_title).setItems(names, (dialog, position) -> openUser(snapshot.get(position).userId))
                    .setNegativeButton(R.string.btn_close, null).show();
        }
    }
    private void openUser(String id) {
        if (binding == null || busy || id == null || id.isEmpty()) return;
        Double distance = null;
        for (NearbyUserResponse user : users) if (id.equals(user.userId)) { distance = user.distanceKm; break; }
        new UserInfoDialog((MainActivity) requireActivity(), id, null, distance).show(getViewLifecycleOwner());
    }
    @Override public void onStart() { super.onStart(); if (studyMap != null) studyMap.start(); }
    @Override public void onPause() { if (studyMap != null) studyMap.pause(); super.onPause(); }
    @Override public void onStop() {
        ++version;
        if (location != null) location.cancel();
        if (active != null) { cancelRequest(active); active = null; }
        if (results != null) { results.dismiss(); results = null; }
        users = Collections.emptyList();
        if (binding != null && !awaitingPermission) { busy(false); status(R.string.radar_hint, 0); }
        if (studyMap != null) { studyMap.clearRadar(); studyMap.stop(); }
        super.onStop();
    }
    @Override public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putBoolean("radar_permission", awaitingPermission); state.putDouble("radar_radius", radius);
        if (studyMap != null) { Bundle mapState = new Bundle(); studyMap.save(mapState); state.putBundle("map_state", mapState); }
    }
    @Override public void onLowMemory() { super.onLowMemory(); if (studyMap != null) studyMap.lowMemory(); }
    @Override public void onDestroyView() {
        ++version;
        if (location != null) location.cancel();
        if (results != null) { results.dismiss(); results = null; }
        active = null; users = Collections.emptyList();
        if (studyMap != null) { studyMap.destroy(); studyMap = null; }
        super.onDestroyView();
    }
}
