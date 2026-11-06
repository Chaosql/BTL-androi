package com.example.myapplication;

import com.example.myapplication.ui.main.UiDialogs;
import com.example.myapplication.ui.main.ScreenFactory;
import com.example.myapplication.ui.main.UiScreen;
import com.example.myapplication.ui.main.MainActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.NumberPicker;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class StudyUiTest {
    @Test public void allScreensAndRowsRender() {
        try (ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity activity = controller.get();
            for (UiScreen screen : UiScreen.values()) {
                activity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.screenHost, ScreenFactory.create(screen)).commitNow();
                Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
                View root = activity.findViewById(R.id.screenHost);
                int width = activity.getResources().getDisplayMetrics().widthPixels;
                int height = activity.getResources().getDisplayMetrics().heightPixels;
                root.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
                root.layout(0, 0, width, height);
                renderRows(root);
                assertNotNull(screen.name(), activity.getSupportFragmentManager().findFragmentById(R.id.screenHost).getView());
            }
            for (int layout : UiDialogs.LAYOUTS) {
                assertNotNull(LayoutInflater.from(activity).inflate(layout, null, false));
                UiDialogs.show(activity, layout);
                org.robolectric.shadows.ShadowDialog.getLatestDialog().dismiss();
            }
        }
    }

    @Test @Config(qualifiers = "w360dp-h640dp-xhdpi")
    public void allScreensRenderOnCompactPhone() { allScreensAndRowsRender(); }

    @Test @Config(qualifiers = "w412dp-h915dp-xxhdpi")
    public void allScreensRenderOnTallPhone() { allScreensAndRowsRender(); }

    @Test public void systemInsetsAndKeyboardAreAppliedOnlyOnce() {
        try (ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).setup()) {
            View root = controller.get().findViewById(R.id.main);
            WindowInsetsCompat keyboard = new WindowInsetsCompat.Builder()
                    .setInsets(WindowInsetsCompat.Type.systemBars(), Insets.of(0, 24, 0, 32))
                    .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, 300)).build();
            ViewCompat.dispatchApplyWindowInsets(root, keyboard);
            ViewCompat.dispatchApplyWindowInsets(root, keyboard);
            assertEquals(24, root.getPaddingTop());
            assertEquals(300, root.getPaddingBottom());
            WindowInsetsCompat closed = new WindowInsetsCompat.Builder()
                    .setInsets(WindowInsetsCompat.Type.systemBars(), Insets.of(0, 24, 0, 32)).build();
            ViewCompat.dispatchApplyWindowInsets(root, closed);
            assertEquals(32, root.getPaddingBottom());
        }
    }

    @Test public void scheduleTabsAndAppointmentFieldsMatchPresentation() {
        try (ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity activity = controller.get();
            activity.getSupportFragmentManager().executePendingTransactions();
            activity.showScreen(UiScreen.SCHEDULE);
            activity.getSupportFragmentManager().executePendingTransactions();
            activity.findViewById(R.id.tabWed).performClick();
            assertTrue(activity.findViewById(R.id.tabWed).isSelected());
            assertEquals(1f, activity.findViewById(R.id.tabMon).getAlpha(), 0f);
            String firstDate = ((TextView) activity.findViewById(R.id.tvDateTag)).getText().toString();
            activity.findViewById(R.id.btnWeekNext).performClick();
            assertTrue(!firstDate.equals(((TextView) activity.findViewById(R.id.tvDateTag)).getText().toString()));
            activity.showScreen(UiScreen.CREATE_APPOINTMENT);
            activity.getSupportFragmentManager().executePendingTransactions();
            assertEquals("Tạo cuộc hẹn", ((TextView) activity.findViewById(R.id.tvScreenTitle)).getText().toString());
            assertEquals(activity.getColor(R.color.brown), ((NumberPicker) activity.findViewById(R.id.npHour)).getTextColor());
            UiDialogs.show(activity, R.layout.dialog_booking_detail);
            android.app.Dialog dialog = org.robolectric.shadows.ShadowDialog.getLatestDialog();
            assertEquals(View.GONE, dialog.findViewById(R.id.layoutRatingSection).getVisibility());
            assertEquals(View.GONE, dialog.findViewById(R.id.btnComplete).getVisibility());
            assertEquals(View.GONE, dialog.findViewById(R.id.btnCancel).getVisibility());
            dialog.dismiss();
        }
    }

    @Test public void loginNavigationAndChatPreviewWork() {
        try (ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity activity = controller.get();
            activity.getSupportFragmentManager().executePendingTransactions();
            activity.showScreen(UiScreen.MAP);
            activity.getSupportFragmentManager().executePendingTransactions();
            assertNotNull(activity.findViewById(R.id.mapView));
            activity.findViewById(R.id.btnFriend).performClick();
            activity.getSupportFragmentManager().executePendingTransactions();
            assertNotNull(activity.findViewById(R.id.rvFriendsList));
            activity.showScreen(UiScreen.CHAT);
            activity.getSupportFragmentManager().executePendingTransactions();
            RecyclerView list = activity.findViewById(R.id.rvChatMessages);
            int before = list.getAdapter().getItemCount();
            EditText input = activity.findViewById(R.id.etMessage);
            input.setText("Tin nhắn mẫu");
            activity.findViewById(R.id.btnSend).performClick();
            assertEquals(before + 1, list.getAdapter().getItemCount());
            assertTrue(input.getText().toString().isEmpty());
        }
    }

    @Test public void inputHintsAndBackgroundsAreConsistent() {
        try (ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity activity = controller.get();
            View subject = LayoutInflater.from(activity).inflate(R.layout.dialog_add_subject, null, false);
            EditText subjectName = subject.findViewById(R.id.etSubjectName);
            assertEquals("Tên môn học", subjectName.getHint().toString());
            assertTrue(subjectName.getText().toString().isEmpty());
            assertNotNull(subjectName.getBackground());
            assertEquals(activity.getColor(R.color.hint), subjectName.getCurrentHintTextColor());
            subjectName.setText("Java");
            assertEquals("Java", subjectName.getText().toString());

            View registration = LayoutInflater.from(activity).inflate(R.layout.fragment_register, null, false);
            assertEquals("Nhập lại mật khẩu", ((EditText) registration.findViewById(R.id.edtConfirmPassword)).getHint().toString());
            assertEquals("Nhập họ", ((EditText) registration.findViewById(R.id.edtLastName)).getHint().toString());
            View personal = LayoutInflater.from(activity).inflate(R.layout.dialog_edit_personal_info, null, false);
            assertEquals(activity.getString(R.string.hint_birth_date),
                    ((EditText) personal.findViewById(R.id.etDateOfBirth)).getHint().toString());
            View info = LayoutInflater.from(activity).inflate(R.layout.fragment_registration_info, null, false);
            assertEquals(activity.getString(R.string.hint_birth_date),
                    ((EditText) info.findViewById(R.id.etDateOfBirth)).getHint().toString());
            View password = LayoutInflater.from(activity).inflate(R.layout.fragment_forgot_password, null, false);
            assertEquals(activity.getColor(R.color.input_text), ((EditText) password.findViewById(R.id.edtNewPassword)).getCurrentTextColor());
            View success = LayoutInflater.from(activity).inflate(R.layout.dialog_login_success, null, false);
            assertTrue(((TextView) success.findViewById(R.id.tvMessage)).getText().toString().contains("Study Date"));
            assertTrue(!activity.getString(R.string.verify_your_email).contains("abc123@gmail.com"));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void renderRows(View root) {
        if (root instanceof RecyclerView) {
            RecyclerView list = (RecyclerView) root;
            RecyclerView.Adapter adapter = list.getAdapter();
            if (adapter != null) {
                for (int i = 0; i < adapter.getItemCount(); i++) {
                    RecyclerView.ViewHolder holder = adapter.createViewHolder(list, adapter.getItemViewType(i));
                    adapter.bindViewHolder(holder, i);
                }
            }
        } else if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) renderRows(group.getChildAt(i));
        }
    }
}
