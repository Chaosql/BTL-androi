package com.example.myapplication.ui.base;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BasePreviewAdapter extends RecyclerView.Adapter<BasePreviewAdapter.Holder> {
    private final List<PreviewRow> source = new ArrayList<>();
    private final List<PreviewRow> visible = new ArrayList<>();
    protected final BaseFragment owner;
    public BasePreviewAdapter(BaseFragment owner, PreviewRow... rows) {
        this.owner = owner;
        for (PreviewRow row : rows) source.add(row);
        visible.addAll(source);
    }
    @Override public int getItemViewType(int position) { return visible.get(position).layout; }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int layout) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false));
    }
    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        PreviewRow row = visible.get(position);
        for (Map.Entry<Integer, String> entry : row.text.entrySet())
            BaseFragment.text(holder.itemView, entry.getKey(), entry.getValue());
        for (int id : new int[]{R.id.ivFriendAvatar, R.id.ivAvatar, R.id.ivSenderAvatar, R.id.ivMyAvatar, R.id.imgAvatar}) {
            View view = holder.itemView.findViewById(id);
            if (view instanceof ImageView) ((ImageView) view).setImageResource(R.drawable.avatar);
        }
        View image = holder.itemView.findViewById(R.id.ivImageContent);
        if (image instanceof ImageView) ((ImageView) image).setImageResource(R.drawable.bg_profile);
        holder.itemView.setOnClickListener(v -> { if (row.click != null) row.click.run(); });
        bindActions(holder.itemView, row);
    }
    @Override public int getItemCount() { return visible.size(); }
    public void filter(String query) {
        String keyword = query.trim().toLowerCase(Locale.ROOT);
        visible.clear();
        for (PreviewRow row : source) {
            String content = String.join(" ", row.text.values()).toLowerCase(Locale.ROOT);
            if (content.contains(keyword)) visible.add(row);
        }
        notifyDataSetChanged();
    }
    protected void addRow(PreviewRow row) {
        source.add(row);
        visible.add(row);
        notifyItemInserted(visible.size() - 1);
    }
    protected void bindActions(View root, PreviewRow row) { }
    public static class Holder extends RecyclerView.ViewHolder { Holder(View view) { super(view); } }
}
