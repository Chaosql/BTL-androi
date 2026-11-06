package com.example.myapplication.ui.chat.component;

import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.data.remote.dto.response.MessageResponse;
import com.example.myapplication.databinding.LayoutMessageActionsBinding;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import java.util.function.Consumer;

public class MessageActionsDialog extends BasePreviewDialog<LayoutMessageActionsBinding> {
    private final MessageResponse message;
    private final String self;
    private final Consumer<String> action;
    public MessageActionsDialog(MainActivity activity) { this(activity, null, "", name -> { }); }
    public MessageActionsDialog(MainActivity activity, MessageResponse message, String self, Consumer<String> action) {
        super(activity, LayoutMessageActionsBinding::inflate); this.message = message; this.self = self; this.action = action;
    }
    @Override protected void bind(View content, AlertDialog dialog) {
        boolean available = message != null && message.id != null;
        boolean recalled = available && Boolean.TRUE.equals(message.isRecalled);
        binding.tvDelete.setEnabled(available);
        binding.tvRecall.setVisibility(available && !recalled && self.equals(message.senderId) ? View.VISIBLE : View.GONE);
        binding.tvRecall.setOnClickListener(view -> {
            if (available && !recalled && self.equals(message.senderId)) { dialog.dismiss(); action.accept("recall"); }
        });
        binding.tvDelete.setOnClickListener(view -> { if (available) { dialog.dismiss(); action.accept("delete"); } });
    }
}
