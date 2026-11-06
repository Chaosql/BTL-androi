package com.example.myapplication.ui.chat.component;

import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.data.remote.dto.response.MessageResponse;
import com.example.myapplication.databinding.LayoutReactionPopupBinding;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import java.util.function.BiConsumer;

public class MessageActionsDialog extends BasePreviewDialog<LayoutReactionPopupBinding> {
    private final MessageResponse message;
    private final String self;
    private final BiConsumer<String, String> action;
    public MessageActionsDialog(MainActivity activity) { this(activity, null, "", (name, value) -> { }); }
    public MessageActionsDialog(MainActivity activity, MessageResponse message, String self, BiConsumer<String, String> action) {
        super(activity, LayoutReactionPopupBinding::inflate); this.message = message; this.self = self; this.action = action;
    }
    @Override protected void bind(View content, AlertDialog dialog) {
        boolean available = message != null && message.id != null;
        boolean recalled = available && Boolean.TRUE.equals(message.isRecalled);
        binding.tvReply.setEnabled(available && !recalled);
        binding.tvForward.setEnabled(available && !recalled);
        binding.tvDelete.setEnabled(available);
        binding.tvRecall.setVisibility(available && !recalled && self.equals(message.senderId) ? View.VISIBLE : View.GONE);
        binding.layoutEmojiRow.setVisibility(available && !recalled ? View.VISIBLE : View.GONE);
        binding.tvReply.setOnClickListener(view -> { if (available && !recalled) { dialog.dismiss(); action.accept("reply", ""); } });
        binding.tvForward.setOnClickListener(view -> { if (available && !recalled) { dialog.dismiss(); action.accept("forward", ""); } });
        binding.tvRecall.setOnClickListener(view -> {
            if (available && !recalled && self.equals(message.senderId)) { dialog.dismiss(); action.accept("recall", ""); }
        });
        binding.tvDelete.setOnClickListener(view -> { if (available) { dialog.dismiss(); action.accept("delete", ""); } });
        android.widget.TextView[] emoji = {binding.tvEmojiLike, binding.tvEmojiHeart, binding.tvEmojiLaugh, binding.tvEmojiWow, binding.tvEmojiSad, binding.tvEmojiPray};
        for (android.widget.TextView label : emoji) label.setOnClickListener(view -> {
            if (!available || recalled) return;
            String value = label.getText().toString(); dialog.dismiss(); action.accept("reaction", value);
        });
    }
}
