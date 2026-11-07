package com.example.myapplication.ui.schedule;

import android.app.Application;
import com.example.myapplication.ui.base.BaseViewModel;
import com.example.myapplication.ui.base.UiOperation;
import com.example.myapplication.data.repository.schedule.BookingRepository;
import com.example.myapplication.data.remote.dto.request.*;
import com.example.myapplication.data.remote.dto.response.*;
import java.util.List;
import okhttp3.MultipartBody;

public final class BookingViewModel extends BaseViewModel {
    private final BookingRepository repository;

    public BookingViewModel(Application application) {
        super(application);
        repository = new BookingRepository(application);
    }

    public UiOperation<BaseResponse<BookingDto>> createBookingInConversation(String conversationId, CreateBookingRequest request) {
        return execute(repository.createBookingInConversation(conversationId, request));
    }

    public UiOperation<BaseResponse<List<BookingDto>>> getMyBookings() {
        return execute(repository.getMyBookings());
    }

    public UiOperation<BaseResponse<WeeklyBookingResponse>> getWeeklyBookings(String weekStart) {
        return execute(repository.getWeeklyBookings(weekStart));
    }

    public UiOperation<BaseResponse<BookingDto>> getBookingDetail(String bookingId) {
        return execute(repository.getBookingDetail(bookingId));
    }

    public UiOperation<BaseResponse<BookingDto>> acceptBooking(String bookingId) {
        return execute(repository.acceptBooking(bookingId));
    }

    public UiOperation<BaseResponse<BookingDto>> rejectBooking(String bookingId) {
        return execute(repository.rejectBooking(bookingId));
    }

    public UiOperation<BaseResponse<BookingDto>> completeBooking(String bookingId) {
        return execute(repository.completeBooking(bookingId));
    }

    public UiOperation<BaseResponse<BookingDto>> cancelBooking(String bookingId, CancelBookingRequest request) {
        return execute(repository.cancelBooking(bookingId, request));
    }

    public UiOperation<BaseResponse<ActionStatusDto>> rateBooking(String bookingId, RateBookingRequest request) {
        return execute(repository.rateBooking(bookingId, request));
    }
    public boolean creator(BookingDto booking) {
        String id = new com.example.myapplication.data.local.PreferenceManager(getApplication()).getUserId();
        return booking != null && !id.isEmpty() && id.equals(booking.creatorId);
    }
    public boolean member(BookingDto booking) {
        if (booking == null) return false;
        String id = new com.example.myapplication.data.local.PreferenceManager(getApplication()).getUserId();
        if (id.isEmpty()) return false;
        if (creator(booking) || id.equals(booking.partnerId)) return true;
        if (booking.participants != null) for (BookingParticipantDto participant : booking.participants)
            if (participant != null && id.equals(participant.userId)) return true;
        return false;
    }
    public static final class Actions {
        public final boolean accept, reject, cancel, complete, rating, rated;
        Actions(boolean accept, boolean reject, boolean cancel, boolean complete, boolean rating, boolean rated) {
            this.accept = accept; this.reject = reject; this.cancel = cancel; this.complete = complete; this.rating = rating; this.rated = rated;
        }
    }
    public Actions actions(BookingDto booking) {
        boolean member = member(booking), creator = creator(booking);
        String status = booking == null ? "" : BookingUi.status(booking);
        boolean pending = member && "PENDING".equals(status);
        boolean reply = pending && !creator && !"ACCEPTED".equals(booking.participantStatus) && !"REJECTED".equals(booking.participantStatus);
        boolean confirmed = member && ("CONFIRMED".equals(status) || "ACCEPTED".equals(status));
        boolean rating = member && "COMPLETED".equals(status);
        return new Actions(reply, reply, confirmed || pending && creator, confirmed, rating,
                booking != null && (Boolean.TRUE.equals(booking.hasRated) || booking.myRating != null));
    }
    public static boolean validTime(java.time.LocalDateTime selected) {
        return !selected.isBefore(java.time.LocalDateTime.now(BookingUi.ZONE).plusMinutes(30));
    }
}
