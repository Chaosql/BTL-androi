package com.example.myapplication.ui.schedule;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.response.*;
import com.example.myapplication.data.repository.schedule.BookingRepository;
import com.example.myapplication.ui.base.*;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public final class ScheduleViewModel extends BaseViewModel {
    public static final class State {
        public final LocalDate date;
        public final List<BookingDto> bookings;
        public final boolean loading, failed;
        State(LocalDate date, List<BookingDto> bookings, boolean loading, boolean failed) {
            this.date = date; this.bookings = Collections.unmodifiableList(new ArrayList<>(bookings));
            this.loading = loading; this.failed = failed;
        }
    }
    private final BookingRepository repository;
    private final MutableLiveData<State> state = new MutableLiveData<>();
    private final List<BookingDto> weekly = new ArrayList<>(), mine = new ArrayList<>();
    private LocalDate date = LocalDate.now(BookingUi.ZONE);
    private UiOperation<?> weekCall, mineCall;
    private int pending, version;
    private boolean failed, initialized;
    public ScheduleViewModel(Application application) { super(application); repository = new BookingRepository(application); }
    public LiveData<State> state() { return state; }
    public LocalDate date() { return date; }
    public void initialize(LocalDate initial) {
        if (initialized) { publish(); return; }
        initialized = true; date = initial; refresh();
    }
    public void select(LocalDate selected) {
        boolean weekChanged = !monday(date).equals(monday(selected)); date = selected;
        if (weekChanged) refresh(); else publish();
    }
    public void selectDay(int day) { select(monday(date).plusDays(day)); }
    public void moveWeek(int weeks) { select(date.plusWeeks(weeks)); }
    private LocalDate monday(LocalDate value) { return value.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)); }
    public void refresh() {
        if (weekCall != null) weekCall.cancel();
        if (mineCall != null) mineCall.cancel();
        int current = ++version;
        weekly.clear(); mine.clear(); failed = false; pending = 0;
        if (!new PreferenceManager(getApplication()).isLogin()) { publish(); return; }
        pending = 2; publish();
        UiOperation<BaseResponse<WeeklyBookingResponse>> week = execute(repository.getWeeklyBookings(monday(date).toString()));
        weekCall = week;
        watch(week, response -> {
            if (current != version) return;
            if (response.data == null || response.data.daySchedules == null) failed = true;
            else for (List<BookingDto> day : response.data.daySchedules.values()) if (day != null) weekly.addAll(day);
            pending--; publish();
        }, error -> { if (current == version) { pending--; failed = true; publish(); } });
        UiOperation<BaseResponse<List<BookingDto>>> bookings = execute(repository.getMyBookings()); mineCall = bookings;
        watch(bookings, response -> {
            if (current != version) return;
            if (response.data == null) failed = true; else mine.addAll(response.data);
            pending--; publish();
        }, error -> { if (current == version) { pending--; failed = true; publish(); } });
    }
    private void publish() {
        Map<String, BookingDto> unique = new LinkedHashMap<>();
        List<BookingDto> all = new ArrayList<>(weekly); all.addAll(mine);
        for (BookingDto booking : all) {
            BookingReminders.sync(getApplication(), booking);
            if (booking == null || !BookingUi.visible(booking)) continue;
            ZonedDateTime time = BookingUi.time(booking.scheduledAt);
            if (time == null || !date.equals(time.toLocalDate())) continue;
            String key = booking.id == null ? booking.subject + "_" + booking.scheduledAt + "_" + booking.creatorId : booking.id;
            unique.put(key, booking);
        }
        List<BookingDto> visible = new ArrayList<>(unique.values());
        visible.sort(Comparator.comparing(booking -> BookingUi.time(booking.scheduledAt).toInstant()));
        state.setValue(new State(date, visible, pending > 0, failed));
    }
}
