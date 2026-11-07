package com.example.myapplication.ui.base;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import com.example.myapplication.data.remote.network.ApiCallback;
import com.example.myapplication.data.remote.network.ApiError;
import com.example.myapplication.data.remote.network.ApiRequests;
import java.util.function.Consumer;
import java.util.function.Function;
import retrofit2.Call;

public final class UiOperation<T> extends LiveData<UiState<T>> {
    private final Call<T> call;
    private final Function<T, T> transform;
    private final Consumer<UiOperation<?>> finished;
    private boolean started;
    private boolean cancelled;

    UiOperation(Call<T> call, Function<T, T> transform, Consumer<UiOperation<?>> finished) {
        super(UiState.loading());
        this.call = call; this.transform = transform; this.finished = finished;
    }
    @Override protected void onActive() {
        if (started || cancelled) return;
        started = true;
        ApiRequests.enqueue(call, new ApiCallback<T>() {
            @Override public void onSuccess(T result) {
                dispatch(() -> {
                    if (cancelled) return;
                    try { complete(UiState.success(transform.apply(result))); }
                    catch (IllegalArgumentException error) { complete(UiState.error(new ApiError(422, error.getMessage()))); }
                });
            }
            @Override public void onError(ApiError error) {
                dispatch(() -> { if (!cancelled) complete(UiState.error(error)); });
            }
        });
    }
    private void complete(UiState<T> state) {
        setValue(state);
        finished.accept(this);
    }
    public void cancel() {
        cancelled = true;
        call.cancel();
        dispatch(() -> complete(UiState.cancelled()));
    }
    public boolean isCanceled() { return cancelled; }
    private static void dispatch(Runnable action) {
        if (Looper.myLooper() == Looper.getMainLooper()) action.run();
        else new Handler(Looper.getMainLooper()).post(action);
    }
}
