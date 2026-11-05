package com.example.myapplication.ui.schedule;

import com.example.myapplication.R;
import com.example.myapplication.ui.base.BaseFragment;
import com.example.myapplication.ui.base.BasePreviewAdapter;
import com.example.myapplication.ui.base.PreviewRow;

public class ScheduleAdapter extends BasePreviewAdapter {
    public ScheduleAdapter(BaseFragment owner) {
        super(owner,
                new PreviewRow(R.layout.item_schedule).with(R.id.tvTime, "14:30").with(R.id.tvTaskDescription, owner.getString(R.string.ui_uirows_text_26))
                        .click(() -> owner.dialog(R.layout.dialog_booking_detail)),
                new PreviewRow(R.layout.item_schedule).with(R.id.tvTime, "17:00").with(R.id.tvTaskDescription, owner.getString(R.string.ui_uirows_text_27))
                        .click(() -> owner.dialog(R.layout.dialog_booking_detail)));
    }
}
