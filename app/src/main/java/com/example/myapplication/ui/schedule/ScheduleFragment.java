package com.example.myapplication.ui.schedule;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import com.example.myapplication.R;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.databinding.FragmentScheduleBinding;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.main.MainActivity;
import com.example.myapplication.ui.main.UiScreen;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ScheduleFragment extends BaseFragment<FragmentScheduleBinding> {
    private ScheduleViewModel model;
    private ScheduleAdapter adapter;
    public ScheduleFragment() { super(FragmentScheduleBinding::inflate); }
    @Override protected void onScreenCreated(View root, Bundle saved) {
        model = viewModel(ScheduleViewModel.class);
        binding.btnBack.setOnClickListener(view -> back());
        adapter = new ScheduleAdapter(booking -> {
            if (booking.id != null && !booking.id.isEmpty()) new BookingDetailDialog((MainActivity) requireActivity(), booking.id).show();
        });

        binding.rvScheduleList.setAdapter(adapter);
        binding.tvEmptyState.setOnClickListener(view -> model.refresh());
        View[] tabs = {binding.tabMon, binding.tabTue, binding.tabWed, binding.tabThu, binding.tabFri, binding.tabSat, binding.tabSun};
        for (int i = 0; i < tabs.length; i++) {
            int day = i; tabs[i].setOnClickListener(view -> model.selectDay(day));
        }
        binding.btnWeekPrev.setOnClickListener(view -> model.moveWeek(-1));
        binding.btnWeekNext.setOnClickListener(view -> model.moveWeek(1));
        binding.imgMascot.setOnClickListener(view -> {
            Bundle args = new Bundle(); args.putString(BookingUi.SELECTED_DATE, model.date().toString());
            go(UiScreen.CREATE_APPOINTMENT, args);
        });
        getParentFragmentManager().setFragmentResultListener(BookingUi.UPDATED, getViewLifecycleOwner(), (key, args) -> {
            if (args.getString(BookingUi.SELECTED_DATE) != null) model.select(LocalDate.parse(args.getString(BookingUi.SELECTED_DATE)));
            model.refresh(); openDetail(args);
        });
        model.state().observe(getViewLifecycleOwner(), this::render);
        Bundle args = getArguments();
        String date = saved != null ? saved.getString(BookingUi.SELECTED_DATE)
                : args == null ? null : args.getString(BookingUi.SELECTED_DATE);
        model.initialize(date == null ? LocalDate.now(BookingUi.ZONE) : LocalDate.parse(date));
        if (saved == null && args != null) openDetail(args);
    }
    private void render(ScheduleViewModel.State state) {
        binding.tvMonthYear.setText(getString(R.string.schedule_month_year, state.date.getMonthValue(), state.date.getYear()));
        binding.tvDateTag.setText(state.date.format(DateTimeFormatter.ofPattern("dd/M")));
        View[] tabs = {binding.tabMon, binding.tabTue, binding.tabWed, binding.tabThu, binding.tabFri, binding.tabSat, binding.tabSun};
        int selected = state.date.getDayOfWeek().getValue() - 1;
        for (int i = 0; i < tabs.length; i++) {
            tabs[i].setSelected(i == selected);
            tabs[i].setAlpha(1f);
        }
        View root = binding.getRoot();
        root.post(() -> {
            if (binding == null || getView() != root || model.date().getDayOfWeek().getValue() - 1 != selected) return;
            View tag = binding.tvDateTag;
            tag.setTranslationY(binding.layoutDayTabs.getTop() + tabs[selected].getTop()
                    + (tabs[selected].getHeight() - tag.getHeight()) / 2f - tag.getTop());
        });
        adapter.submit(state.bookings);
        binding.progressBar.setVisibility(state.loading ? View.VISIBLE : View.GONE);
        binding.tvEmptyState.setText(state.failed ? R.string.booking_retry : R.string.booking_empty);
        binding.tvEmptyState.setVisibility(!state.loading && (state.failed || state.bookings.isEmpty()) ? View.VISIBLE : View.GONE);
    }
    private void openDetail(Bundle args) {
        String id = args.getString(BookingUi.BOOKING_ID);
        if (id != null && !id.isEmpty() && new PreferenceManager(requireContext()).isLogin())
            new BookingDetailDialog((MainActivity) requireActivity(), id).show();
    }
    @Override public void onSaveInstanceState(@NonNull Bundle state) {
        if (model != null) state.putString(BookingUi.SELECTED_DATE, model.date().toString());
        super.onSaveInstanceState(state);
    }
}
