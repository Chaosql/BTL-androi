package com.example.myapplication.data.repository.schedule;

import android.content.Context;
import com.example.myapplication.data.repository.BaseRepository;
import com.example.myapplication.data.remote.api.ApiService;
import com.example.myapplication.data.remote.dto.request.*;
import com.example.myapplication.data.remote.dto.response.*;
import java.util.List;
import okhttp3.MultipartBody;
import retrofit2.Call;

public final class BookingRepository extends BaseRepository {
    public BookingRepository(Context context) { super(context); }
    public BookingRepository(ApiService api) { super(api); }

    public Call<BaseResponse<BookingDto>> createBookingInConversation(String conversationId, CreateBookingRequest request) {
        return api.createBookingInConversation(conversationId, request);
    }

    public Call<BaseResponse<List<BookingDto>>> getMyBookings() {
        return api.getMyBookings();
    }

    public Call<BaseResponse<WeeklyBookingResponse>> getWeeklyBookings(String weekStart) {
        return api.getWeeklyBookings(weekStart);
    }

    public Call<BaseResponse<BookingDto>> getBookingDetail(String bookingId) {
        return api.getBookingDetail(bookingId);
    }

    public Call<BaseResponse<BookingDto>> acceptBooking(String bookingId) {
        return api.acceptBooking(bookingId);
    }

    public Call<BaseResponse<BookingDto>> rejectBooking(String bookingId) {
        return api.rejectBooking(bookingId);
    }

    public Call<BaseResponse<BookingDto>> completeBooking(String bookingId) {
        return api.completeBooking(bookingId);
    }

    public Call<BaseResponse<BookingDto>> cancelBooking(String bookingId, CancelBookingRequest request) {
        return api.cancelBooking(bookingId, request);
    }

    public Call<BaseResponse<ActionStatusDto>> rateBooking(String bookingId, RateBookingRequest request) {
        return api.rateBooking(bookingId, request);
    }
}
