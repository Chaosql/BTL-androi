package com.example.myapplication.ui.schedule;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.data.remote.dto.response.BookingDto;
import com.example.myapplication.databinding.ItemScheduleBinding;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.Holder> {
    private final List<BookingDto> items = new ArrayList<>();
    private final Consumer<BookingDto> click;
    public ScheduleAdapter(Consumer<BookingDto> click) { this.click = click; }
    public void submit(List<BookingDto> data) { items.clear(); items.addAll(data); notifyDataSetChanged(); }
    @Override public int getItemCount() { return items.size(); }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new Holder(ItemScheduleBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        BookingDto item = items.get(position);
        ItemScheduleBinding binding = holder.binding;
        ZonedDateTime start = BookingUi.time(item.scheduledAt);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm");
        binding.tvTime.setText(start == null ? "—" : start.format(format) + " - " +
                start.plusMinutes(item.durationMinutes == null ? 30 : item.durationMinutes).format(format));
        String title = BookingUi.value(item.subject);
        String location = BookingUi.value(item.locationName);
        String description = location.isEmpty() ? title : binding.getRoot().getContext().getString(R.string.booking_row, title, location);
        binding.tvTaskDescription.setText(binding.getRoot().getContext().getString(R.string.booking_row,
                description, binding.getRoot().getContext().getString(BookingUi.statusText(BookingUi.status(item)))));
        binding.imgItemIcon.setImageResource(R.drawable.ic_schedule_bean);
        binding.getRoot().setOnClickListener(view -> click.accept(item));
    }
    public static class Holder extends RecyclerView.ViewHolder {
        public final ItemScheduleBinding binding;
        Holder(ItemScheduleBinding binding) { super(binding.getRoot()); this.binding = binding; }
    }
}
