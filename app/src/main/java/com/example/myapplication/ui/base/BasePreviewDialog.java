package com.example.myapplication.ui.base;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.R;
import com.example.myapplication.ui.main.MainActivity;
import com.example.myapplication.ui.main.UiDialogs;
import com.example.myapplication.ui.main.UiScreen;

public abstract class BasePreviewDialog<B extends androidx.viewbinding.ViewBinding> {
    protected final MainActivity activity;
    protected B binding;
    private final BindingInflater<B> inflater;
    private final java.util.List<retrofit2.Call<?>> requests = new java.util.ArrayList<>();

    protected <T> void request(retrofit2.Call<T> call, java.util.function.Consumer<T> success,
            java.util.function.Consumer<com.example.myapplication.data.remote.network.ApiError> failure) {
        requests.add(call);
        com.example.myapplication.data.remote.network.ApiRequests.enqueue(call,
                new com.example.myapplication.data.remote.network.ApiCallback<T>() {
                    @Override public void onSuccess(T result) {
                        requests.remove(call);
                        if (binding != null && !activity.isDestroyed()) success.accept(result);
                    }
                    @Override public void onError(com.example.myapplication.data.remote.network.ApiError error) {
                        requests.remove(call);
                        if (binding != null && !activity.isDestroyed()) failure.accept(error);
                    }
                });
    }

    protected BasePreviewDialog(MainActivity activity, BindingInflater<B> inflater) {
        this.activity = activity;
        this.inflater = inflater;
    }

    public final void show() {
        binding = inflater.inflate(LayoutInflater.from(activity), null, false);
        View content = binding.getRoot();
        View container = content;
        if (!(content instanceof ScrollView)) {
            ScrollView scroll = new ScrollView(activity);
            scroll.setFillViewport(false);
            scroll.addView(content, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            container = scroll;
        }
        AlertDialog dialog = new AlertDialog.Builder(activity).setView(container).create();
        for (int id : new int[]{R.id.btnCancel, R.id.btnCancelAdd, R.id.ivClose, R.id.btnClose})
            BaseFragment.on(content, id, dialog::dismiss);
        for (int id : new int[]{R.id.btnSave, R.id.btnConfirm, R.id.btnConfirmAdd, R.id.btnConfirmCancel,
                R.id.btnRate, R.id.btnAccept, R.id.btnReject, R.id.btnComplete, R.id.btnAddFriend,
                R.id.tvOptionPin, R.id.tvOptionBlock, R.id.tvOptionDelete, R.id.tvRecall, R.id.tvDelete}) {
            BaseFragment.on(content, id, () -> {
                Toast.makeText(activity, activity.getString(R.string.ui_uidialogs_text_17), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }
        BaseFragment.on(content, R.id.btnGetStarted, () -> { dialog.dismiss(); activity.showScreen(UiScreen.MAP); });
        BaseFragment.on(content, R.id.btnViewSchedule, () -> { dialog.dismiss(); activity.showScreen(UiScreen.SCHEDULE); });
        BaseFragment.on(content, R.id.btnViewProfile, () -> { dialog.dismiss(); activity.showScreen(UiScreen.PROFILE); });
        BaseFragment.on(content, R.id.tvOptionViewProfile, () -> { dialog.dismiss(); activity.showScreen(UiScreen.PROFILE); });
        BaseFragment.on(content, R.id.tvOptionCreateGroup, () -> { dialog.dismiss(); activity.showScreen(UiScreen.GROUP_DETAIL); });
        BaseFragment.on(content, R.id.btnMenuNewChat, () -> { dialog.dismiss(); activity.showScreen(UiScreen.SEARCH_USER); });
        BaseFragment.on(content, R.id.btnMenuPendingRequests, () -> { dialog.dismiss(); activity.showScreen(UiScreen.PENDING_REQUESTS); });
        BaseFragment.on(content, R.id.btnAddSubject, () -> { dialog.dismiss(); UiDialogs.show(activity, R.layout.dialog_add_subject); });
        BaseFragment.on(content, R.id.btnCancel, dialog::dismiss);

        bind(content, dialog);
        for (int id : new int[]{R.id.tvEmojiLike, R.id.tvEmojiHeart, R.id.tvEmojiLaugh, R.id.tvEmojiWow,
                R.id.tvEmojiSad, R.id.tvEmojiPray, R.id.tvReply})
            BaseFragment.on(content, id, dialog::dismiss);
        BaseFragment.on(content, R.id.tvForward, () -> { dialog.dismiss(); UiDialogs.show(activity, R.layout.dialog_forward_select); });
        dialog.setOnShowListener(ignored -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                android.util.DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
                int width = Math.min((int) (metrics.widthPixels * 0.9f), (int) (560 * metrics.density));
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                dialog.getWindow().setLayout(width,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });
        androidx.lifecycle.DefaultLifecycleObserver observer = new androidx.lifecycle.DefaultLifecycleObserver() {
            @Override public void onDestroy(androidx.lifecycle.LifecycleOwner owner) { dialog.dismiss(); }
        };
        activity.getLifecycle().addObserver(observer);
        dialog.setOnDismissListener(ignored -> {
            for (retrofit2.Call<?> call : requests) call.cancel();
            requests.clear();
            binding = null;
            activity.getLifecycle().removeObserver(observer);
        });
        dialog.show();
        }

    protected abstract void bind(View content, AlertDialog dialog);
}
