package com.example.myapplication.ui.chat.component;

import com.example.myapplication.databinding.LayoutReactionPopupBinding;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import android.view.View;
import androidx.appcompat.app.AlertDialog;

public class MessageActionsDialog extends BasePreviewDialog<LayoutReactionPopupBinding> {
    public MessageActionsDialog(MainActivity activity) { super(activity, LayoutReactionPopupBinding::inflate); }

    @Override protected void bind(View content, AlertDialog dialog) {

    }
}
