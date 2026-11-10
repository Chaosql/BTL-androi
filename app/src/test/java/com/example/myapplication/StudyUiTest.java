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
            android.graphics.Bitmap pixels = android.graphics.Bitmap.createBitmap(100, 100, android.graphics.Bitmap.Config.ARGB_8888);
            android.graphics.drawable.Drawable background = root.getBackground();
            android.graphics.Rect bounds = new android.graphics.Rect(background.getBounds());
            background.setBounds(0, 0, 100, 100); background.draw(new android.graphics.Canvas(pixels));
            assertEquals(controller.get().getColor(R.color.bg), pixels.getPixel(50, 1));
            assertEquals(controller.get().getColor(R.color.bg_2), pixels.getPixel(50, 99));
            background.setBounds(bounds); pixels.recycle();
        }
    }

    @Test public void sendOtpRemainsInsideViewportWhenKeyboardOpensAndCloses() {
        try (ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity activity = controller.get();
            activity.getSupportFragmentManager().executePendingTransactions();
            activity.showScreen(UiScreen.INPUT_EMAIL);
            activity.getSupportFragmentManager().executePendingTransactions();
            View root = activity.findViewById(R.id.main);
            View button = activity.findViewById(R.id.btnSendEmail);
            android.widget.EditText email = activity.findViewById(R.id.edtEmail);
            email.requestFocus(); email.setText("student@example.com");
            float density = activity.getResources().getDisplayMetrics().density;
            int width = (int) (360 * density), height = (int) (640 * density);
            for (int keyboardHeight : new int[]{0, 300, 0}) {
                ViewCompat.dispatchApplyWindowInsets(root, new WindowInsetsCompat.Builder()
                        .setInsets(WindowInsetsCompat.Type.systemBars(), Insets.of(0, (int) (24 * density), 0, (int) (32 * density)))
                        .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, (int) (keyboardHeight * density))).build());
                root.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
                root.layout(0, 0, width, height);
                android.graphics.Rect bounds = new android.graphics.Rect(0, 0, button.getWidth(), button.getHeight());
                ((android.view.ViewGroup) root).offsetDescendantRectToMyCoords(button, bounds);
                assertEquals(View.VISIBLE, button.getVisibility());
                assertTrue(button.isEnabled());
                assertTrue(bounds.height() > 0);
                assertTrue(bounds.top >= root.getPaddingTop());
                assertTrue(bounds.bottom <= height - root.getPaddingBottom());
                assertEquals("student@example.com", email.getText().toString());
            }
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

    @Test public void navigationWorksAndChatRequiresConversation() {
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
            assertEquals(before, list.getAdapter().getItemCount());
            assertTrue(!input.getText().toString().isEmpty());
            assertTrue(!activity.findViewById(R.id.btnSend).isEnabled());
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

    @Test public void boundRegistrationControlsKeepTheirActions() {
        try (ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity activity = controller.get();
            activity.getSupportFragmentManager().executePendingTransactions();
            activity.showScreen(UiScreen.REGISTER);
            activity.getSupportFragmentManager().executePendingTransactions();
            View root = activity.getSupportFragmentManager().findFragmentById(R.id.screenHost).requireView();
            com.example.myapplication.databinding.FragmentRegisterBinding form =
                    com.example.myapplication.databinding.FragmentRegisterBinding.bind(root);
            assertEquals(3, form.spGender.getAdapter().getCount());
            form.edtPassword.setText("Password123");
            form.edtConfirmPassword.setText("Password123");
            assertTrue(!form.ivTogglePassword.isSelected());
            form.ivTogglePassword.performClick();
            assertTrue(form.ivTogglePassword.isSelected());
            assertTrue(!form.ivToggleConfirmPassword.isSelected());
            assertEquals(activity.getString(R.string.password_hide), form.ivTogglePassword.getContentDescription());
            assertEquals(null, form.edtPassword.getTransformationMethod());
            assertTrue(form.edtConfirmPassword.getTransformationMethod()
                    instanceof android.text.method.PasswordTransformationMethod);
            form.ivToggleConfirmPassword.performClick();
            assertEquals(null, form.edtConfirmPassword.getTransformationMethod());
            form.ivTogglePassword.performClick();
            assertTrue(form.edtPassword.getTransformationMethod()
                    instanceof android.text.method.PasswordTransformationMethod);
            assertTrue(!form.ivTogglePassword.isSelected());
            assertEquals(activity.getString(R.string.password_show), form.ivTogglePassword.getContentDescription());
            assertEquals(form.edtPassword.length(), form.edtPassword.getSelectionStart());
            form.ivCalendar.performClick();
            android.app.DatePickerDialog picker = (android.app.DatePickerDialog)
                    org.robolectric.shadows.ShadowDialog.getLatestDialog();
            picker.updateDate(2004, 10, 7);
            picker.onClick(picker, android.content.DialogInterface.BUTTON_POSITIVE);
            picker.dismiss();
            assertEquals("07/11/2004", form.edtDate.getText().toString());
            form.btnBack.performClick();
            activity.getSupportFragmentManager().executePendingTransactions();
            assertTrue(activity.getSupportFragmentManager().findFragmentById(R.id.screenHost)
                    instanceof com.example.myapplication.ui.auth.LoginFragment);
        }
    }

    @Test public void passwordArtworkChangesAndStaysVisibleUntilAllFieldsAreHidden() {
        try (ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity activity = controller.get(); activity.getSupportFragmentManager().executePendingTransactions();
            for (UiScreen screen : new UiScreen[]{UiScreen.LOGIN, UiScreen.REGISTER, UiScreen.FORGOT_PASSWORD}) {
                activity.showScreen(screen); activity.getSupportFragmentManager().executePendingTransactions();
                View root = activity.getSupportFragmentManager().findFragmentById(R.id.screenHost).requireView();
                android.widget.ImageView artwork = root.findViewById(R.id.ivPic);
                View first = root.findViewById(R.id.ivTogglePassword);
                View second = root.findViewById(R.id.ivToggleConfirmPassword);
                assertEquals(R.drawable.frame3, imageResource(artwork));
                first.performClick(); assertEquals(R.drawable.frame1, imageResource(artwork));
                if (second != null) {
                    second.performClick(); first.performClick();
                    assertEquals(R.drawable.frame1, imageResource(artwork));
                    second.performClick();
                } else first.performClick();
                assertEquals(R.drawable.frame3, imageResource(artwork));
            }
        }
    }
    private int imageResource(android.widget.ImageView view) {
        org.robolectric.shadows.ShadowDrawable drawable = org.robolectric.shadow.api.Shadow.extract(view.getDrawable());
        return drawable.getCreatedFromResId();
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
