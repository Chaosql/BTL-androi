package com.example.myapplication.ui.schedule;

import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.R;
import com.example.myapplication.data.remote.dto.request.CancelBookingRequest;
import com.example.myapplication.data.repository.schedule.BookingRepository;
import com.example.myapplication.databinding.DialogCancelReasonBinding;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;

public class CancelBookingDialog extends BasePreviewDialog<DialogCancelReasonBinding> {
    private final String bookingId;
    private final Runnable cancelled;
    private final Runnable closed;
    private boolean busy;
    public CancelBookingDialog(MainActivity activity) { this(activity, null, () -> { }, () -> { }); }
    public CancelBookingDialog(MainActivity activity, String id, Runnable cancelled, Runnable closed) {
        super(activity, DialogCancelReasonBinding::inflate); bookingId = id; this.cancelled = cancelled; this.closed = closed;
    }
    @Override protected void bind(View content, AlertDialog dialog) {
        binding.btnConfirmCancel.setEnabled(bookingId != null && !bookingId.isEmpty());
        binding.btnCancel.setOnClickListener(view -> { if (!busy) { dialog.dismiss(); closed.run(); } });
        dialog.setOnCancelListener(cancel -> closed.run());
        binding.btnConfirmCancel.setOnClickListener(view -> {
            if (busy || bookingId == null || bookingId.isEmpty()) return;
            busy = true;
            binding.btnConfirmCancel.setEnabled(false); binding.btnCancel.setEnabled(false);
            binding.etCancelReason.setEnabled(false); dialog.setCancelable(false);
            String reason = binding.etCancelReason.getText().toString().trim();
            request(new BookingRepository(activity).cancelBooking(bookingId, new CancelBookingRequest(reason)), response -> {
                BookingUi.updated(activity);
                Toast.makeText(activity, R.string.booking_updated, Toast.LENGTH_SHORT).show();
                dialog.dismiss(); cancelled.run();
            }, failure -> {
                busy = false;
                binding.btnConfirmCancel.setEnabled(true); binding.btnCancel.setEnabled(true);
                binding.etCancelReason.setEnabled(true); dialog.setCancelable(true);
                binding.etCancelReason.setError(failure.message);
            });
        });
    }
}
