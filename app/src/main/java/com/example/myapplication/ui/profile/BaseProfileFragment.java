package com.example.myapplication.ui.profile;

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
import com.example.myapplication.ui.profile.ProfileViewModel;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.base.BindingInflater;
import com.example.myapplication.ui.main.MainActivity;
import com.example.myapplication.ui.main.UiScreen;
import java.time.LocalDate;
import java.time.Period;
import java.util.Locale;

public abstract class BaseProfileFragment<B extends ViewBinding> extends BaseFragment<B> {
    private ProfileViews views;
    protected abstract ProfileViews profileViews();
    private UserDto user;
    private boolean loading;
    private boolean uploading;
    private ProfileViewModel model;
    private final ActivityResultLauncher<String> avatarPicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null && binding != null) model.prepareAvatar(uri);
            });

    protected BaseProfileFragment(BindingInflater<B> inflater) { super(inflater); }

    protected void bindProfile(View root, boolean editingProfile) {
        views = profileViews();
        model = viewModel(ProfileViewModel.class);
        user = null;
        loading = false;
        uploading = false;
        renderLoading();
        model.summary().observe(getViewLifecycleOwner(), state -> {
            loading = state.status == com.example.myapplication.ui.base.UiState.Status.LOADING;
            if (state.status == com.example.myapplication.ui.base.UiState.Status.ERROR) {
                Toast.makeText(requireContext(), state.error.message, Toast.LENGTH_LONG).show();
            } else if (state.status == com.example.myapplication.ui.base.UiState.Status.SUCCESS) {
                user = state.data.user; render(root, user);
                text(views.tvSubjectCount, state.data.subjects == null ? getString(R.string.profile_loading_value) : String.valueOf(state.data.subjects));
                text(views.tvAppointmentCount, state.data.bookings == null ? getString(R.string.profile_loading_value) : String.valueOf(state.data.bookings));
            }
        });
        model.avatarState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            uploading = state.status == com.example.myapplication.ui.base.UiState.Status.LOADING;
            if (state.status == com.example.myapplication.ui.base.UiState.Status.ERROR) {
                Toast.makeText(requireContext(), state.error.message, Toast.LENGTH_LONG).show(); model.acknowledgeAvatar();
            } else if (state.status == com.example.myapplication.ui.base.UiState.Status.SUCCESS) {
                model.acknowledgeAvatar(); ProfileUi.saved(requireActivity());
            }
        });
        getParentFragmentManager().setFragmentResultListener(ProfileUi.UPDATED, getViewLifecycleOwner(),
                (key, data) -> loadProfile(root));
        on(views.btnUpdateProfile, () -> {
            if (loading || uploading) return;
            if (!new PreferenceManager(requireContext()).isLogin()) { go(UiScreen.LOGIN); return; }
            if (user == null) { loadProfile(root); return; }
            if (editingProfile) requireActivity().getOnBackPressedDispatcher().onBackPressed();
            else go(UiScreen.EDIT_PROFILE);
        });
        on(views.cvSettings, () -> go(UiScreen.ACCOUNT_SETTINGS));
        on(views.cvAppointment, () -> go(UiScreen.SCHEDULE));
        on(views.cvHonor, () -> new SubjectManagementDialog(host()).show());
        Runnable personal = () -> {
            if (user != null) new EditPersonalInfoDialog(host(), user).show();
        };
        Runnable intro = () -> {
            if (user != null) new EditIntroductionDialog(host(), user).show();
        };
        if (editingProfile) {
            on(views.tvAddInfo, personal);
            on(views.cvInfo, personal);
            on(views.tvIntroduction, intro);
            on(views.ivTogglePassword, intro);
        }
        on(views.imgEditAvatar, () -> {
            if (user != null && !uploading) avatarPicker.launch("image/*");
        });
        model.loadSummary(views.tvSubjectCount != null, false);
    }

    private MainActivity host() { return (MainActivity) requireActivity(); }

    private void loadProfile(View root) { model.loadSummary(views.tvSubjectCount != null, true); }

    private String display(String value) {
        return value == null || value.trim().isEmpty() ? getString(R.string.profile_unknown) : value;
    }

    private void renderLoading() {
        String placeholder = getString(R.string.profile_loading_value);
        for (android.widget.TextView field : new android.widget.TextView[] {
                views.tvUsername, views.tvBuddyStatus, views.tvIntroduction, views.tvAge,
                views.tvGender, views.tvSchool, views.tvMajor, views.tvLocation,
                views.tvFavoriteRating, views.tvAppointmentCount, views.tvSubjectCount }) {
            text(field, placeholder);
        }
        views.avatar.setImageResource(R.drawable.avatar);
    }

    private void render(View root, UserDto data) {
        text(views.tvUsername, display((ProfileUi.value(data.lastName) + " " + ProfileUi.value(data.firstName)).trim()));
        text(views.tvBuddyStatus, getString(Boolean.TRUE.equals(data.buddyActive)
                ? R.string.profile_radar_on : R.string.profile_radar_off));
        text(views.tvIntroduction, data.statusTag == null || data.statusTag.isEmpty() ? getString(R.string.profile_no_intro) : data.statusTag);
        LocalDate birth = ProfileUi.date(data.dateOfBirth);
        String age = birth != null && !birth.isAfter(LocalDate.now())
                ? String.valueOf(Period.between(birth, LocalDate.now()).getYears()) : getString(R.string.profile_unknown);
        text(views.tvAge, getString(R.string.profile_age, age));
        String gender = data.gender == null ? getString(R.string.profile_unknown) :
                getString("MALE".equals(data.gender) ? R.string.profile_gender_male :
                        "FEMALE".equals(data.gender) ? R.string.profile_gender_female : R.string.profile_gender_other);
        text(views.tvGender, getString(R.string.profile_gender, gender));
        text(views.tvSchool, getString(R.string.profile_school, display(data.university)));
        text(views.tvMajor, getString(R.string.profile_major, display(data.majorName)));
        text(views.tvLocation, getString(R.string.profile_location, display(data.location)));
        String score = data.trustScore == null ? getString(R.string.profile_loading_value) : String.format(Locale.US, "%.1f", data.trustScore);
        text(views.tvFavoriteRating, score);
        text(views.tvAppointmentCount, getString(R.string.profile_loading_value));
        text(views.tvSubjectCount, data.subjects == null ? getString(R.string.profile_loading_value) : String.valueOf(data.subjects.size()));
        Glide.with(this).load(data.avatar).placeholder(R.drawable.avatar).error(R.drawable.avatar).into(views.avatar);
    }

    @Override public void onDestroyView() {
        user = null;
        views = null;
        super.onDestroyView();
    }

    protected static final class ProfileViews {
        final View btnUpdateProfile;
        final View cvAppointment;
        final View cvHonor;
        final View cvInfo;
        final View cvSettings;
        final View imgEditAvatar;
        final View ivTogglePassword;
        final View tvAddInfo;
        final android.widget.TextView tvAge;
        final android.widget.TextView tvAppointmentCount;
        final android.widget.TextView tvBuddyStatus;
        final android.widget.TextView tvFavoriteRating;
        final android.widget.TextView tvGender;
        final android.widget.TextView tvIntroduction;
        final android.widget.TextView tvLocation;
        final android.widget.TextView tvMajor;
        final android.widget.TextView tvSchool;
        final android.widget.TextView tvSubjectCount;
        final android.widget.TextView tvUsername;
        final ImageView avatar;

        ProfileViews(com.example.myapplication.databinding.FragmentProfileBinding binding) {
            btnUpdateProfile = binding.btnUpdateProfile;
            cvAppointment = binding.cvAppointment;
            cvHonor = binding.cvHonor;
            cvInfo = binding.cvInfo;
            cvSettings = binding.cvSettings;
            imgEditAvatar = null;
            ivTogglePassword = null;
            tvAddInfo = null;
            tvAge = binding.tvAge;
            tvAppointmentCount = binding.tvAppointmentCount;
            tvBuddyStatus = binding.tvBuddyStatus;
            tvFavoriteRating = binding.tvFavoriteRating;
            tvGender = binding.tvGender;
            tvIntroduction = binding.tvIntroduction;
            tvLocation = binding.tvLocation;
            tvMajor = binding.tvMajor;
            tvSchool = binding.tvSchool;
            tvSubjectCount = binding.tvSubjectCount;
            tvUsername = binding.tvUsername;
            avatar = binding.ivAvatar;
        }

        ProfileViews(com.example.myapplication.databinding.FragmentEditProfileBinding binding) {
            btnUpdateProfile = binding.btnUpdateProfile;
            cvAppointment = null;
            cvHonor = null;
            cvInfo = binding.cvInfo;
            cvSettings = null;
            imgEditAvatar = binding.imgEditAvatar;
            ivTogglePassword = binding.ivTogglePassword;
            tvAddInfo = binding.tvAddInfo;
            tvAge = binding.tvAge;
            tvAppointmentCount = null;
            tvBuddyStatus = binding.tvBuddyStatus;
            tvFavoriteRating = null;
            tvGender = binding.tvGender;
            tvIntroduction = binding.tvIntroduction;
            tvLocation = binding.tvLocation;
            tvMajor = binding.tvMajor;
            tvSchool = binding.tvSchool;
            tvSubjectCount = null;
            tvUsername = binding.tvUsername;
            avatar = binding.imgAvatar;
        }
    }
}
