package com.example.myapplication;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import com.example.myapplication.ui.main.MainActivity;
import com.example.myapplication.ui.main.ScreenFactory;
import com.example.myapplication.ui.main.UiScreen;
import com.example.myapplication.ui.auth.InputEmailFragment;
import com.example.myapplication.ui.auth.VerifyOtpFragment;
import com.example.myapplication.ui.auth.ForgotPasswordFragment;
import com.example.myapplication.ui.schedule.ScheduleFragment;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class ScreenStructureTest {
    @Test public void everyScreenHasItsOwnRestorableFragment() throws Exception {
        Set<Class<?>> classes = new HashSet<>();
        for (UiScreen screen : UiScreen.values()) {
            Fragment fragment = ScreenFactory.create(screen);
            assertTrue(screen.name(), classes.add(fragment.getClass()));
            assertTrue(fragment.getClass().getName().startsWith("com.example.myapplication.ui."));
            assertEquals(fragment.getClass(), fragment.getClass().getConstructor().newInstance().getClass());
        }
        Bundle original = new Bundle();
        original.putBoolean(VerifyOtpFragment.ARG_RESET_PASSWORD, true);
        Fragment fragment = ScreenFactory.create(UiScreen.VERIFY_OTP, original);
        original.putBoolean(VerifyOtpFragment.ARG_RESET_PASSWORD, false);
        assertTrue(fragment.requireArguments().getBoolean(VerifyOtpFragment.ARG_RESET_PASSWORD));
    }

    @Test public void passwordResetRequiresEmailAndKeepsBackNavigation() {
        try (ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity activity = controller.get();
            activity.getSupportFragmentManager().executePendingTransactions();
            activity.findViewById(R.id.tvForgot).performClick();
            activity.getSupportFragmentManager().executePendingTransactions();
            assertTrue(current(activity) instanceof InputEmailFragment);
            activity.findViewById(R.id.btnSendEmail).performClick();
            activity.getSupportFragmentManager().executePendingTransactions();
            assertTrue(current(activity) instanceof InputEmailFragment);
            assertTrue(((android.widget.EditText) activity.findViewById(R.id.edtEmail)).getError() != null);
            activity.getOnBackPressedDispatcher().onBackPressed();
            activity.getSupportFragmentManager().executePendingTransactions();
            assertTrue(current(activity) instanceof com.example.myapplication.ui.auth.LoginFragment);
        }
    }

    @Test public void selectedScheduleDateSurvivesActivityRecreation() {
        try (ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity activity = controller.get();
            activity.getSupportFragmentManager().executePendingTransactions();
            activity.showScreen(UiScreen.SCHEDULE);
            activity.getSupportFragmentManager().executePendingTransactions();
            activity.findViewById(R.id.tabTue).performClick();
            activity.findViewById(R.id.btnWeekNext).performClick();
            String date = ((android.widget.TextView) activity.findViewById(R.id.tvDateTag)).getText().toString();
            controller.recreate();
            activity = controller.get();
            activity.getSupportFragmentManager().executePendingTransactions();
            assertTrue(current(activity) instanceof ScheduleFragment);
            assertEquals(date, ((android.widget.TextView) activity.findViewById(R.id.tvDateTag)).getText().toString());
            assertTrue(activity.findViewById(R.id.tabTue).isSelected());
        }
    }

    private Fragment current(MainActivity activity) {
        return activity.getSupportFragmentManager().findFragmentById(R.id.screenHost);
    }
}
