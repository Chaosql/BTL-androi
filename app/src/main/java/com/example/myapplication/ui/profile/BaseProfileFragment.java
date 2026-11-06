package com.example.myapplication.ui.profile;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.viewbinding.ViewBinding;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.response.UserDto;
import com.example.myapplication.data.repository.profile.ProfileRepository;
import com.example.myapplication.data.repository.schedule.BookingRepository;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.base.BindingInflater;
import com.example.myapplication.ui.main.MainActivity;
import com.example.myapplication.ui.main.UiScreen;
import java.time.LocalDate;
import java.time.Period;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseProfileFragment<B extends ViewBinding> extends BaseFragment<B> {
    private UserDto user;
    private boolean loading;
    private boolean reloadPending;
    private int loadVersion;
    private boolean uploading;
    private ExecutorService images;
    private final ActivityResultLauncher<String> avatarPicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null && binding != null) uploadAvatar(uri);
            });

    protected BaseProfileFragment(BindingInflater<B> inflater) { super(inflater); }

    protected void bindProfile(View root, boolean editingProfile) {
        images = Executors.newSingleThreadExecutor();
        user = null;
        loading = false;
        reloadPending = false;
        uploading = false;
        render(root, new UserDto());
        getParentFragmentManager().setFragmentResultListener(ProfileUi.UPDATED, getViewLifecycleOwner(),
                (key, data) -> loadProfile(root));
        on(root, R.id.btnUpdateProfile, () -> {
            if (loading || uploading) return;
            if (!new PreferenceManager(requireContext()).isLogin()) { go(UiScreen.LOGIN); return; }
            if (user == null) { loadProfile(root); return; }
            if (editingProfile) requireActivity().getOnBackPressedDispatcher().onBackPressed();
            else go(UiScreen.EDIT_PROFILE);
        });
        on(root, R.id.cvSettings, () -> go(UiScreen.ACCOUNT_SETTINGS));
        on(root, R.id.cvAppointment, () -> go(UiScreen.SCHEDULE));
        on(root, R.id.cvHonor, () -> new SubjectManagementDialog(host()).show());
        Runnable personal = () -> {
            if (user != null) new EditPersonalInfoDialog(host(), user).show();
        };
        Runnable intro = () -> {
            if (user != null) new EditIntroductionDialog(host(), user).show();
        };
        on(root, R.id.tvAddInfo, personal);
        on(root, R.id.cvInfo, personal);
        if (editingProfile) {
            on(root, R.id.tvIntroduction, intro);
            on(root, R.id.ivTogglePassword, intro);
        }
        on(root, R.id.imgEditAvatar, () -> {
            if (user != null && !uploading) avatarPicker.launch("image/*");
        });
        loadProfile(root);
    }

    private MainActivity host() { return (MainActivity) requireActivity(); }

    private void loadProfile(View root) {
        if (!new PreferenceManager(requireContext()).isLogin()) return;
        if (loading) { reloadPending = true; return; }
        loading = true;
        int version = ++loadVersion;
        request(new ProfileRepository(requireContext()).getCurrentUser(), response -> {
            loading = false;
            if (reloadPending) { reloadPending = false; loadProfile(root); return; }
            if (response.data == null) {
                Toast.makeText(requireContext(), R.string.profile_empty_response, Toast.LENGTH_LONG).show();
                return;
            }
            user = response.data;
            render(root, user);
            if (root.findViewById(R.id.tvSubjectCount) != null) {
                String id = new PreferenceManager(requireContext()).getUserId();
                request(new ProfileRepository(requireContext()).getSubjects(id, null), subjects -> {
                    if (version == loadVersion)
                        text(root, R.id.tvSubjectCount, subjects.data == null ? "—" : String.valueOf(subjects.data.size()));
                });
                request(new BookingRepository(requireContext()).getMyBookings(), bookings -> {
                    if (version == loadVersion)
                        text(root, R.id.tvAppointmentCount, bookings.data == null ? "—" : String.valueOf(bookings.data.size()));
                });
            }
        }, error -> {
            loading = false;
            if (reloadPending) { reloadPending = false; loadProfile(root); return; }
            Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show();
        });
    }

    private String display(String value) {
        return value == null || value.trim().isEmpty() ? getString(R.string.profile_unknown) : value;
    }

    private void render(View root, UserDto data) {
        text(root, R.id.tvUsername, display((ProfileUi.value(data.lastName) + " " + ProfileUi.value(data.firstName)).trim()));
        text(root, R.id.tvBuddyStatus, getString(Boolean.TRUE.equals(data.buddyActive)
                ? R.string.profile_radar_on : R.string.profile_radar_off));
        text(root, R.id.tvIntroduction, data.statusTag == null || data.statusTag.isEmpty() ? getString(R.string.profile_no_intro) : data.statusTag);
        LocalDate birth = ProfileUi.date(data.dateOfBirth);
        String age = birth != null && !birth.isAfter(LocalDate.now())
                ? String.valueOf(Period.between(birth, LocalDate.now()).getYears()) : getString(R.string.profile_unknown);
        text(root, R.id.tvAge, getString(R.string.profile_age, age));
        String gender = data.gender == null ? getString(R.string.profile_unknown) :
                getString("MALE".equals(data.gender) ? R.string.profile_gender_male :
                        "FEMALE".equals(data.gender) ? R.string.profile_gender_female : R.string.profile_gender_other);
        text(root, R.id.tvGender, getString(R.string.profile_gender, gender));
        text(root, R.id.tvSchool, getString(R.string.profile_school, display(data.university)));
        text(root, R.id.tvMajor, getString(R.string.profile_major, display(data.majorName)));
        text(root, R.id.tvLocation, getString(R.string.profile_location, display(data.location)));
        String score = data.trustScore == null ? "—" : String.format(Locale.US, "%.1f", data.trustScore);
        text(root, R.id.tvFriendsCount, getString(R.string.profile_score, score));
        text(root, R.id.tvFavoriteRating, score);
        text(root, R.id.tvAppointmentCount, "—");
        text(root, R.id.tvSubjectCount, data.subjects == null ? "—" : String.valueOf(data.subjects.size()));
        ImageView avatar = root.findViewById(R.id.imgAvatar);
        if (avatar == null) avatar = root.findViewById(R.id.ivAvatar);
        if (avatar != null) Glide.with(this).load(data.avatar).placeholder(R.drawable.avatar).error(R.drawable.avatar).into(avatar);
    }

    private void uploadAvatar(Uri uri) {
        if (user == null || uploading) return;
        uploading = true;
        View view = binding.getRoot();
        android.content.ContentResolver resolver = requireContext().getContentResolver();
        String id = new PreferenceManager(requireContext()).getUserId();
        images.execute(() -> {
            try {
                okhttp3.MultipartBody.Part part = AvatarImage.read(resolver, uri);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (binding == null || binding.getRoot() != view) return;
                    request(new ProfileRepository(requireContext()).uploadAvatar(id, part), response -> {
                        uploading = false;
                        ProfileUi.saved(requireActivity());
                    }, error -> {
                        uploading = false;
                        Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show();
                    });
                });
            } catch (Exception error) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (binding == null || binding.getRoot() != view) return;
                    uploading = false;
                    Toast.makeText(requireContext(), R.string.profile_avatar_error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override public void onDestroyView() {
        if (images != null) images.shutdownNow();
        user = null;
        super.onDestroyView();
    }
}
