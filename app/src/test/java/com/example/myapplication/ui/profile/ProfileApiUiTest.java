package com.example.myapplication.ui.profile;

import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.example.myapplication.R;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.dto.response.LoginData;
import com.example.myapplication.data.remote.network.RetrofitClient;
import com.example.myapplication.ui.main.MainActivity;
import com.example.myapplication.ui.main.UiScreen;
import com.google.gson.*;
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
public class ProfileApiUiTest {
    private MockWebServer server;
    private PreferenceManager preferences;
    private ActivityController<MainActivity> controller;
    private MainActivity activity;
    private JsonObject user;
    private JsonArray subjects;
    private final List<String> mutations = Collections.synchronizedList(new ArrayList<>());
    private volatile boolean failSubject;
    private volatile boolean failAcademic;
    private volatile boolean failIntroduction;
    private volatile boolean failPassword;

    @Before public void setup() throws Exception {
        user = JsonParser.parseString("{\"id\":\"user-1\",\"email\":\"test@example.com\",\"firstName\":\"Hoang\",\"lastName\":\"Nguyen\",\"gender\":\"MALE\",\"dateOfBirth\":\"2005-08-15\",\"university\":\"School\",\"majorName\":\"Java\",\"phone\":\"0901234567\",\"location\":\"Hanoi\",\"statusTag\":\"Hello\",\"trustScore\":97.5,\"buddyActive\":false}").getAsJsonObject();
        subjects = JsonParser.parseString("[{\"id\":\"s1\",\"name\":\"Java\",\"type\":\"STRENGTH\"}]").getAsJsonArray();
        server = new MockWebServer();
        server.setDispatcher(new Dispatcher() {
            @Override public MockResponse dispatch(RecordedRequest request) {
                String path = request.getRequestUrl().encodedPath();
                if ("GET".equals(request.getMethod())) {
                    if (path.endsWith("/subjects")) return response(subjects);
                    if (path.equals("/api/v1/bookings")) return response(new JsonArray());
                    return response(user);
                }
                String body = request.getBody().readUtf8();
                mutations.add(request.getMethod() + " " + path + " " + body);
                if (path.endsWith("/avatar")) return response(user);
                if (path.endsWith("/password") && failPassword)
                    return new MockResponse().setResponseCode(422).setBody("{\"message\":\"Password rejected\"}");
                if (path.endsWith("/academic-info") && failAcademic)
                    return new MockResponse().setResponseCode(503).setBody("{\"message\":\"Try again\"}");
                if (path.endsWith("/status-tag") && failIntroduction)
                    return new MockResponse().setResponseCode(422).setBody("{\"message\":\"Rejected\"}");
                if (path.endsWith("/subjects")) {
                    if (failSubject) return new MockResponse().setResponseCode(422).setBody("{\"message\":\"Subject rejected\"}");
                    JsonObject subject = JsonParser.parseString(body).getAsJsonObject();
                    subject.addProperty("id", "s2");
                    subjects.add(subject);
                    return response(subject);
                }
                if (path.contains("/subjects/") && "DELETE".equals(request.getMethod())) {
                    subjects.remove(0);
                    return response(JsonParser.parseString("{\"status\":true}"));
                }
                JsonObject changes = JsonParser.parseString(body).getAsJsonObject();
                for (String key : changes.keySet()) user.add(key, changes.get(key));
                return response(user);
            }
        });
        server.start();
        preferences = new PreferenceManager(RuntimeEnvironment.getApplication());
        preferences.logout();
        preferences.saveLogin(new LoginData("Bearer", "token", "refresh", "user-1", null), "test@example.com");
        RetrofitClient.setApiServiceForTests(RetrofitClient.create(server.url("/").toString(), preferences));
        controller = Robolectric.buildActivity(MainActivity.class).setup();
        activity = controller.get();
        activity.getSupportFragmentManager().executePendingTransactions();
    }

    @After public void cleanup() throws Exception {
        if (controller != null) controller.close();
        RetrofitClient.setApiServiceForTests(null);
        preferences.logout();
        server.shutdown();
    }

    private MockResponse response(JsonElement data) {
        JsonObject wrapper = new JsonObject();
        wrapper.addProperty("codeStatus", 200);
        wrapper.add("data", data.deepCopy());
        return new MockResponse().setHeader("Content-Type", "application/json").setBody(wrapper.toString());
    }

    private void open(UiScreen screen) throws Exception {
        activity.showScreen(screen);
        activity.getSupportFragmentManager().executePendingTransactions();
        await(() -> activity.findViewById(R.id.tvUsername) != null &&
                ((TextView) activity.findViewById(R.id.tvUsername)).getText().toString().equals("Nguyen Hoang"));
    }

    private void await(BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
            Shadows.shadowOf(Looper.getMainLooper()).idle();
            Thread.sleep(10);
        }
        assertTrue("Timed out waiting for profile API", condition.getAsBoolean());
    }

    @Test public void profileRendersServerDataAndStatistics() throws Exception {
        open(UiScreen.PROFILE);
        await(() -> ((TextView) activity.findViewById(R.id.tvSubjectCount)).getText().toString().equals("1"));
        await(() -> ((TextView) activity.findViewById(R.id.tvAppointmentCount)).getText().toString().equals("0"));
        assertEquals("Hello", ((TextView) activity.findViewById(R.id.tvIntroduction)).getText().toString());
        assertTrue(((TextView) activity.findViewById(R.id.tvSchool)).getText().toString().contains("School"));
        assertEquals("97.5", ((TextView) activity.findViewById(R.id.tvFavoriteRating)).getText().toString());
    }

    @Test public void viewingProfileRequiresEditScreenForPersonalChanges() throws Exception {
        open(UiScreen.PROFILE);
        assertFalse(activity.findViewById(R.id.cvInfo).hasOnClickListeners());
        assertFalse(activity.findViewById(R.id.tvBuddyStatus).hasOnClickListeners());
        assertFalse(activity.findViewById(R.id.cvInfo).performClick());
        assertFalse(activity.findViewById(R.id.tvBuddyStatus).performClick());
        assertTrue(mutations.isEmpty());
        activity.findViewById(R.id.btnUpdateProfile).performClick();
        activity.getSupportFragmentManager().executePendingTransactions();
        await(() -> activity.findViewById(R.id.tvAddInfo) != null);
        assertTrue(activity.findViewById(R.id.cvInfo).hasOnClickListeners());
        assertFalse(activity.findViewById(R.id.tvBuddyStatus).hasOnClickListeners());
    }

    @Test public void radarCanOnlyBeChangedFromAccountSettings() throws Exception {
        activity.showScreen(UiScreen.ACCOUNT_SETTINGS);
        activity.getSupportFragmentManager().executePendingTransactions();
        com.google.android.material.materialswitch.MaterialSwitch radar = activity.findViewById(R.id.switchRadar);
        await(radar::isEnabled);
        assertFalse(radar.isChecked());
        radar.performClick();
        await(() -> radar.isEnabled() && user.get("buddyActive").getAsBoolean());
        assertTrue(radar.isChecked());
        radar.performClick();
        await(() -> radar.isEnabled() && !user.get("buddyActive").getAsBoolean());
        assertFalse(radar.isChecked());
        assertEquals(2, mutations.size());
    }

    @Test public void introductionUpdatesApiAndVisibleProfile() throws Exception {
        open(UiScreen.EDIT_PROFILE);
        activity.findViewById(R.id.tvIntroduction).performClick();
        android.app.Dialog dialog = ShadowDialog.getLatestDialog();
        ((EditText) dialog.findViewById(R.id.etStatusTag)).setText("Study together");
        dialog.findViewById(R.id.btnSave).performClick();
        await(() -> !dialog.isShowing());
        await(() -> ((TextView) activity.findViewById(R.id.tvIntroduction)).getText().toString().equals("Study together"));
        assertTrue(mutations.get(0).contains("/profile/user-1/status-tag"));
    }

    @Test public void failedIntroductionKeepsDialogAndInput() throws Exception {
        failIntroduction = true;
        open(UiScreen.EDIT_PROFILE);
        activity.findViewById(R.id.tvIntroduction).performClick();
        android.app.Dialog dialog = ShadowDialog.getLatestDialog();
        ((EditText) dialog.findViewById(R.id.etStatusTag)).setText("Keep this text");
        dialog.findViewById(R.id.btnSave).performClick();
        await(() -> dialog.findViewById(R.id.btnSave).isEnabled());
        assertTrue(dialog.isShowing());
        assertEquals(View.VISIBLE, dialog.findViewById(R.id.tvFormError).getVisibility());
        assertEquals("Keep this text", ((EditText) dialog.findViewById(R.id.etStatusTag)).getText().toString());
        assertTrue(dialog.findViewById(R.id.btnCancel).isEnabled());
        dialog.dismiss();
    }

    @Test public void personalInfoValidatesAndSavesThreeEndpoints() throws Exception {
        open(UiScreen.EDIT_PROFILE);
        activity.findViewById(R.id.tvAddInfo).performClick();
        android.app.Dialog dialog = ShadowDialog.getLatestDialog();
        EditText firstName = dialog.findViewById(R.id.etFirstName);
        firstName.setText("");
        dialog.findViewById(R.id.btnSave).performClick();
        assertNull(firstName.getError());
        assertEquals(View.VISIBLE, dialog.findViewById(R.id.tvErrorFirstName).getVisibility());
        assertEquals(0, mutations.size());
        firstName.setText("Updated");
        ((EditText) dialog.findViewById(R.id.etDateOfBirth)).setText("31/02/2005");
        dialog.findViewById(R.id.btnSave).performClick();
        assertEquals(0, mutations.size());
        ((EditText) dialog.findViewById(R.id.etDateOfBirth)).setText("15/08/2005");
        ((EditText) dialog.findViewById(R.id.etUniversity)).setText("New school");
        ((EditText) dialog.findViewById(R.id.etLocation)).setText("New city");
        dialog.findViewById(R.id.btnSave).performClick();
        await(() -> !dialog.isShowing());
        assertEquals(3, mutations.size());
        assertTrue(mutations.get(0).contains("personal-info"));
        assertTrue(mutations.get(0).contains("2005-08-15"));
        assertTrue(mutations.get(1).contains("academic-info"));
        assertTrue(mutations.get(2).contains("/location"));
        await(() -> ((TextView) activity.findViewById(R.id.tvUsername)).getText().toString().equals("Nguyen Updated"));
    }

    @Test public void partialSaveKeepsFormAndAllowsRetry() throws Exception {
        failAcademic = true;
        open(UiScreen.EDIT_PROFILE);
        activity.findViewById(R.id.tvAddInfo).performClick();
        android.app.Dialog dialog = ShadowDialog.getLatestDialog();
        ((EditText) dialog.findViewById(R.id.etFirstName)).setText("Updated");
        ((EditText) dialog.findViewById(R.id.etUniversity)).setText("New school");
        dialog.findViewById(R.id.btnSave).performClick();
        await(() -> dialog.findViewById(R.id.btnSave).isEnabled());
        assertTrue(dialog.isShowing());
        assertTrue(dialog.findViewById(R.id.etFirstName).isEnabled());
        assertEquals(View.VISIBLE, dialog.findViewById(R.id.tvFormError).getVisibility());
        failAcademic = false;
        dialog.findViewById(R.id.btnSave).performClick();
        await(() -> !dialog.isShowing());
        assertEquals("New school", user.get("university").getAsString());
    }

    @Test public void subjectAddRejectsDuplicateThenRefreshesManagementList() throws Exception {
        new SubjectManagementDialog(activity).show();
        android.app.Dialog management = ShadowDialog.getLatestDialog();
        await(() -> management.findViewById(R.id.btnAddSubject).isEnabled());
        management.findViewById(R.id.btnAddSubject).performClick();
        android.app.Dialog add = ShadowDialog.getLatestDialog();
        await(() -> add.findViewById(R.id.btnConfirmAdd).isEnabled());
        EditText name = add.findViewById(R.id.etSubjectName);
        add.findViewById(R.id.btnConfirmAdd).performClick();
        assertEquals(activity.getString(R.string.profile_subject_required), ((TextView) add.findViewById(R.id.tvSubjectError)).getText().toString());
        assertEquals(0, mutations.size());
        name.setText(" java ");
        assertEquals(View.GONE, add.findViewById(R.id.tvSubjectError).getVisibility());
        add.findViewById(R.id.btnConfirmAdd).performClick();
        assertNull(name.getError());
        assertEquals(View.VISIBLE, add.findViewById(R.id.tvSubjectError).getVisibility());
        assertEquals(activity.getString(R.string.profile_duplicate_subject), ((TextView) add.findViewById(R.id.tvSubjectError)).getText().toString());
        assertEquals(0, mutations.size());
        name.setText("Android");
        add.findViewById(R.id.rbReview).performClick();
        add.findViewById(R.id.btnConfirmAdd).performClick();
        await(() -> !add.isShowing());
        await(() -> management.findViewById(R.id.btnAddSubject).isEnabled());
        assertEquals(2, subjects.size());
        assertTrue(mutations.get(0).contains("NEED_REVIEW"));
        management.dismiss();
    }

    @Test public void subjectApiFailureStaysInlineAndAllowsRetry() throws Exception {
        new AddSubjectDialog(activity).show();
        android.app.Dialog dialog = ShadowDialog.getLatestDialog();
        await(() -> dialog.findViewById(R.id.btnConfirmAdd).isEnabled());
        EditText name = dialog.findViewById(R.id.etSubjectName);
        name.setText("Android");
        failSubject = true;
        dialog.findViewById(R.id.btnConfirmAdd).performClick();
        await(() -> dialog.findViewById(R.id.tvSubjectError).getVisibility() == View.VISIBLE);
        assertEquals("Subject rejected", ((TextView) dialog.findViewById(R.id.tvSubjectError)).getText().toString());
        assertTrue(dialog.isShowing());
        assertTrue(dialog.findViewById(R.id.btnConfirmAdd).isEnabled());
        assertTrue(dialog.findViewById(R.id.btnCancelAdd).isEnabled());
        assertEquals(1, subjects.size());
        name.setText("Android SDK");
        assertEquals(View.GONE, dialog.findViewById(R.id.tvSubjectError).getVisibility());
        failSubject = false;
        dialog.findViewById(R.id.btnConfirmAdd).performClick();
        await(() -> !dialog.isShowing());
        assertEquals(2, subjects.size());
    }

    @Test public void subjectDeleteUsesServerIdAfterConfirmation() throws Exception {
        new SubjectManagementDialog(activity).show();
        android.app.Dialog management = ShadowDialog.getLatestDialog();
        await(() -> management.findViewById(R.id.btnDeleteSubject) != null);
        management.findViewById(R.id.btnDeleteSubject).performClick();
        AlertDialog confirmation = (AlertDialog) ShadowDialog.getLatestDialog();
        confirmation.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        await(() -> subjects.size() == 0);
        await(() -> management.findViewById(R.id.btnAddSubject).isEnabled());
        assertTrue(mutations.get(0).startsWith("DELETE /api/v1/profile/user-1/subjects/s1"));
        management.dismiss();
    }

    @Test public void registrationInfoPreservesReadOnlyIdentifiers() throws Exception {
        activity.showScreen(UiScreen.REGISTRATION_INFO);
        activity.getSupportFragmentManager().executePendingTransactions();
        await(() -> activity.findViewById(R.id.btnSave).isEnabled());
        assertNull(((EditText) activity.findViewById(R.id.etEmail)).getKeyListener());
        assertNull(((EditText) activity.findViewById(R.id.etId)).getKeyListener());
        ((EditText) activity.findViewById(R.id.etFirstName)).setText("New name");
        activity.findViewById(R.id.btnSave).performClick();
        await(() -> activity.findViewById(R.id.btnSave).isEnabled());
        assertTrue(mutations.get(0).contains("test@example.com"));
        assertTrue(mutations.get(0).contains("0901234567"));
    }

    @Test public void changePasswordUsesInlineErrorsAndClearsThemOnEdit() throws Exception {
        activity.showScreen(UiScreen.CHANGE_PASSWORD);
        activity.getSupportFragmentManager().executePendingTransactions();
        EditText old = activity.findViewById(R.id.etOldPassword);
        EditText next = activity.findViewById(R.id.etNewPassword);
        EditText confirm = activity.findViewById(R.id.etConfirmPassword);
        activity.findViewById(R.id.btnOk).performClick();
        assertEquals(View.VISIBLE, activity.findViewById(R.id.tvOldPasswordError).getVisibility());
        assertNull(old.getError());
        old.setText("oldpass123");
        assertEquals(View.GONE, activity.findViewById(R.id.tvOldPasswordError).getVisibility());
        next.setText("short");
        activity.findViewById(R.id.btnOk).performClick();
        assertEquals(View.VISIBLE, activity.findViewById(R.id.tvNewPasswordError).getVisibility());
        assertNull(next.getError());
        next.setText("newpass123"); confirm.setText("different123");
        activity.findViewById(R.id.btnOk).performClick();
        assertEquals(View.VISIBLE, activity.findViewById(R.id.tvConfirmPasswordError).getVisibility());
        assertTrue(mutations.isEmpty());
        confirm.setText("newpass123"); failPassword = true;
        activity.findViewById(R.id.btnOk).performClick();
        await(() -> activity.findViewById(R.id.btnOk).isEnabled());
        assertEquals(View.VISIBLE, activity.findViewById(R.id.tvFormError).getVisibility());
        assertEquals("Password rejected", ((TextView) activity.findViewById(R.id.tvFormError)).getText().toString());
        assertEquals("newpass123", next.getText().toString());
        old.setText("correctold123");
        assertEquals(View.GONE, activity.findViewById(R.id.tvFormError).getVisibility());
    }

    @Test public void avatarPickerUploadsMultipartImage() throws Exception {
        open(UiScreen.EDIT_PROFILE);
        android.net.Uri uri = android.net.Uri.parse("content://test.images/avatar.png");
        org.robolectric.shadows.ShadowContentResolver.registerProviderInternal("test.images", new ImageProvider());
        java.io.ByteArrayOutputStream data = new java.io.ByteArrayOutputStream();
        android.graphics.Bitmap image = android.graphics.Bitmap.createBitmap(4, 4, android.graphics.Bitmap.Config.ARGB_8888);
        image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, data);
        image.recycle();
        Shadows.shadowOf(activity.getContentResolver()).registerInputStream(uri, new java.io.ByteArrayInputStream(data.toByteArray()));
        activity.findViewById(R.id.imgEditAvatar).performClick();
        org.robolectric.shadows.ShadowActivity.IntentForResult picker = Shadows.shadowOf(activity).getNextStartedActivityForResult();
        assertNotNull(picker);
        activity.getActivityResultRegistry().dispatchResult(picker.requestCode, android.app.Activity.RESULT_OK,
                new android.content.Intent().setData(uri));
        await(() -> !mutations.isEmpty());
        assertTrue(mutations.get(0).startsWith("POST /api/v1/profile/user-1/avatar"));
        assertTrue(mutations.get(0).contains("name=\"file\"; filename=\"avatar.png\""));
        assertTrue(mutations.get(0).contains("Content-Type: image/png"));
    }

    @Test public void oversizedAvatarIsRejectedBeforeHttpRequest() throws Exception {
        android.net.Uri uri = android.net.Uri.parse("content://test.images/avatar.png");
        org.robolectric.shadows.ShadowContentResolver.registerProviderInternal("test.images", new ImageProvider());
        Shadows.shadowOf(activity.getContentResolver()).registerInputStream(uri,
                new java.io.ByteArrayInputStream(new byte[AvatarImage.MAX_BYTES + 1]));
        assertThrows(java.io.IOException.class, () -> AvatarImage.read(activity.getContentResolver(), uri));
        assertTrue(mutations.isEmpty());
    }

    public static class ImageProvider extends android.content.ContentProvider {
        @Override public boolean onCreate() { return true; }
        @Override public String getType(android.net.Uri uri) { return "image/png"; }
        @Override public android.database.Cursor query(android.net.Uri uri, String[] projection, String selection,
                String[] arguments, String sort) { return null; }
        @Override public android.net.Uri insert(android.net.Uri uri, android.content.ContentValues values) { return null; }
        @Override public int delete(android.net.Uri uri, String selection, String[] arguments) { return 0; }
        @Override public int update(android.net.Uri uri, android.content.ContentValues values, String selection, String[] arguments) { return 0; }
    }
}
