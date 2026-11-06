package com.example.myapplication.ui.friends.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import com.example.myapplication.ui.base.BindingInflater;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class SocialAdapter<T, B extends ViewBinding> extends RecyclerView.Adapter<SocialAdapter.Holder<B>> {
    protected final List<T> items = new ArrayList<>();
    protected final Set<String> busy = new HashSet<>();
    private final BindingInflater<B> inflater;
    protected SocialAdapter(BindingInflater<B> inflater) { this.inflater = inflater; }
    public void submit(List<T> data) { items.clear(); if (data != null) items.addAll(data); notifyDataSetChanged(); }
    public void busy(String id, boolean value) { if (value) busy.add(id); else busy.remove(id); notifyDataSetChanged(); }
    public boolean isBusy(String id) { return busy.contains(id); }
    public void remove(T item) { int index = items.indexOf(item); if (index >= 0) { items.remove(index); notifyItemRemoved(index); } }
    @Override public int getItemCount() { return items.size(); }
    @NonNull @Override public Holder<B> onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new Holder<>(inflater.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override public void onBindViewHolder(@NonNull Holder<B> holder, int position) { bind(holder.binding, items.get(position)); }
    protected abstract void bind(B binding, T item);
    public static class Holder<B extends ViewBinding> extends RecyclerView.ViewHolder {
        public final B binding;
        Holder(B binding) { super(binding.getRoot()); this.binding = binding; }
    }
}
