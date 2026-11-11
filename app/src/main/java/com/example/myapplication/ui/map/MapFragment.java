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
import com.example.myapplication.data.remote.dto.response.*;
import com.example.myapplication.ui.map.MapViewModel;
import com.example.myapplication.ui.profile.ProfileViewModel;
import com.example.myapplication.ui.friends.SocialUi;
import com.example.myapplication.ui.friends.UserInfoDialog;
import com.example.myapplication.ui.main.MainActivity;
import java.util.*;
import com.example.myapplication.ui.base.UiOperation;

public class MapFragment extends BaseFragment<FragmentMapBinding> {
    private StudyMap studyMap;
    private DeviceLocation location;
    private MapViewModel model;
    private UiOperation<?> active;
    private boolean busy, awaitingPermission, radiusError;
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
        String avatarOwner = new PreferenceManager(requireContext()).getUserId();
        request(viewModel(ProfileViewModel.class).getCurrentUser(), response -> {
            if (response.data != null && avatarOwner.equals(response.data.id)
                    && avatarOwner.equals(new PreferenceManager(requireContext()).getUserId())) showAvatar(response.data.avatar);
        }, error -> { });
        request(viewModel(com.example.myapplication.ui.notification.NotificationViewModel.class)
                .getUnreadNotificationCount(), response -> {
            int count = response.data == null ? 0 : Math.max(0, response.data);
            binding.tvNotificationBadge.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
            binding.tvNotificationBadge.setText(count > 99 ? "99+" : String.valueOf(count));
            binding.btnNotification.setContentDescription(count > 0 ? getString(R.string.notification_title_count, count)
                    : getString(R.string.title_notification));
        }, error -> { });
    }

    private void showAvatar(String url) {
        com.bumptech.glide.Glide.with(binding.imgAvatar).load(url)
                .placeholder(R.drawable.avatar).error(R.drawable.avatar).fallback(R.drawable.avatar)
                .circleCrop().into(binding.imgAvatar);
    }

    @Override protected void onScreenCreated(View root, Bundle state) {
        enableScreenCatalog(binding.imgCrown);
        model = viewModel(com.example.myapplication.ui.map.MapViewModel.class); location = new DeviceLocation(requireContext());
        owner = new PreferenceManager(requireContext()).getUserId();
        awaitingPermission = state != null && state.getBoolean("radar_permission");
        radius = state == null ? 3 : state.getDouble("radar_radius", 3);
        users = Collections.emptyList(); link = 0;
        studyMap = new StudyMap(binding.mapView, message -> {
            if (binding == null || message.isEmpty() || message.equals(getString(R.string.map_loading))) return;
            android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show();
        });
        studyMap.create(state == null ? null : state.getBundle("map_state"));
        studyMap.onUserClick(this::openUser);
        model.radar().observe(getViewLifecycleOwner(), result -> {
            if (result == null) {
                if (!owner.equals(new PreferenceManager(requireContext()).getUserId())) {
                    busy(false); users = Collections.emptyList(); studyMap.clearRadar(); status(R.string.radar_hint, 0);
                }
                return;
            }
            if (result.status == com.example.myapplication.ui.base.UiState.Status.LOADING) status(R.string.radar_scanning, 0);
            else if (result.status == com.example.myapplication.ui.base.UiState.Status.ERROR) failed(result.error.message);
            else if (result.status == com.example.myapplication.ui.base.UiState.Status.SUCCESS) {
                busy(false); users = result.data.users;
                studyMap.showRadar(result.data.latitude, result.data.longitude, result.data.radius, users);
                binding.tvRadarStatus.setTextColor(requireContext().getColor(R.color.input_text));
                binding.tvRadarStatus.setVisibility(View.VISIBLE);
                binding.tvRadarStatus.setText(users.isEmpty() ? getString(R.string.radar_empty, radius)
                        : getString(R.string.radar_results, users.size(), radius)); link = users.isEmpty() ? 0 : 4;
            }
        });
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
        binding.tvRadarStatus.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        binding.etDistance.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence text, int start, int before, int count) {
                if (radiusError && binding != null) { radiusError = false; status(R.string.radar_hint, 0); }
            }
            @Override public void afterTextChanged(android.text.Editable text) { }
        });
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
    private void status(int text, int action) {
        boolean idle = text == R.string.radar_hint;
        boolean progress = text == R.string.radar_checking || text == R.string.radar_locating || text == R.string.radar_scanning;
        if (!idle && !progress) { showRadarError(getString(text)); return; }
        binding.tvRadarStatus.setTextColor(requireContext().getColor(R.color.input_text));
        binding.tvRadarStatus.setText(idle ? "" : getString(text));
        binding.tvRadarStatus.setVisibility(idle ? View.GONE : View.VISIBLE);
        link = action;
    }
    private void showRadarError(String message) {
        binding.tvRadarStatus.setText(null);
        binding.tvRadarStatus.setVisibility(View.GONE);
        link = 0;
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show();
    }
    private void scan() {
        if (busy || awaitingPermission) return;
        if (!new PreferenceManager(requireContext()).isLogin()) { go(UiScreen.LOGIN); return; }
        try { radius = RadarData.radius(binding.etDistance.getText().toString()); }
        catch (IllegalArgumentException error) {
            radiusError = true;
            binding.etDistance.requestFocus(); status(R.string.radar_invalid_radius, 0); return;
        }
        radiusError = false;
        android.view.inputmethod.InputMethodManager keyboard = (android.view.inputmethod.InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (keyboard != null) keyboard.hideSoftInputFromWindow(binding.etDistance.getWindowToken(), 0);
        binding.etDistance.clearFocus();
        owner = new PreferenceManager(requireContext()).getUserId();
        busy(true); users = Collections.emptyList(); studyMap.clearRadar(); status(R.string.radar_checking, 0);
        int current = ++version;
        UiOperation<BaseResponse<UserDto>> call = viewModel(com.example.myapplication.ui.profile.ProfileViewModel.class).getCurrentUser(); active = call;
        request(call, response -> {
            if (!current(current)) return;
            active = null;
            if (response.data == null || !owner.equals(response.data.id)) { failed(getString(R.string.feature_invalid)); return; }
            showAvatar(response.data.avatar);
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
        if (current(current)) model.scan(point.getLatitude(), point.getLongitude(), radius);
    }
    private void failed(String message) {
        active = null; busy(false);
        showRadarError(message);
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
        if (model != null) model.cancelRadar();
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
        if (model != null) model.cancelRadar();
        if (location != null) location.cancel();
        if (results != null) { results.dismiss(); results = null; }
        active = null; users = Collections.emptyList();
        if (studyMap != null) { studyMap.destroy(); studyMap = null; }
        super.onDestroyView();
    }
}
