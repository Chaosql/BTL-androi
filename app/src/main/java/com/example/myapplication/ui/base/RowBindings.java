package com.example.myapplication.ui.base;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.viewbinding.ViewBinding;
import com.example.myapplication.R;
import com.example.myapplication.databinding.*;

public final class RowBindings {
    public static ViewBinding inflate(int layout, LayoutInflater inflater, ViewGroup parent) {
        if (layout == R.layout.item_blocked_user) return ItemBlockedUserBinding.inflate(inflater, parent, false);
        if (layout == R.layout.item_chat_booking_incoming) return ItemChatBookingIncomingBinding.inflate(inflater, parent, false);
        if (layout == R.layout.item_chat_booking_outgoing) return ItemChatBookingOutgoingBinding.inflate(inflater, parent, false);
        if (layout == R.layout.item_chat_image_incoming) return ItemChatImageIncomingBinding.inflate(inflater, parent, false);
        if (layout == R.layout.item_chat_image_outgoing) return ItemChatImageOutgoingBinding.inflate(inflater, parent, false);
        if (layout == R.layout.item_chat_incoming) return ItemChatIncomingBinding.inflate(inflater, parent, false);
        if (layout == R.layout.item_chat_outgoing) return ItemChatOutgoingBinding.inflate(inflater, parent, false);
        if (layout == R.layout.item_dialog_subject) return ItemDialogSubjectBinding.inflate(inflater, parent, false);
        if (layout == R.layout.item_friend) return ItemFriendBinding.inflate(inflater, parent, false);
        if (layout == R.layout.item_notification) return ItemNotificationBinding.inflate(inflater, parent, false);
        if (layout == R.layout.item_pending_request) return ItemPendingRequestBinding.inflate(inflater, parent, false);
        if (layout == R.layout.item_schedule) return ItemScheduleBinding.inflate(inflater, parent, false);
        if (layout == R.layout.item_spinner_dropdown) return ItemSpinnerDropdownBinding.inflate(inflater, parent, false);
        if (layout == R.layout.item_spinner_gender) return ItemSpinnerGenderBinding.inflate(inflater, parent, false);
        if (layout == R.layout.item_spinner_gender_dropdown) return ItemSpinnerGenderDropdownBinding.inflate(inflater, parent, false);
        if (layout == R.layout.item_spinner_selected) return ItemSpinnerSelectedBinding.inflate(inflater, parent, false);
        if (layout == R.layout.item_user_search_result) return ItemUserSearchResultBinding.inflate(inflater, parent, false);
        throw new IllegalArgumentException("Unknown row layout: " + layout);
    }
    private RowBindings() { }
}
