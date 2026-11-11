package com.example.myapplication.ui.base;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ScrollView;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.ui.main.MainActivity;

public abstract class BasePreviewDialog<B extends androidx.viewbinding.ViewBinding> {
    protected final MainActivity activity;
    protected B binding;
    private final BindingInflater<B> inflater;
    private DialogOwner dialogOwner = new DialogOwner();

    protected final <V extends androidx.lifecycle.ViewModel> V viewModel(Class<V> type) {
        return new androidx.lifecycle.ViewModelProvider(dialogOwner,
                new androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory(activity.getApplication())).get(type);
    }
    protected <T> void request(UiOperation<T> operation, java.util.function.Consumer<T> success,
            java.util.function.Consumer<com.example.myapplication.data.remote.network.ApiError> failure) {
        DialogOwner owner = dialogOwner;
        operation.observe(owner, state -> {
            if (binding == null || activity.isDestroyed() || !state.isFinished()) return;
            operation.removeObservers(owner);
            if (state.status == UiState.Status.SUCCESS) success.accept(state.data);
            else if (state.status == UiState.Status.ERROR) failure.accept(state.error);
        });
    }

    protected final <T> void observe(androidx.lifecycle.LiveData<T> state, java.util.function.Consumer<T> render) {
        state.observe(dialogOwner, value -> { if (binding != null) render.accept(value); });
    }

    protected BasePreviewDialog(MainActivity activity, BindingInflater<B> inflater) {
        this.activity = activity;
        this.inflater = inflater;
    }

    public final void show() {
        show(activity);
    }
    public final void show(androidx.lifecycle.LifecycleOwner owner) {
        if (dialogOwner.lifecycle.getCurrentState() == androidx.lifecycle.Lifecycle.State.DESTROYED)
            dialogOwner = new DialogOwner();
        dialogOwner.lifecycle.setCurrentState(androidx.lifecycle.Lifecycle.State.CREATED);
        com.example.myapplication.databinding.LayoutDialogContainerBinding shell =
                com.example.myapplication.databinding.LayoutDialogContainerBinding.inflate(LayoutInflater.from(activity));
        binding = inflater.inflate(LayoutInflater.from(activity), shell.contentHost, false);
        if (owner.getLifecycle().getCurrentState() == androidx.lifecycle.Lifecycle.State.DESTROYED) {
            dialogOwner.close(); onDismissed(); binding = null; return;
        }
        View content = binding.getRoot();
        View container = content;
        if (!(content instanceof ScrollView)) {
            shell.contentHost.addView(content);
            container = shell.getRoot();
        }
        AlertDialog dialog = new AlertDialog.Builder(activity).setView(container).create();
        bind(content, dialog);
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
        owner.getLifecycle().addObserver(observer);
        dialog.setOnDismissListener(ignored -> {
            dialogOwner.close();
            onDismissed();
            binding = null;
            owner.getLifecycle().removeObserver(observer);
        });
        dialog.show();
        dialogOwner.lifecycle.setCurrentState(androidx.lifecycle.Lifecycle.State.STARTED);
        }

    protected abstract void bind(View content, AlertDialog dialog);
    protected void onDismissed() { }
    protected void cancelRequest(UiOperation<?> call) { call.cancel(); }
    private static final class DialogOwner implements androidx.lifecycle.LifecycleOwner, androidx.lifecycle.ViewModelStoreOwner {
        private final androidx.lifecycle.LifecycleRegistry lifecycle = new androidx.lifecycle.LifecycleRegistry(this);
        private final androidx.lifecycle.ViewModelStore store = new androidx.lifecycle.ViewModelStore();
        @Override public androidx.lifecycle.Lifecycle getLifecycle() { return lifecycle; }
        @Override public androidx.lifecycle.ViewModelStore getViewModelStore() { return store; }
        void close() {
            lifecycle.setCurrentState(androidx.lifecycle.Lifecycle.State.DESTROYED);
            store.clear();
        }
    }
}
