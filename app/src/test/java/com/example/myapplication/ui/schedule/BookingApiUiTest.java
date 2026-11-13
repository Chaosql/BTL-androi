package com.example.myapplication.ui.schedule;

import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.response.LoginData;
import com.example.myapplication.data.remote.network.RetrofitClient;
import com.example.myapplication.ui.main.MainActivity;
import com.example.myapplication.ui.main.UiScreen;
import com.google.gson.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import okhttp3.mockwebserver.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowDialog;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class BookingApiUiTest {
    private MockWebServer server;
    private PreferenceManager preferences;
    private ActivityController<MainActivity> controller;
    private MainActivity activity;
    private JsonObject booking;
    private LocalDate day;
    private volatile boolean failAction;
    private volatile boolean unchangedAction;
    private volatile boolean failLoad;
    private volatile boolean forbiddenDetail;
    private volatile boolean duplicate;
    private final List<String> mutations = Collections.synchronizedList(new ArrayList<>());
    private final List<JsonObject> bodies = Collections.synchronizedList(new ArrayList<>());
    private final List<String> weeks = Collections.synchronizedList(new ArrayList<>());

    private MockResponse response(JsonElement data) {
        JsonObject body = new JsonObject(); body.addProperty("codeStatus", 200); body.add("data", data.deepCopy());
        return new MockResponse().setHeader("Content-Type", "application/json").setBody(body.toString());
    }
    @Before public void setup() throws Exception {
        day = LocalDate.now(BookingUi.ZONE).plusDays(2);
        booking = JsonParser.parseString("{\"id\":\"b1\",\"subject\":\"Java\",\"status\":\"PENDING\",\"creatorId\":\"creator\",\"partnerId\":\"self\",\"creatorName\":\"Creator\",\"partnerName\":\"Partner\",\"durationMinutes\":60,\"locationName\":\"Library\",\"hasRated\":false}").getAsJsonObject();
        booking.addProperty("scheduledAt", day.atTime(10, 0).atZone(BookingUi.ZONE).toInstant().toString());
        server = new MockWebServer();
        server.setDispatcher(new Dispatcher() {
            @Override public MockResponse dispatch(RecordedRequest request) {
                String path = request.getRequestUrl().encodedPath();
                if ("GET".equals(request.getMethod())) {
                    if (failLoad) return new MockResponse().setResponseCode(503).setBody("{\"message\":\"Unavailable\"}");
                    if (path.equals("/api/v1/bookings/weekly")) {
                        weeks.add(request.getRequestUrl().queryParameter("weekStart"));
                        JsonArray list = new JsonArray(); list.add(booking.deepCopy());
                        if (duplicate) list.add(booking.deepCopy());
                        JsonObject days = new JsonObject(); days.add(day.toString(), list);
                        JsonObject week = new JsonObject(); week.add("daySchedules", days);
                        return response(week);
                    }
                    if (path.equals("/api/v1/bookings")) {
                        JsonArray list = new JsonArray(); list.add(booking.deepCopy()); return response(list);
                    }
                    if (path.equals("/api/v1/bookings/b1")) {
                        if (forbiddenDetail) return new MockResponse().setResponseCode(403).setBody("{\"message\":\"Denied\"}");
                        return response(booking);
                    }
                    if (path.equals("/api/v1/friends")) {
                        JsonObject friend = new JsonObject(); friend.addProperty("userId", "peer");
                        friend.addProperty("firstName", "Peer"); friend.addProperty("lastName", "Nguyen");
                        JsonArray content = new JsonArray(); content.add(friend);
                        JsonObject page = new JsonObject(); page.add("content", content); page.addProperty("last", true);
                        return response(page);
                    }
                    return new MockResponse().setResponseCode(404);
                }
                String raw = request.getBody().readUtf8();
                JsonObject body = raw.isEmpty() ? new JsonObject() : JsonParser.parseString(raw).getAsJsonObject();
                bodies.add(body);
                mutations.add(request.getMethod() + " " + path);
                if (failAction) return new MockResponse().setResponseCode(409).setBody("{\"message\":\"Conflict\"}");
                if (unchangedAction && (path.endsWith("/complete") || path.endsWith("/cancel"))) return response(booking);
                if (path.equals("/api/v1/chat/conversations/direct")) {
                    JsonObject conversation = new JsonObject(); conversation.addProperty("id", "c1"); return response(conversation);
                }
                if (path.equals("/api/v1/bookings/conversation/c1")) {
                    for (String key : body.keySet()) booking.add(key, body.get(key));
                    booking.addProperty("creatorId", "self"); booking.addProperty("partnerId", "peer");
                    return response(booking);
                }
                if (path.endsWith("/accept")) booking.addProperty("status", "CONFIRMED");
                if (path.endsWith("/reject")) booking.addProperty("status", "REJECTED");
                if (path.endsWith("/complete")) booking.addProperty("status", "COMPLETED");
                if (path.endsWith("/cancel")) {
                    booking.addProperty("status", "CANCELLED");
                    booking.add("cancelReason", body.get("cancelReason"));
                }
                if (path.endsWith("/rate")) {
                    booking.addProperty("hasRated", true); booking.add("myRating", body.get("score"));
                    JsonObject status = new JsonObject(); status.addProperty("status", true); return response(status);
                }
                return response(booking);
            }
        });
        server.start();
        preferences = new PreferenceManager(RuntimeEnvironment.getApplication()); preferences.logout();
        preferences.saveLogin(new LoginData("Bearer", "token", "refresh", "self", null), "test@example.com");
        RetrofitClient.setApiServiceForTests(RetrofitClient.create(server.url("/").toString(), preferences));
        controller = Robolectric.buildActivity(MainActivity.class).setup(); activity = controller.get();
        activity.getSupportFragmentManager().executePendingTransactions();
    }
    @After public void cleanup() throws Exception {
        controller.close(); RetrofitClient.setApiServiceForTests(null); preferences.logout(); server.shutdown();
    }
    private void await(BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
            Shadows.shadowOf(Looper.getMainLooper()).idle(); Thread.sleep(10);
        }
        assertTrue("Timed out waiting for booking API", condition.getAsBoolean());
    }
    private void openSchedule() {
        Bundle args = new Bundle(); args.putString(BookingUi.SELECTED_DATE, day.toString());
        activity.showScreen(UiScreen.SCHEDULE, args); activity.getSupportFragmentManager().executePendingTransactions();
    }
    private android.app.Dialog detail() throws Exception {
        new BookingDetailDialog(activity, "b1").show();
        android.app.Dialog dialog = ShadowDialog.getLatestDialog();
        await(() -> ((TextView) dialog.findViewById(R.id.tvSubject)).getText().toString().equals("Java"));
        return dialog;
    }
    private void openCreate(boolean conversation) {
        Bundle args = new Bundle(); args.putString(BookingUi.SELECTED_DATE, day.toString());
        if (conversation) args.putString(CreateAppointmentFragment.CONVERSATION_ID, "c1");
        activity.showScreen(UiScreen.CREATE_APPOINTMENT, args); activity.getSupportFragmentManager().executePendingTransactions();
        ((NumberPicker) activity.findViewById(R.id.npHour)).setValue(10);
        ((NumberPicker) activity.findViewById(R.id.npMinute)).setValue(0);
    }
    private RecyclerView list() { return activity.findViewById(R.id.rvScheduleList); }

    @Test public void calendarMergesDuplicatesAndUsesVietnamDate() throws Exception {
        duplicate = true;
        booking.addProperty("scheduledAt", day.minusDays(1).atTime(19, 0).toInstant(ZoneOffset.UTC).toString());
        openSchedule();
        await(() -> activity.findViewById(R.id.progressBar).getVisibility() == View.GONE);
        assertEquals(1, list().getAdapter().getItemCount());
        ScheduleAdapter adapter = (ScheduleAdapter) list().getAdapter();
        ScheduleAdapter.Holder row = adapter.createViewHolder(list(), 0);
        adapter.bindViewHolder(row, 0);
        assertEquals("02:00 - 03:00", row.binding.tvTime.getText().toString());
        activity.findViewById(R.id.btnWeekNext).performClick();
        await(() -> activity.findViewById(R.id.progressBar).getVisibility() == View.GONE);
        assertEquals(0, list().getAdapter().getItemCount());
        assertTrue(weeks.size() >= 2);
    }
    @Test public void pendingRecipientCanAcceptThenCompleteAndRate() throws Exception {
        android.app.Dialog dialog = detail();
        assertEquals(View.VISIBLE, dialog.findViewById(R.id.btnAccept).getVisibility());
        assertEquals(View.VISIBLE, dialog.findViewById(R.id.btnCancel).getVisibility());
        dialog.findViewById(R.id.btnAccept).performClick();
        await(() -> dialog.findViewById(R.id.btnComplete).getVisibility() == View.VISIBLE);
        dialog.findViewById(R.id.btnComplete).performClick();
        await(() -> dialog.findViewById(R.id.layoutRatingSection).getVisibility() == View.VISIBLE);
        ((RatingBar) dialog.findViewById(R.id.ratingBar)).setRating(4);
        dialog.findViewById(R.id.btnRate).performClick();
        await(() -> dialog.findViewById(R.id.btnRate).getVisibility() == View.GONE);
        assertEquals(Arrays.asList("PUT /api/v1/bookings/b1/accept", "PATCH /api/v1/bookings/b1/complete",
                "POST /api/v1/bookings/b1/rate"), mutations);
        assertEquals(4, bodies.get(2).get("score").getAsInt());
        dialog.dismiss();
    }
    @Test public void creatorCannotAcceptOwnInvitation() throws Exception {
        booking.addProperty("creatorId", "self"); booking.addProperty("partnerId", "peer");
        android.app.Dialog dialog = detail();
        assertEquals(View.GONE, dialog.findViewById(R.id.btnAccept).getVisibility());
        assertEquals(View.VISIBLE, dialog.findViewById(R.id.btnCancel).getVisibility());
        dialog.findViewById(R.id.btnAccept).performClick();
        assertTrue(mutations.isEmpty());
        dialog.dismiss();
    }
    @Test public void confirmedCreatorCanCompleteOwnBooking() throws Exception {
        booking.addProperty("creatorId", "self"); booking.addProperty("partnerId", "peer");
        booking.addProperty("participantStatus", "CREATOR"); booking.addProperty("status", "CONFIRMED");
        android.app.Dialog dialog = detail();
        assertEquals(View.VISIBLE, dialog.findViewById(R.id.btnComplete).getVisibility());
        assertEquals(View.VISIBLE, dialog.findViewById(R.id.btnCancel).getVisibility());
        dialog.findViewById(R.id.btnComplete).performClick();
        await(() -> dialog.findViewById(R.id.layoutRatingSection).getVisibility() == View.VISIBLE);
        assertEquals(Collections.singletonList("PATCH /api/v1/bookings/b1/complete"), mutations);
        dialog.dismiss();
    }
    @Test public void pendingInvitedParticipantCanCancelWithReason() throws Exception {
        android.app.Dialog parent = detail();
        parent.findViewById(R.id.btnCancel).performClick();
        android.app.Dialog cancel = ShadowDialog.getLatestDialog();
        ((EditText) cancel.findViewById(R.id.etCancelReason)).setText("Cannot attend");
        cancel.findViewById(R.id.btnConfirmCancel).performClick();
        await(() -> !cancel.isShowing() && !parent.isShowing());
        assertEquals(Collections.singletonList("PATCH /api/v1/bookings/b1/cancel"), mutations);
        assertEquals("Cannot attend", bodies.get(0).get("cancelReason").getAsString());
    }
    @Test public void serverCreatorRoleCanIdentifyCreatorWhenCreatorIdIsMissing() throws Exception {
        booking.remove("creatorId"); booking.remove("partnerId");
        booking.addProperty("participantStatus", "CREATOR"); booking.addProperty("status", "CONFIRMED");
        android.app.Dialog dialog = detail();
        assertEquals(View.VISIBLE, dialog.findViewById(R.id.btnComplete).getVisibility());
        assertEquals(View.VISIBLE, dialog.findViewById(R.id.btnCancel).getVisibility());
        dialog.dismiss();
    }
    @Test public void participantsUserIdIdentifiesInvitedParticipant() throws Exception {
        booking.addProperty("partnerId", "creator"); booking.addProperty("status", "CONFIRMED");
        booking.add("participants", JsonParser.parseString("[{\"id\":\"invitation-1\",\"userId\":\"self\",\"status\":\"ACCEPTED\"}]"));
        android.app.Dialog dialog = detail();
        assertEquals(View.VISIBLE, dialog.findViewById(R.id.btnComplete).getVisibility());
        assertEquals(View.VISIBLE, dialog.findViewById(R.id.btnCancel).getVisibility());
        dialog.dismiss();
    }
    @Test public void verifiedDetailAllowsCommonActionsWithoutCreatorMetadata() throws Exception {
        booking.remove("creatorId"); booking.remove("partnerId"); booking.addProperty("status", "CONFIRMED");
        BookingViewModel model = new BookingViewModel(RuntimeEnvironment.getApplication());
        com.example.myapplication.data.remote.dto.response.BookingDto raw = new Gson().fromJson(booking,
                com.example.myapplication.data.remote.dto.response.BookingDto.class);
        assertFalse(model.actions(raw).complete);
        assertFalse(model.actions(raw).cancel);
        android.app.Dialog dialog = detail();
        assertEquals(View.VISIBLE, dialog.findViewById(R.id.btnComplete).getVisibility());
        assertEquals(View.VISIBLE, dialog.findViewById(R.id.btnCancel).getVisibility());
        assertEquals(View.GONE, dialog.findViewById(R.id.tvErrorMessage).getVisibility());
        dialog.findViewById(R.id.btnComplete).performClick();
        await(() -> dialog.findViewById(R.id.layoutRatingSection).getVisibility() == View.VISIBLE);
        assertEquals(Collections.singletonList("PATCH /api/v1/bookings/b1/complete"), mutations);
        dialog.dismiss();
    }
    @Test public void verifiedPendingDetailWithoutRoleCanCancelButCannotAcceptOrReject() throws Exception {
        booking.remove("creatorId"); booking.remove("partnerId");
        android.app.Dialog dialog = detail();
        assertEquals(View.VISIBLE, dialog.findViewById(R.id.btnCancel).getVisibility());
        assertEquals(View.GONE, dialog.findViewById(R.id.btnComplete).getVisibility());
        assertEquals(View.GONE, dialog.findViewById(R.id.btnAccept).getVisibility());
        assertEquals(View.GONE, dialog.findViewById(R.id.btnReject).getVisibility());
        dialog.dismiss();
    }
    @Test public void forbiddenDetailNeverEnablesActions() throws Exception {
        forbiddenDetail = true;
        new BookingDetailDialog(activity, "b1").show();
        android.app.Dialog dialog = ShadowDialog.getLatestDialog();
        await(() -> "Denied".equals(((TextView) dialog.findViewById(R.id.tvErrorMessage)).getText().toString()));
        assertEquals(View.GONE, dialog.findViewById(R.id.btnCancel).getVisibility());
        assertEquals(View.GONE, dialog.findViewById(R.id.btnComplete).getVisibility());
        dialog.findViewById(R.id.btnComplete).performClick();
        assertTrue(mutations.isEmpty()); dialog.dismiss();
    }
    @Test public void changingAccountInvalidatesVerifiedDetailActions() throws Exception {
        booking.remove("creatorId"); booking.remove("partnerId"); booking.addProperty("status", "CONFIRMED");
        android.app.Dialog dialog = detail();
        preferences.saveLogin(new LoginData("Bearer", "other-token", "refresh", "other-user", null), "other@example.com");
        dialog.findViewById(R.id.btnComplete).performClick();
        dialog.findViewById(R.id.btnCancel).performClick();
        assertTrue(mutations.isEmpty()); dialog.dismiss();
    }
    @Test public void unchangedCompleteResponseShowsErrorInsteadOfSuccess() throws Exception {
        booking.addProperty("status", "CONFIRMED"); unchangedAction = true;
        android.app.Dialog dialog = detail();
        dialog.findViewById(R.id.btnComplete).performClick();
        await(() -> dialog.findViewById(R.id.btnComplete).isEnabled());
        assertEquals(activity.getString(R.string.booking_update_unconfirmed), ((TextView) dialog.findViewById(R.id.tvErrorMessage)).getText().toString());
        assertEquals(View.GONE, dialog.findViewById(R.id.layoutRatingSection).getVisibility());
        dialog.dismiss();
    }

    @Test public void rejectedInvitationHasNoActions() throws Exception {
        android.app.Dialog dialog = detail();
        dialog.findViewById(R.id.btnReject).performClick();
        await(() -> activity.getString(R.string.booking_rejected).equals(((TextView) dialog.findViewById(R.id.tvStatus)).getText().toString()));
        assertEquals(View.GONE, dialog.findViewById(R.id.btnAccept).getVisibility());
        assertEquals(View.GONE, dialog.findViewById(R.id.btnCancel).getVisibility());
        dialog.dismiss();
    }
    @Test public void cancellationKeepsReasonOnFailureThenRetries() throws Exception {
        booking.addProperty("status", "ACCEPTED");
        android.app.Dialog parent = detail();
        parent.findViewById(R.id.btnCancel).performClick();
        android.app.Dialog cancel = ShadowDialog.getLatestDialog();
        cancel.findViewById(R.id.btnConfirmCancel).performClick();
        assertTrue(mutations.isEmpty());
        assertEquals(View.VISIBLE, cancel.findViewById(R.id.tvCancelError).getVisibility());
        assertNull(((EditText) cancel.findViewById(R.id.etCancelReason)).getError());
        ((EditText) cancel.findViewById(R.id.etCancelReason)).setText("Busy");
        assertEquals(View.GONE, cancel.findViewById(R.id.tvCancelError).getVisibility());
        failAction = true;
        cancel.findViewById(R.id.btnConfirmCancel).performClick();
        await(() -> cancel.findViewById(R.id.btnConfirmCancel).isEnabled());
        assertTrue(cancel.isShowing()); assertTrue(parent.isShowing());
        assertEquals(View.VISIBLE, cancel.findViewById(R.id.tvCancelError).getVisibility());
        assertEquals("Busy", ((EditText) cancel.findViewById(R.id.etCancelReason)).getText().toString());
        failAction = false;
        cancel.findViewById(R.id.btnConfirmCancel).performClick();
        await(() -> !cancel.isShowing() && !parent.isShowing());
        assertEquals("Busy", bodies.get(bodies.size() - 1).get("cancelReason").getAsString());
    }
    @Test public void failedAcceptKeepsDialogAndEnablesRetry() throws Exception {
        failAction = true; android.app.Dialog dialog = detail();
        dialog.findViewById(R.id.btnAccept).performClick();
        await(() -> dialog.findViewById(R.id.btnAccept).isEnabled());
        assertEquals(View.VISIBLE, dialog.findViewById(R.id.tvErrorMessage).getVisibility());
        assertTrue(dialog.isShowing()); dialog.dismiss();
    }
    @Test public void ratingRejectsZeroAndDoesNotResendExistingRating() throws Exception {
        booking.addProperty("status", "COMPLETED");
        android.app.Dialog dialog = detail();
        ((RatingBar) dialog.findViewById(R.id.ratingBar)).setRating(0);
        dialog.findViewById(R.id.btnRate).performClick();
        assertTrue(mutations.isEmpty());
        dialog.dismiss();
        booking.addProperty("hasRated", true); booking.addProperty("myRating", 3);
        dialog = detail();
        assertEquals(View.GONE, dialog.findViewById(R.id.btnRate).getVisibility());
        assertTrue(((RatingBar) dialog.findViewById(R.id.ratingBar)).isIndicator());
        dialog.dismiss();
    }
    @Test public void createValidatesLocationAndPastDateBeforeRequest() {
        openCreate(true);
        activity.findViewById(R.id.btnCreate).performClick();
        assertNotNull(((EditText) activity.findViewById(R.id.etLocationName)).getError());
        assertTrue(mutations.isEmpty());
        ((EditText) activity.findViewById(R.id.etLocationName)).setText("Library");
        activity.findViewById(R.id.layoutDateField).performClick();
        android.app.DatePickerDialog picker = (android.app.DatePickerDialog) ShadowDialog.getLatestDialog();
        LocalDate past = day.minusDays(3);
        picker.getDatePicker().updateDate(past.getYear(), past.getMonthValue() - 1, past.getDayOfMonth());
        picker.onClick(picker, android.content.DialogInterface.BUTTON_POSITIVE);
        picker.dismiss();
        activity.findViewById(R.id.btnCreate).performClick();
        assertEquals(activity.getString(R.string.booking_invalid_time),
                ((TextView) activity.findViewById(R.id.tvErrorMessage)).getText().toString());
        assertTrue(mutations.isEmpty());
    }
    @Test public void createUsesUtcAndKeepsFormWhenApiFails() throws Exception {
        failAction = true; openCreate(true);
        ((EditText) activity.findViewById(R.id.etLocationName)).setText("Library");
        ((Spinner) activity.findViewById(R.id.spDuration)).setSelection(2);
        ((EditText) activity.findViewById(R.id.etNote)).setText("  Bring laptop\nReview Java  ");
        activity.findViewById(R.id.btnCreate).performClick();
        await(() -> activity.findViewById(R.id.btnCreate).isEnabled());
        assertEquals("Library", ((EditText) activity.findViewById(R.id.etLocationName)).getText().toString());
        assertEquals(day.atTime(3, 0).toInstant(ZoneOffset.UTC).toString(), bodies.get(0).get("scheduledAt").getAsString());
        assertEquals(60, bodies.get(0).get("durationMinutes").getAsInt());
        assertEquals("Bring laptop\nReview Java", bodies.get(0).get("note").getAsString());
        assertEquals("  Bring laptop\nReview Java  ", ((EditText) activity.findViewById(R.id.etNote)).getText().toString());
        assertTrue(activity.findViewById(R.id.etNote).isEnabled());
    }
    @Test public void createFromScheduleChoosesFriendAndGetsConversation() throws Exception {
        openSchedule();
        activity.findViewById(R.id.imgMascot).performClick();
        activity.getSupportFragmentManager().executePendingTransactions();
        ((NumberPicker) activity.findViewById(R.id.npHour)).setValue(10);
        ((NumberPicker) activity.findViewById(R.id.npMinute)).setValue(0);
        ((EditText) activity.findViewById(R.id.etLocationName)).setText("Library");
        activity.findViewById(R.id.btnCreate).performClick();
        AlertDialog chooser = (AlertDialog) ShadowDialog.getLatestDialog();
        await(() -> chooser.getListView().getAdapter().getCount() == 1);
        chooser.getListView().performItemClick(null, 0, 0);
        await(() -> mutations.size() == 2);
        await(() -> activity.findViewById(R.id.rvScheduleList) != null);
        assertEquals("POST /api/v1/chat/conversations/direct", mutations.get(0));
        assertEquals("peer", bodies.get(0).get("targetUserId").getAsString());
        assertEquals("POST /api/v1/bookings/conversation/c1", mutations.get(1));
    }
    @Test public void optionalNoteCanBeEmpty() throws Exception {
        openCreate(true);
        ((EditText) activity.findViewById(R.id.etLocationName)).setText("Library");
        activity.findViewById(R.id.btnCreate).performClick();
        await(() -> !bodies.isEmpty());
        assertEquals("", bodies.get(0).get("note").getAsString());
        await(() -> activity.findViewById(R.id.rvScheduleList) != null);
    }
    @Test public void serverNoteAppearsInDetail() throws Exception {
        booking.addProperty("note", "Bring laptop");
        android.app.Dialog dialog = detail();
        assertEquals(activity.getString(R.string.booking_note_detail, "Bring laptop"),
                ((TextView) dialog.findViewById(R.id.tvNote)).getText().toString());
        dialog.dismiss();
    }

    @Test public void failedCalendarCanRetry() throws Exception {
        failLoad = true; openSchedule();
        await(() -> activity.findViewById(R.id.progressBar).getVisibility() == View.GONE);
        assertEquals(View.VISIBLE, activity.findViewById(R.id.tvEmptyState).getVisibility());
        failLoad = false; activity.findViewById(R.id.tvEmptyState).performClick();
        await(() -> list().getAdapter().getItemCount() == 1);
    }
    @Test public void creationDateTimeAndDurationSurviveRecreation() {
        openCreate(true);
        ((Spinner) activity.findViewById(R.id.spDuration)).setSelection(4);
        ((CheckBox) activity.findViewById(R.id.cbIsRecurring)).setChecked(true);
        ((EditText) activity.findViewById(R.id.etLocationName)).setText("Library");
        ((EditText) activity.findViewById(R.id.etNote)).setText("Bring notes");
        controller.recreate(); activity = controller.get();
        activity.getSupportFragmentManager().executePendingTransactions();
        assertEquals(10, ((NumberPicker) activity.findViewById(R.id.npHour)).getValue());
        assertEquals(4, ((Spinner) activity.findViewById(R.id.spDuration)).getSelectedItemPosition());
        assertEquals("Library", ((EditText) activity.findViewById(R.id.etLocationName)).getText().toString());
        assertEquals(day.format(BookingUi.DATE), ((TextView) activity.findViewById(R.id.tvSelectedDate)).getText().toString());
        assertTrue(((CheckBox) activity.findViewById(R.id.cbIsRecurring)).isChecked());
        assertEquals("Bring notes", ((EditText) activity.findViewById(R.id.etNote)).getText().toString());
    }

    @Test public void doubleAcceptOnlySendsOneRequest() throws Exception {
        android.app.Dialog dialog = detail();
        dialog.findViewById(R.id.btnAccept).performClick();
        dialog.findViewById(R.id.btnAccept).performClick();
        await(() -> dialog.findViewById(R.id.btnComplete).getVisibility() == View.VISIBLE);
        assertEquals(Collections.singletonList("PUT /api/v1/bookings/b1/accept"), mutations);
        dialog.dismiss();
    }
    @Test public void unrelatedUserHasReadOnlyBooking() throws Exception {
        booking.addProperty("participantStatus", "NOT_INVOLVED");
        booking.addProperty("partnerId", "someone-else");
        booking.addProperty("status", "CONFIRMED");
        android.app.Dialog dialog = detail();
        assertEquals(View.GONE, dialog.findViewById(R.id.btnComplete).getVisibility());
        assertEquals(View.GONE, dialog.findViewById(R.id.btnCancel).getVisibility());
        dialog.findViewById(R.id.btnComplete).performClick();
        assertTrue(mutations.isEmpty());
        dialog.dismiss();
    }
}
