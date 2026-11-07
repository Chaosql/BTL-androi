package com.example.myapplication.ui.profile;

import com.example.myapplication.ui.base.UiOperation;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.viewbinding.ViewBinding;
import com.example.myapplication.R;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.response.UserDto;
import com.example.myapplication.ui.profile.ProfileViewModel;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.base.BindingInflater;
import com.example.myapplication.ui.main.MainActivity;
import java.util.function.Consumer;

abstract class BaseProfileDialog<B extends ViewBinding> extends BasePreviewDialog<B> {
    protected final ProfileViewModel model;
    protected final String userId;
    private final UserDto initialUser;

    BaseProfileDialog(MainActivity activity, BindingInflater<B> inflater, UserDto user) {
        super(activity, inflater);
        model = viewModel(com.example.myapplication.ui.profile.ProfileViewModel.class);
        userId = new PreferenceManager(activity).getUserId();
        initialUser = user;
    }

    protected void loadUser(View save, Consumer<UserDto> success) {
        save.setEnabled(false);
        ProfileUi.inputs(binding.getRoot(), false);
        if (initialUser != null) {
            ProfileUi.inputs(binding.getRoot(), true);
            save.setEnabled(true);
            success.accept(initialUser);
            return;
        }
        if (userId.isEmpty()) return;
        request(model.getProfile(userId), response -> {
            if (response.data == null) { error(activity.getString(R.string.profile_empty_response)); return; }
            save.setEnabled(true);
            ProfileUi.inputs(binding.getRoot(), true);
            success.accept(response.data);
        }, failure -> error(failure.message));
    }

    protected <T> void submit(UiOperation<T> call, View save, View cancel, AlertDialog dialog) {
        save.setEnabled(false);
        ProfileUi.inputs(binding.getRoot(), false);
        dialog.setCancelable(false);
        if (cancel != null) cancel.setEnabled(false);
        request(call, response -> {
            ProfileUi.saved(activity);
            dialog.dismiss();
        }, failure -> {
            save.setEnabled(true);
            ProfileUi.inputs(binding.getRoot(), true);
            dialog.setCancelable(true);
            if (cancel != null) cancel.setEnabled(true);
            error(failure.message);
        });
    }

    protected void error(String message) { Toast.makeText(activity, message, Toast.LENGTH_LONG).show(); }
}
