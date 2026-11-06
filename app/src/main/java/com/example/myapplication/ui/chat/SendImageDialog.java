package com.example.myapplication.ui.chat;

import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.repository.chat.MessageRepository;
import com.example.myapplication.databinding.DialogSendImageBinding;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;

public final class SendImageDialog extends BasePreviewDialog<DialogSendImageBinding> {
    private final String conversation;
    private final ChatImage image;
    private final Runnable uploaded;
    private final Runnable closed;
    private boolean busy;
    public SendImageDialog(MainActivity activity, String conversation, ChatImage image, Runnable uploaded, Runnable closed) {
        super(activity, DialogSendImageBinding::inflate);
        this.conversation = conversation; this.image = image; this.uploaded = uploaded; this.closed = closed;
    }
    @Override protected void bind(View content, AlertDialog dialog) {
        Glide.with(activity).load(image.previewBytes()).fitCenter().into(binding.ivPreview);
        binding.btnSendImage.setOnClickListener(view -> {
            if (busy || !image.file().isFile()) return;
            busy = true;
            binding.btnSendImage.setEnabled(false); binding.btnCancel.setEnabled(false);
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.tvStatus.setText(R.string.media_uploading);
            binding.tvStatus.setTextColor(activity.getColor(R.color.brown));
            binding.tvStatus.setVisibility(View.VISIBLE);
            dialog.setCancelable(false);
            request(new MessageRepository(activity).uploadImage(conversation, image.part()), response -> {
                ChatUi.updated(activity);
                Toast.makeText(activity, R.string.media_uploaded, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                uploaded.run();
            }, error -> {
                busy = false;
                binding.btnSendImage.setEnabled(true); binding.btnCancel.setEnabled(true);
                binding.btnSendImage.setText(R.string.media_retry);
                binding.progressBar.setVisibility(View.GONE);
                binding.tvStatus.setTextColor(activity.getColor(R.color.error));
                binding.tvStatus.setText(error.code == 0 ? activity.getString(R.string.media_uncertain) : error.message);
                dialog.setCancelable(true);
            });
        });
    }
    @Override protected void onDismissed() {
        Glide.with(activity.getApplicationContext()).clear(binding.ivPreview);
        image.close();
        closed.run();
    }
}
