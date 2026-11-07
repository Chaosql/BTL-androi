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
        model.summary().observe(getViewLifecycleOwner(), state -> {
            loading = state.status == com.example.myapplication.ui.base.UiState.Status.LOADING;
            if (state.status == com.example.myapplication.ui.base.UiState.Status.ERROR) {
                Toast.makeText(requireContext(), state.error.message, Toast.LENGTH_LONG).show();
            } else if (state.status == com.example.myapplication.ui.base.UiState.Status.SUCCESS) {
                user = state.data.user; render(root, user);
                text(views.tvSubjectCount, state.data.subjects == null ? "—" : String.valueOf(state.data.subjects));
                text(views.tvAppointmentCount, state.data.bookings == null ? "—" : String.valueOf(state.data.bookings));
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
        user = null;
        loading = false;
        uploading = false;
        render(root, new UserDto());
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
        on(views.tvBuddyStatus, () -> {
            if (user == null || loading || uploading) return;
            boolean enable = !Boolean.TRUE.equals(user.buddyActive);
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setMessage(getString(R.string.buddy_confirm, getString(enable ? R.string.feature_enable : R.string.feature_disable)))
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        if (user == null || loading) return;
                        loading = true;
                        request(viewModel(com.example.myapplication.ui.profile.ProfileViewModel.class).toggleBuddyStatus(
                                new PreferenceManager(requireContext()).getUserId(),
                                new com.example.myapplication.data.remote.dto.request.ToggleBuddyStatusRequest(enable)), response -> {
                            loading = false;
                            if (response.data == null || !Boolean.valueOf(enable).equals(response.data.buddyActive)) {
                                Toast.makeText(requireContext(), R.string.feature_invalid, Toast.LENGTH_LONG).show(); return;
                            }
                            user = response.data; render(root, user); ProfileUi.updated(host());
                        }, error -> { loading = false; Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show(); });
                    }).show();
        });
        Runnable personal = () -> {
            if (user != null) new EditPersonalInfoDialog(host(), user).show();
        };
        Runnable intro = () -> {
            if (user != null) new EditIntroductionDialog(host(), user).show();
        };
        on(views.tvAddInfo, personal);
        on(views.cvInfo, personal);
        if (editingProfile) {
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
        String score = data.trustScore == null ? "â€”" : String.format(Locale.US, "%.1f", data.trustScore);
        text(views.tvFriendsCount, getString(R.string.profile_score, score));
        text(views.tvFavoriteRating, score);
        text(views.tvAppointmentCount, "â€”");
        text(views.tvSubjectCount, data.subjects == null ? "â€”" : String.valueOf(data.subjects.size()));
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
        final android.widget.TextView tvFriendsCount;
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
            tvFriendsCount = null;
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
            tvFriendsCount = binding.tvFriendsCount;
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
