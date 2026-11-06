package com.example.myapplication.ui.chat;

import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.myapplication.databinding.DialogViewImageBinding;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;

public final class ImageViewerDialog extends BasePreviewDialog<DialogViewImageBinding> {
    private final String url;
    public ImageViewerDialog(MainActivity activity, String url) { super(activity, DialogViewImageBinding::inflate); this.url = url; }
    @Override protected void bind(View content, AlertDialog dialog) {
        binding.ivImage.setOnClickListener(view -> load());
        load();
    }
    private void load() {
        binding.tvImageError.setVisibility(View.GONE);
        if (url == null) { binding.tvImageError.setVisibility(View.VISIBLE); return; }
        DialogViewImageBinding current = binding;
        Glide.with(activity).load(url).fitCenter().listener(new RequestListener<Drawable>() {
            @Override public boolean onLoadFailed(@Nullable GlideException error, Object model, Target<Drawable> target, boolean first) {
                if (binding == current) binding.tvImageError.setVisibility(View.VISIBLE);
                return false;
            }
            @Override public boolean onResourceReady(Drawable drawable, Object model, Target<Drawable> target, DataSource source, boolean first) {
                return false;
            }
        }).into(binding.ivImage);
    }
    @Override protected void onDismissed() { Glide.with(activity.getApplicationContext()).clear(binding.ivImage); }
}
