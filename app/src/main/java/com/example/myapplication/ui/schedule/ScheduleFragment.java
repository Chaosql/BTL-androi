package com.example.myapplication.ui.schedule;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.main.UiScreen;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

public class ScheduleFragment extends BaseFragment {
    private LocalDate selectedScheduleDate = LocalDate.now();

    private void updateSchedule(View root) {
        text(root, R.id.tvMonthYear, getString(R.string.schedule_month_year,
                selectedScheduleDate.getMonthValue(), selectedScheduleDate.getYear()));
        text(root, R.id.tvDateTag, selectedScheduleDate.format(DateTimeFormatter.ofPattern("dd/M")));
        int[] tabs = {R.id.tabMon, R.id.tabTue, R.id.tabWed, R.id.tabThu, R.id.tabFri, R.id.tabSat, R.id.tabSun};
        int selected = selectedScheduleDate.getDayOfWeek().getValue() - 1;
        for (int i = 0; i < tabs.length; i++) {
            View tab = root.findViewById(tabs[i]);
            tab.setSelected(i == selected);
            tab.setBackgroundResource(i == selected ? R.drawable.bg_tab_selected : R.drawable.bg_tab_unselected);
            tab.setAlpha(1f);
        }
        View tag = root.findViewById(R.id.tvDateTag);
        root.post(() -> {
            if (!isAdded() || getView() != root) return;
            View selectedTab = root.findViewById(tabs[selected]);
            View tabColumn = root.findViewById(R.id.layoutDayTabs);
            tag.setTranslationY(tabColumn.getTop() + selectedTab.getTop()
                    + (selectedTab.getHeight() - tag.getHeight()) / 2f - tag.getTop());
        });
    }

    @Override public void onSaveInstanceState(@NonNull Bundle state) {
        state.putString("selected_date", selectedScheduleDate.toString());
        super.onSaveInstanceState(state);
    }

    public ScheduleFragment() { super(R.layout.fragment_schedule); }

    @Override protected void onScreenCreated(View root, Bundle state) {
        if (state != null) selectedScheduleDate = LocalDate.parse(state.getString("selected_date", LocalDate.now().toString()));
        list(root, R.id.rvScheduleList, new ScheduleAdapter(this));
        int[] tabs = {R.id.tabMon, R.id.tabTue, R.id.tabWed, R.id.tabThu, R.id.tabFri, R.id.tabSat, R.id.tabSun};
        for (int index = 0; index < tabs.length; index++) {
            int day = index;
            int id = tabs[index];
            on(root, id, () -> {
                selectedScheduleDate = selectedScheduleDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusDays(day);
                updateSchedule(root);
            });
        }
        on(root, R.id.btnWeekPrev, () -> { selectedScheduleDate = selectedScheduleDate.minusWeeks(1); updateSchedule(root); });
        on(root, R.id.btnWeekNext, () -> { selectedScheduleDate = selectedScheduleDate.plusWeeks(1); updateSchedule(root); });
        updateSchedule(root);
        on(root, R.id.imgMascot, () -> go(UiScreen.CREATE_APPOINTMENT));
    }
}
