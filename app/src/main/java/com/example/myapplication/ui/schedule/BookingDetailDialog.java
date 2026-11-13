package com.example.myapplication.ui.schedule;

import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.request.RateBookingRequest;
import com.example.myapplication.data.remote.dto.response.BookingDto;
import com.example.myapplication.data.remote.dto.response.BaseResponse;
import com.example.myapplication.ui.schedule.BookingViewModel;
import com.example.myapplication.databinding.DialogBookingDetailBinding;
import com.example.myapplication.ui.base.BasePreviewDialog;
import com.example.myapplication.ui.main.MainActivity;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;
import com.example.myapplication.ui.base.UiOperation;

public class BookingDetailDialog extends BasePreviewDialog<DialogBookingDetailBinding> {
    private final String bookingId;
    private final BookingViewModel model;
    private final String userId;
    private BookingDto booking;
    private String verifiedOwner;
    private boolean busy;
    public BookingDetailDialog(MainActivity activity) { this(activity, null); }
    public BookingDetailDialog(MainActivity activity, String id) {
        super(activity, DialogBookingDetailBinding::inflate);
        bookingId = id; model = viewModel(com.example.myapplication.ui.schedule.BookingViewModel.class);
        userId = new PreferenceManager(activity).getUserId();
    }
    @Override protected void bind(View content, AlertDialog dialog) {
        binding.btnCancel.setOnClickListener(view -> dialog.dismiss());
        binding.ivClose.setOnClickListener(view -> dialog.dismiss());
        hideActions();
        binding.btnAccept.setOnClickListener(view -> update(view, () -> model.acceptBooking(bookingId), dialog));
        binding.btnReject.setOnClickListener(view -> update(view, () -> model.rejectBooking(bookingId), dialog));
        binding.btnComplete.setOnClickListener(view -> update(view, () -> model.completeBooking(bookingId), dialog));
        binding.btnCancel.setOnClickListener(view -> {
            if (busy || booking == null || !member() || binding.btnCancel.getVisibility() != View.VISIBLE) return;
            busy = true; buttons();
            new CancelBookingDialog(activity, bookingId, () -> {
                if (binding != null) dialog.dismiss();
            }, () -> {
                if (binding != null) { busy = false; buttons(); }
            }).show();
        });
        binding.btnRate.setOnClickListener(view -> rate(dialog));
        binding.tvErrorMessage.setOnClickListener(view -> { if (!busy) load(); });
        if (bookingId != null && !bookingId.isEmpty() && !userId.isEmpty()) load();
    }

    private void hideActions() {
        binding.btnAccept.setVisibility(View.GONE); binding.btnReject.setVisibility(View.GONE);
        binding.btnComplete.setVisibility(View.GONE); binding.btnCancel.setVisibility(View.GONE);
        binding.layoutRatingSection.setVisibility(View.GONE);
    }
    private void load() {
        verifiedOwner = null;
        busy = true; hideActions();
        binding.tvErrorMessage.setTextColor(activity.getColor(R.color.brown));
        binding.tvErrorMessage.setText(R.string.booking_loading);
        binding.tvErrorMessage.setVisibility(View.VISIBLE);
        request(model.getBookingDetail(bookingId), response -> {
            busy = false;
            if (response.data == null || !bookingId.equals(response.data.id)) {
                error(activity.getString(R.string.booking_invalid_response)); return;
            }
            if (!sessionValid()) { error(activity.getString(R.string.api_invalid_login)); return; }
            booking = response.data;
            verifiedOwner = userId;
            binding.tvErrorMessage.setVisibility(View.GONE);
            render();
        }, failure -> { busy = false; error(failure.message); });
    }
    private boolean sessionValid() {
        PreferenceManager session = new PreferenceManager(activity);
        return session.isLogin() && userId.equals(session.getUserId());
    }
    private boolean creator() { return sessionValid() && model.creator(booking); }
    private boolean member() { return sessionValid() && (model.member(booking) || model.verifiedMember(booking, verifiedOwner)); }
    private void render() {
        BookingReminders.sync(activity, booking);
        binding.tvSubject.setText(BookingUi.value(booking.subject));
        binding.tvStatus.setText(BookingUi.statusText(BookingUi.status(booking)));
        ZonedDateTime time = BookingUi.time(booking.scheduledAt);
        binding.tvTimeAndDate.setText(activity.getString(R.string.booking_time_detail,
                time == null ? "—" : time.format(DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy")),
                booking.durationMinutes == null ? 30 : booking.durationMinutes));
        String location = String.join(" ", BookingUi.value(booking.locationName), BookingUi.value(booking.locationAddress),
                BookingUi.value(booking.locationDistrict), BookingUi.value(booking.locationCity)).trim();
        binding.tvLocation.setText(activity.getString(R.string.booking_location_detail, location));
        binding.tvNote.setText(booking.cancelReason == null || booking.cancelReason.isEmpty()
                ? activity.getString(R.string.booking_note_detail, BookingUi.value(booking.note))
                : activity.getString(R.string.booking_cancel_detail, booking.cancelReason));
        Glide.with(activity).load(creator() ? booking.partnerAvatar : booking.creatorAvatar)
                .placeholder(R.drawable.avatar).error(R.drawable.avatar).into(binding.imgPartnerAvatar);
        hideActions();
        BookingViewModel.Actions actions = sessionValid() ? model.actions(booking, verifiedOwner) : model.actions(null);
        binding.btnAccept.setVisibility(actions.accept ? View.VISIBLE : View.GONE);
        binding.btnReject.setVisibility(actions.reject ? View.VISIBLE : View.GONE);
        binding.btnCancel.setVisibility(actions.cancel ? View.VISIBLE : View.GONE);
        binding.btnComplete.setVisibility(actions.complete ? View.VISIBLE : View.GONE);
        binding.layoutRatingSection.setVisibility(actions.rating ? View.VISIBLE : View.GONE);
        if (actions.rating) {
            binding.ratingBar.setIsIndicator(actions.rated);
            binding.ratingBar.setRating(actions.rated ? booking.myRating == null ? 5 : booking.myRating : 5);
            binding.btnRate.setVisibility(actions.rated ? View.GONE : View.VISIBLE);
        }
        buttons();

    }
    private void buttons() {
        binding.btnAccept.setEnabled(!busy); binding.btnReject.setEnabled(!busy);
        binding.btnComplete.setEnabled(!busy); binding.btnCancel.setEnabled(!busy);
        binding.btnRate.setEnabled(!busy); binding.ratingBar.setEnabled(!busy);
    }
    private void update(View action, Supplier<UiOperation<BaseResponse<BookingDto>>> call, AlertDialog dialog) {
        if (busy || booking == null || !member() || action.getVisibility() != View.VISIBLE) return;
        if (action.getId() == R.id.btnAccept) activity.enableNotifications();
        busy = true; buttons(); dialog.setCancelable(false); binding.ivClose.setEnabled(false);
        binding.tvErrorMessage.setVisibility(View.GONE);
        request(call.get(), response -> {
            busy = false; dialog.setCancelable(true); binding.ivClose.setEnabled(true);
            BookingUi.updated(activity);
            if (response.data != null && bookingId.equals(response.data.id)) { booking = response.data; render(); }
            else load();
            Toast.makeText(activity, R.string.booking_updated, Toast.LENGTH_SHORT).show();
        }, failure -> {
            busy = false; dialog.setCancelable(true); binding.ivClose.setEnabled(true); buttons(); error(failure.message);
        });
    }
    private void rate(AlertDialog dialog) {
        if (busy || booking == null || !member() || !"COMPLETED".equals(BookingUi.status(booking))
                || Boolean.TRUE.equals(booking.hasRated) || booking.myRating != null) return;
        float selected = binding.ratingBar.getRating();
        int score = Math.round(selected);
        if (score < 1 || score > 5 || Math.abs(selected - score) > 0.01f) {
            error(activity.getString(R.string.booking_invalid_rating)); return;
        }
        busy = true; buttons(); dialog.setCancelable(false); binding.ivClose.setEnabled(false);
        request(model.rateBooking(bookingId, new RateBookingRequest(score)), response -> {
            busy = false; dialog.setCancelable(true); binding.ivClose.setEnabled(true);
            booking.hasRated = true; booking.myRating = score;
            binding.tvErrorMessage.setVisibility(View.GONE); render();
            BookingUi.updated(activity);
            Toast.makeText(activity, R.string.booking_rated, Toast.LENGTH_SHORT).show();
        }, failure -> {
            busy = false; dialog.setCancelable(true); binding.ivClose.setEnabled(true); buttons(); error(failure.message);
        });
    }
    private void error(String message) {
        binding.tvErrorMessage.setTextColor(activity.getColor(R.color.error));
        binding.tvErrorMessage.setText(message); binding.tvErrorMessage.setVisibility(View.VISIBLE);
    }
}
