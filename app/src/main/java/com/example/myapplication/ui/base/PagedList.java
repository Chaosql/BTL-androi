package com.example.myapplication.ui.base;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplication.data.remote.dto.response.BaseResponse;
import com.example.myapplication.data.remote.dto.response.PageResponse;
import com.example.myapplication.data.remote.network.ApiError;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class PagedList<T> {
    public static final class State<T> {
        public final List<T> items;
        public final boolean loading, last;
        public final ApiError error;
        State(Collection<T> items, boolean loading, boolean last, ApiError error) {
            this.items = Collections.unmodifiableList(new ArrayList<>(items));
            this.loading = loading; this.last = last; this.error = error;
        }
    }
    private final BaseViewModel owner;
    private final BiFunction<String, Integer, UiOperation<BaseResponse<PageResponse<T>>>> loader;
    private final Function<T, String> key;
    private final Map<String, T> items = new LinkedHashMap<>();
    private final MutableLiveData<State<T>> state = new MutableLiveData<>(new State<>(Collections.emptyList(), false, false, null));
    private UiOperation<?> active;
    private int page = -1, version;
    private boolean loading, last, initialized;
    private String keyword = "";
    public PagedList(BaseViewModel owner, BiFunction<String, Integer, UiOperation<BaseResponse<PageResponse<T>>>> loader, Function<T, String> key) {
        this.owner = owner; this.loader = loader; this.key = key;
    }
    public LiveData<State<T>> state() { return state; }
    public void initialize(String query) { if (!initialized) load(query, true); }
    public void cancel() {
        version++; if (active != null) active.cancel(); active = null; loading = false;
    }
    public void load(String query, boolean reset) {
        query = query == null ? "" : query.trim();
        if (!query.equals(keyword)) reset = true;
        if (!reset && (loading || last)) return;
        cancel(); initialized = true; keyword = query;
        int next = reset ? 0 : page + 1;
        boolean replace = reset;
        loading = true; publish(null);
        int current = version;
        UiOperation<BaseResponse<PageResponse<T>>> operation = loader.apply(keyword, next); active = operation;
        owner.watch(operation, response -> {
            if (version != current) return;
            active = null; loading = false;
            if (response.data == null || response.data.content == null) { publish(new ApiError(422, null)); return; }
            if (replace) items.clear();
            for (T item : response.data.content) {
                if (item == null) continue;
                String id = key.apply(item);
                if (id == null || id.isEmpty()) continue;
                items.remove(id); items.put(id, item);
            }
            page = next;
            last = response.data.totalPages != null && response.data.totalPages > 0
                    ? next + 1 >= response.data.totalPages : Boolean.TRUE.equals(response.data.last);
            publish(null);
        }, error -> { if (current == version) { active = null; loading = false; publish(error); } });
    }
    public void replace(T item) {
        if (item != null && items.containsKey(key.apply(item))) { items.put(key.apply(item), item); publish(null); }
    }
    public void remove(String id) { items.remove(id); publish(null); }
    private void publish(ApiError error) { state.setValue(new State<>(items.values(), loading, last, error)); }
}
