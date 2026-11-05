package com.example.myapplication.ui.schedule;

import com.example.myapplication.R;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import java.time.LocalDate;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.main.UiDialogs;

public class BookingDetailDialog extends BasePreviewDialog {
    public BookingDetailDialog(MainActivity activity) { super(activity, R.layout.dialog_booking_detail); }

    @Override protected void bind(View content, AlertDialog dialog) {

        BaseFragment.text(content, R.id.tvTitle, activity.getString(R.string.ui_uidialogs_text_18));
        BaseFragment.text(content, R.id.tvSubject, activity.getString(R.string.ui_uidialogs_text_19));
        BaseFragment.text(content, R.id.tvPartnerName, "Minh Anh");
        BaseFragment.text(content, R.id.tvTimeAndDate, activity.getString(R.string.ui_uidialogs_text_20)
                + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        BaseFragment.text(content, R.id.tvStatus, activity.getString(R.string.ui_uidialogs_text_21));
        BaseFragment.text(content, R.id.tvLocation, activity.getString(R.string.ui_uidialogs_text_22));
        BaseFragment.text(content, R.id.tvNote, activity.getString(R.string.ui_uidialogs_text_23));
        BaseFragment.on(content, R.id.btnCancel, () -> { dialog.dismiss(); UiDialogs.show(activity, R.layout.dialog_cancel_reason); });
        View rating = content.findViewById(R.id.layoutRatingSection);
        if (rating != null) rating.setVisibility(View.GONE);
        content.findViewById(R.id.btnComplete).setVisibility(View.VISIBLE);
        content.findViewById(R.id.btnCancel).setVisibility(View.VISIBLE);
    }
}
