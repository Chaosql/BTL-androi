package com.example.myapplication.ui.schedule;

import com.example.myapplication.databinding.FragmentScheduleBinding;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.main.UiScreen;
import com.example.myapplication.data.remote.dto.response.BookingDto;
import com.example.myapplication.data.remote.dto.response.BaseResponse;
import com.example.myapplication.data.remote.dto.response.WeeklyBookingResponse;
import com.example.myapplication.data.repository.schedule.BookingRepository;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.ui.main.MainActivity;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.time.ZonedDateTime;
import retrofit2.Call;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

public class ScheduleFragment extends BaseFragment<FragmentScheduleBinding> {
    private LocalDate selectedScheduleDate = LocalDate.now(BookingUi.ZONE);
    private ScheduleAdapter adapter;
    private final List<BookingDto> weekly = new ArrayList<>();
    private final List<BookingDto> mine = new ArrayList<>();
    private Call<?> weeklyCall;
    private Call<?> mineCall;
    private int version;
    private int pending;
    private boolean failed;

    private void updateSchedule(View root) {
        binding.tvMonthYear.setText(getString(R.string.schedule_month_year,
                selectedScheduleDate.getMonthValue(), selectedScheduleDate.getYear()));
        binding.tvDateTag.setText(selectedScheduleDate.format(DateTimeFormatter.ofPattern("dd/M")));
        int[] tabs = {R.id.tabMon, R.id.tabTue, R.id.tabWed, R.id.tabThu, R.id.tabFri, R.id.tabSat, R.id.tabSun};
        int selected = selectedScheduleDate.getDayOfWeek().getValue() - 1;
        for (int i = 0; i < tabs.length; i++) {
            View tab = root.findViewById(tabs[i]);
            tab.setSelected(i == selected);
            tab.setBackgroundResource(i == selected ? R.drawable.bg_tab_selected : R.drawable.bg_tab_unselected);
            tab.setAlpha(1f);
        }
        View tag = binding.tvDateTag;
        root.post(() -> {
            if (!isAdded() || getView() != root) return;
            View selectedTab = root.findViewById(tabs[selected]);
            View tabColumn = binding.layoutDayTabs;
            tag.setTranslationY(tabColumn.getTop() + selectedTab.getTop()
                    + (selectedTab.getHeight() - tag.getHeight()) / 2f - tag.getTop());
        });
        render();
    }

    private void render() {
        if (binding == null || adapter == null) return;
        Map<String, BookingDto> unique = new LinkedHashMap<>();
        List<BookingDto> all = new ArrayList<>(weekly);
        all.addAll(mine);
        for (BookingDto booking : all) {
            if (booking == null || !BookingUi.visible(booking)) continue;
            ZonedDateTime time = BookingUi.time(booking.scheduledAt);
            if (time == null || !time.toLocalDate().equals(selectedScheduleDate)) continue;
            String key = booking.id == null ? booking.subject + "_" + booking.scheduledAt + "_" + booking.creatorId : booking.id;
            unique.put(key, booking);
        }
        List<BookingDto> visible = new ArrayList<>(unique.values());
        visible.sort(Comparator.comparing(booking -> BookingUi.time(booking.scheduledAt).toInstant()));
        adapter.submit(visible);
        binding.progressBar.setVisibility(pending > 0 ? View.VISIBLE : View.GONE);
        binding.tvEmptyState.setText(failed ? R.string.booking_retry : R.string.booking_empty);
        binding.tvEmptyState.setVisibility(pending == 0 && (failed || visible.isEmpty()) ? View.VISIBLE : View.GONE);
    }

    private void load() {
        if (!new PreferenceManager(requireContext()).isLogin()) { render(); return; }
        if (weeklyCall != null) cancelRequest(weeklyCall);
        if (mineCall != null) cancelRequest(mineCall);
        int current = ++version;
        weekly.clear(); mine.clear(); pending = 2; failed = false; render();
        BookingRepository repository =
                new BookingRepository(requireContext());
        String monday = selectedScheduleDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString();
        Call<BaseResponse<WeeklyBookingResponse>> week = repository.getWeeklyBookings(monday);
        weeklyCall = week;
        request(week, response -> {
            if (current != version) return;
            if (response.data == null || response.data.daySchedules == null) failed = true;
            else for (List<BookingDto> day : response.data.daySchedules.values())
                if (day != null) weekly.addAll(day);
            pending--; render();
        }, error -> { if (current == version) { pending--; failed = true; render(); } });
        Call<BaseResponse<List<BookingDto>>> bookings = repository.getMyBookings();
        mineCall = bookings;
        request(bookings, response -> {
            if (current != version) return;
            if (response.data == null) failed = true;
            else mine.addAll(response.data);
            pending--; render();
        }, error -> { if (current == version) { pending--; failed = true; render(); } });
    }

    @Override public void onSaveInstanceState(@NonNull Bundle state) {
        state.putString("selected_date", selectedScheduleDate.toString());
        super.onSaveInstanceState(state);
    }

    public ScheduleFragment() { super(FragmentScheduleBinding::inflate); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        Bundle arguments = getArguments();
        if (state != null) selectedScheduleDate = LocalDate.parse(state.getString(BookingUi.SELECTED_DATE, selectedScheduleDate.toString()));
        else if (arguments != null && arguments.getString(BookingUi.SELECTED_DATE) != null)
            selectedScheduleDate = LocalDate.parse(arguments.getString(BookingUi.SELECTED_DATE));
        weekly.clear(); mine.clear(); pending = 0; failed = false;
        adapter = new ScheduleAdapter(booking -> {
            if (booking.id != null && !booking.id.isEmpty())
                new BookingDetailDialog((MainActivity) requireActivity(), booking.id).show();
        });
        binding.rvScheduleList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvScheduleList.setAdapter(adapter);
        binding.tvEmptyState.setOnClickListener(view -> load());
        getParentFragmentManager().setFragmentResultListener(BookingUi.UPDATED, getViewLifecycleOwner(), (key, data) -> {
            if (data.getString(BookingUi.SELECTED_DATE) != null) selectedScheduleDate = LocalDate.parse(data.getString(BookingUi.SELECTED_DATE));
            updateSchedule(root); load();
            openDetail(data);
        });
        int[] tabs = {R.id.tabMon, R.id.tabTue, R.id.tabWed, R.id.tabThu, R.id.tabFri, R.id.tabSat, R.id.tabSun};
        for (int index = 0; index < tabs.length; index++) {
            int day = index;
            int id = tabs[index];
            on(root, id, () -> {
                selectedScheduleDate = selectedScheduleDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusDays(day);
                updateSchedule(root);
            });
        }
        binding.btnWeekPrev.setOnClickListener(bindingView -> { selectedScheduleDate = selectedScheduleDate.minusWeeks(1); updateSchedule(root); load(); });
        binding.btnWeekNext.setOnClickListener(bindingView -> { selectedScheduleDate = selectedScheduleDate.plusWeeks(1); updateSchedule(root); load(); });
        updateSchedule(root);
        binding.imgMascot.setOnClickListener(bindingView -> {
            Bundle data = new Bundle(); data.putString(BookingUi.SELECTED_DATE, selectedScheduleDate.toString());
            go(UiScreen.CREATE_APPOINTMENT, data);
        });
        load();
        if (state == null && arguments != null) openDetail(arguments);
    }

    private void openDetail(Bundle arguments) {
        String id = arguments.getString(BookingUi.BOOKING_ID);
        if (id != null && !id.isEmpty() && new PreferenceManager(requireContext()).isLogin())
            new BookingDetailDialog((MainActivity) requireActivity(), id).show();
    }
}
