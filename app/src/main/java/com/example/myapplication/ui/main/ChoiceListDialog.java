package com.example.myapplication.ui.main;

import android.view.View;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.R;
import com.example.myapplication.databinding.DialogChoiceListBinding;
import com.example.myapplication.ui.base.BasePreviewDialog;
import java.util.function.IntConsumer;

final class ChoiceListDialog extends BasePreviewDialog<DialogChoiceListBinding> {
    private final int title;
    private final String[] labels;
    private final IntConsumer selected;
    ChoiceListDialog(MainActivity activity, int title, String[] labels, IntConsumer selected) {
        super(activity, DialogChoiceListBinding::inflate);
        this.title = title; this.labels = labels.clone(); this.selected = selected;
    }
    @Override protected void bind(View content, AlertDialog dialog) {
        binding.tvChoiceTitle.setText(title);
        binding.listChoices.setAdapter(new ArrayAdapter<>(activity, R.layout.item_dialog_choice, labels));
        binding.listChoices.setOnItemClickListener((parent, view, position, id) -> {
            dialog.dismiss(); selected.accept(position);
        });
        binding.btnCloseChoices.setOnClickListener(view -> dialog.dismiss());
    }
}
