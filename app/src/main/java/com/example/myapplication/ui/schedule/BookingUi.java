package com.example.myapplication.ui.schedule;

import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import com.example.myapplication.R;
import com.example.myapplication.data.remote.dto.response.BookingDto;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Locale;

public final class BookingUi {
    public static final String UPDATED = "booking_updated";
    public static final String BOOKING_ID = "booking_id";
    public static final String SELECTED_DATE = "selected_date";
    public static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.US)
            .withResolverStyle(ResolverStyle.STRICT);
    static String value(String value) { return value == null ? "" : value; }
    static ZonedDateTime time(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        String input = raw.trim().replace(' ', 'T');
        try { return Instant.parse(input).atZone(ZONE); }
        catch (RuntimeException ignored) {
            try { return OffsetDateTime.parse(input).atZoneSameInstant(ZONE); }
            catch (RuntimeException offset) {
                try { return LocalDateTime.parse(input).atOffset(ZoneOffset.UTC).atZoneSameInstant(ZONE); }
                catch (RuntimeException invalid) { return null; }
            }
        }
    }
    static String status(BookingDto booking) { return value(booking.status).toUpperCase(Locale.ROOT); }
    static boolean visible(BookingDto booking) {
        String state = status(booking);
        return state.equals("PENDING") || state.equals("ACCEPTED") || state.equals("CONFIRMED") || state.equals("COMPLETED");
    }
    static int statusText(String status) {
        switch (status) {
            case "PENDING": return R.string.booking_pending;
            case "ACCEPTED": case "CONFIRMED": return R.string.booking_confirmed;
            case "COMPLETED": return R.string.booking_completed;
            case "CANCELLED": case "CANCELED": return R.string.booking_status_cancelled;
            case "REJECTED": return R.string.booking_rejected;
            default: return R.string.booking_unknown;
        }
    }
    static void updated(FragmentActivity activity) {
        activity.getSupportFragmentManager().setFragmentResult(UPDATED, new Bundle());
    }
    private BookingUi() { }
}
