package com.example.myapplication.ui.schedule;

import com.example.myapplication.databinding.DialogBookingDetailBinding;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import java.time.LocalDate;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.main.UiDialogs;

public class BookingDetailDialog extends BasePreviewDialog<DialogBookingDetailBinding> {
    public BookingDetailDialog(MainActivity activity) { super(activity, DialogBookingDetailBinding::inflate); }

    @Override protected void bind(View content, AlertDialog dialog) {

        binding.tvTitle.setText(activity.getString(R.string.ui_uidialogs_text_18));
        binding.tvSubject.setText(activity.getString(R.string.ui_uidialogs_text_19));
        binding.tvPartnerName.setText("Minh Anh");
        binding.tvTimeAndDate.setText(activity.getString(R.string.ui_uidialogs_text_20)
                + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        binding.tvStatus.setText(activity.getString(R.string.ui_uidialogs_text_21));
        binding.tvLocation.setText(activity.getString(R.string.ui_uidialogs_text_22));
        binding.tvNote.setText(activity.getString(R.string.ui_uidialogs_text_23));
        binding.btnCancel.setOnClickListener(bindingView -> { dialog.dismiss(); UiDialogs.show(activity, R.layout.dialog_cancel_reason); });
        View rating = content.findViewById(R.id.layoutRatingSection);
        if (rating != null) rating.setVisibility(View.GONE);
        content.findViewById(R.id.btnComplete).setVisibility(View.VISIBLE);
        content.findViewById(R.id.btnCancel).setVisibility(View.VISIBLE);
    }
}
