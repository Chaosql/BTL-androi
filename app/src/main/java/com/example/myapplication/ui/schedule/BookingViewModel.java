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
        return execute(repository.completeBooking(bookingId), response -> verifiedUpdate(response, bookingId, "COMPLETED"));
    }

    public UiOperation<BaseResponse<BookingDto>> cancelBooking(String bookingId, CancelBookingRequest request) {
        return execute(repository.cancelBooking(bookingId, request), response -> verifiedUpdate(response, bookingId, "CANCELLED"));
    }

    private BaseResponse<BookingDto> verifiedUpdate(BaseResponse<BookingDto> response, String id, String status) {
        if (response.data == null || !id.equals(response.data.id) || !status.equals(BookingUi.status(response.data)))
            throw new IllegalArgumentException(getApplication().getString(com.example.myapplication.R.string.booking_update_unconfirmed));
        return response;
    }

    public UiOperation<BaseResponse<ActionStatusDto>> rateBooking(String bookingId, RateBookingRequest request) {
        return execute(repository.rateBooking(bookingId, request));
    }
    public boolean creator(BookingDto booking) {
        String id = new com.example.myapplication.data.local.PreferenceManager(getApplication()).getUserId();
        return booking != null && !id.isEmpty() && (id.equals(booking.creatorId)
                || ((booking.creatorId == null || booking.creatorId.trim().isEmpty()) && "CREATOR".equals(role(booking))));
    }
    public boolean member(BookingDto booking) {
        if (booking == null) return false;
        String id = new com.example.myapplication.data.local.PreferenceManager(getApplication()).getUserId();
        if (id.isEmpty()) return false;
        if (creator(booking)) return true;
        String role = role(booking);
        if ("NOT_INVOLVED".equals(role)) return false;
        if (booking.participants != null) for (BookingParticipantDto participant : booking.participants)
            if (participant != null && id.equals(participant.userId)) return true;
        return id.equals(booking.partnerId) || "PENDING".equals(role) || "ACCEPTED".equals(role) || "REJECTED".equals(role);
    }
    public boolean verifiedMember(BookingDto booking, String verifiedOwner) {
        com.example.myapplication.data.local.PreferenceManager session = new com.example.myapplication.data.local.PreferenceManager(getApplication());
        return booking != null && verifiedOwner != null && session.isLogin() && verifiedOwner.equals(session.getUserId())
                && !"NOT_INVOLVED".equals(role(booking));
    }
    private static String role(BookingDto booking) {
        return booking.participantStatus == null ? "" : booking.participantStatus.trim().toUpperCase(java.util.Locale.ROOT);
    }
    public static final class Actions {
        public final boolean accept, reject, cancel, complete, rating, rated;
        Actions(boolean accept, boolean reject, boolean cancel, boolean complete, boolean rating, boolean rated) {
            this.accept = accept; this.reject = reject; this.cancel = cancel; this.complete = complete; this.rating = rating; this.rated = rated;
        }
    }
    public Actions actions(BookingDto booking) { return actions(booking, null); }
    public Actions actions(BookingDto booking, String verifiedOwner) {
        boolean knownMember = member(booking);
        boolean member = knownMember || verifiedMember(booking, verifiedOwner), creator = creator(booking);
        String status = booking == null ? "" : BookingUi.status(booking);
        boolean pending = member && "PENDING".equals(status);
        boolean reply = pending && knownMember && !creator && !"ACCEPTED".equals(role(booking)) && !"REJECTED".equals(role(booking));
        boolean confirmed = member && ("CONFIRMED".equals(status) || "ACCEPTED".equals(status));
        boolean rating = member && "COMPLETED".equals(status);
        return new Actions(reply, reply, confirmed || pending, confirmed, rating,
                booking != null && (Boolean.TRUE.equals(booking.hasRated) || booking.myRating != null));
    }
    public static boolean validTime(java.time.LocalDateTime selected) {
        return !selected.isBefore(java.time.LocalDateTime.now(BookingUi.ZONE).plusMinutes(30));
    }
}
