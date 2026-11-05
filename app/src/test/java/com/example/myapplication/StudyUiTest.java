package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
                        .replace(R.id.screenHost, StudyScreenFragment.create(screen)).commitNow();
                Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
                View root = activity.findViewById(R.id.screenHost);
                root.measure(View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(1800, View.MeasureSpec.EXACTLY));
                root.layout(0, 0, 1080, 1800);
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

    @Test public void loginNavigationAndChatPreviewWork() {
        try (ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity activity = controller.get();
            activity.getSupportFragmentManager().executePendingTransactions();
            activity.findViewById(R.id.tvLogin).performClick();
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
