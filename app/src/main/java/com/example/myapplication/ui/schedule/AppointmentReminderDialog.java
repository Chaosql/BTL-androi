package com.example.myapplication.ui.schedule;

import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.data.remote.dto.response.BookingDto;
import com.example.myapplication.databinding.DialogAppointmentReminderBinding;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class AppointmentReminderDialog extends BasePreviewDialog<DialogAppointmentReminderBinding> {
    private final BookingDto booking;
    public AppointmentReminderDialog(MainActivity activity) { this(activity, null); }
    public AppointmentReminderDialog(MainActivity activity, BookingDto booking) {
        super(activity, DialogAppointmentReminderBinding::inflate); this.booking = booking;
    }
    @Override protected void bind(View content, AlertDialog dialog) {
        binding.btnClose.setOnClickListener(view -> dialog.dismiss());
        binding.btnViewSchedule.setOnClickListener(view -> { dialog.dismiss(); activity.showScreen(com.example.myapplication.ui.main.UiScreen.SCHEDULE); });
        if (booking == null) return;
        binding.tvSubject.setText(BookingUi.value(booking.subject));
        ZonedDateTime time = BookingUi.time(booking.scheduledAt);
        binding.tvTime.setText(time == null ? "—" : time.format(DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy")));
        binding.tvLocation.setText(BookingUi.value(booking.locationName));
    }
}
