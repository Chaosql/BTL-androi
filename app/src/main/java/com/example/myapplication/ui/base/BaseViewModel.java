package com.example.myapplication.ui.base;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import retrofit2.Call;

public abstract class BaseViewModel extends AndroidViewModel {
    private final Set<UiOperation<?>> operations = new HashSet<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    protected BaseViewModel(Application application) { super(application); }
    public final LiveData<Boolean> loading() { return loading; }
    protected final <T> UiOperation<T> execute(Call<T> call) { return execute(call, value -> value); }
    protected final <T> UiOperation<T> execute(Call<T> call, Function<T, T> transform) {
        UiOperation<T> operation = new UiOperation<>(call, transform, completed -> {
            operations.remove(completed);
            loading.setValue(!operations.isEmpty());
        });
        operations.add(operation);
        loading.setValue(true);
        return operation;
    }
    protected final <T> void watch(UiOperation<T> operation, java.util.function.Consumer<T> success,
            java.util.function.Consumer<com.example.myapplication.data.remote.network.ApiError> failure) {
        operation.observeForever(new androidx.lifecycle.Observer<UiState<T>>() {
            @Override public void onChanged(UiState<T> state) {
                if (!state.isFinished()) return;
                operation.removeObserver(this);
                if (state.status == UiState.Status.SUCCESS) success.accept(state.data);
                else if (state.status == UiState.Status.ERROR) failure.accept(state.error);
            }
        });
    }
    @Override protected void onCleared() {
        for (UiOperation<?> operation : new ArrayList<>(operations)) operation.cancel();
        operations.clear();
        super.onCleared();
    }
}
