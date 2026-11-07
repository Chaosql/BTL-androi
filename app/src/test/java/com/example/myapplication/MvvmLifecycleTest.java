package com.example.myapplication;

import android.app.Application;
import android.os.Looper;
import androidx.lifecycle.*;
import com.example.myapplication.data.local.PreferenceManager;
import com.example.myapplication.data.remote.network.RetrofitClient;
import com.example.myapplication.ui.auth.AuthViewModel;
import com.example.myapplication.ui.base.*;
import com.example.myapplication.ui.main.MainActivity;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import okhttp3.Request;
import okhttp3.mockwebserver.*;
import okio.Timeout;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import retrofit2.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class MvvmLifecycleTest {
    public static class ProbeViewModel extends BaseViewModel {
        public ProbeViewModel(Application application) { super(application); }
        UiOperation<String> load(Call<String> call) { return execute(call); }
    }
    private static class Owner implements LifecycleOwner {
        final LifecycleRegistry lifecycle = new LifecycleRegistry(this);
        @Override public Lifecycle getLifecycle() { return lifecycle; }
    }
    private static class PendingCall implements Call<String> {
        boolean started, cancelled;
        Callback<String> callback;
        @Override public void enqueue(Callback<String> callback) { started = true; this.callback = callback; }
        @Override public Response<String> execute() { throw new UnsupportedOperationException(); }
        @Override public boolean isExecuted() { return started; }
        @Override public void cancel() { cancelled = true; }
        @Override public boolean isCanceled() { return cancelled; }
        @Override public Call<String> clone() { return new PendingCall(); }
        @Override public Request request() { return new Request.Builder().url("https://example.test/").build(); }
        @Override public Timeout timeout() { return Timeout.NONE; }
    }
    @Test public void clearingViewModelCancelsPendingWork() {
        ViewModelStore store = new ViewModelStore();
        ProbeViewModel model = new ViewModelProvider(() -> store,
                new ViewModelProvider.AndroidViewModelFactory(RuntimeEnvironment.getApplication())).get(ProbeViewModel.class);
        PendingCall call = new PendingCall(); UiOperation<String> operation = model.load(call);
        assertFalse(call.started);
        Observer<UiState<String>> observer = state -> { };
        operation.observeForever(observer); assertTrue(call.started);
        store.clear();
        assertTrue(call.cancelled); assertEquals(UiState.Status.CANCELLED, operation.getValue().status);
        operation.removeObserver(observer);
    }
    @Test public void responseIsRetainedWithoutHoldingDestroyedViewObserver() {
        ProbeViewModel model = new ProbeViewModel(RuntimeEnvironment.getApplication());
        PendingCall call = new PendingCall(); UiOperation<String> operation = model.load(call);
        Owner first = new Owner(); first.lifecycle.setCurrentState(Lifecycle.State.STARTED);
        AtomicInteger staleUpdates = new AtomicInteger();
        operation.observe(first, state -> { if (state.status == UiState.Status.SUCCESS) staleUpdates.incrementAndGet(); });
        first.lifecycle.setCurrentState(Lifecycle.State.DESTROYED);
        assertFalse(call.cancelled);
        call.callback.onResponse(call, Response.success("loaded"));
        assertEquals(0, staleUpdates.get());
        Owner next = new Owner(); next.lifecycle.setCurrentState(Lifecycle.State.STARTED);
        java.util.List<String> values = new java.util.ArrayList<>();
        operation.observe(next, state -> { if (state.status == UiState.Status.SUCCESS) values.add(state.data); });
        assertEquals(java.util.Collections.singletonList("loaded"), values);
        next.lifecycle.setCurrentState(Lifecycle.State.DESTROYED);
    }
    @Test public void pendingLoginSurvivesRecreationWithoutResending() throws Exception {
        MockWebServer server = new MockWebServer(); AtomicInteger logins = new AtomicInteger();
        PreferenceManager session = new PreferenceManager(RuntimeEnvironment.getApplication()); session.logout();
        server.setDispatcher(new Dispatcher() {
            @Override public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath().contains("/auth/login")) {
                    logins.incrementAndGet();
                    return new MockResponse().setHeader("Content-Type", "application/json")
                            .setBody("{\"codeStatus\":200,\"data\":{\"id\":\"self\",\"accessToken\":\"access\",\"refreshToken\":\"refresh\"}}")
                            .setBodyDelay(500, TimeUnit.MILLISECONDS);
                }
                return new MockResponse().setHeader("Content-Type", "application/json").setBody("{\"codeStatus\":200,\"data\":0}");
            }
        });
        server.start(); RetrofitClient.setApiServiceForTests(RetrofitClient.create(server.url("/").toString(), session));
        try (var controller = Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity activity = controller.get(); activity.getSupportFragmentManager().executePendingTransactions();
            androidx.fragment.app.Fragment screen = activity.getSupportFragmentManager().findFragmentById(R.id.screenHost);
            AuthViewModel before = new ViewModelProvider(screen, new ViewModelProvider.AndroidViewModelFactory(activity.getApplication())).get(AuthViewModel.class);
            com.example.myapplication.databinding.FragmentLoginBinding form = com.example.myapplication.databinding.FragmentLoginBinding.bind(screen.requireView());
            form.edtEmail.setText("test@example.com"); form.edtPassword.setText("Password123"); form.tvLogin.performClick();
            await(() -> logins.get() == 1);
            controller.recreate(); activity = controller.get(); activity.getSupportFragmentManager().executePendingTransactions();
            screen = activity.getSupportFragmentManager().findFragmentById(R.id.screenHost);
            assertSame(before, new ViewModelProvider(screen, new ViewModelProvider.AndroidViewModelFactory(activity.getApplication())).get(AuthViewModel.class));
            MainActivity recreated = activity;
            await(() -> session.isLogin() && recreated.findViewById(R.id.mapView) != null);
            assertEquals(1, logins.get()); assertEquals("test@example.com", session.getEmail());
        } finally { RetrofitClient.setApiServiceForTests(null); session.logout(); server.shutdown(); }
    }
    private void await(BooleanSupplier check) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(8);
        while (!check.getAsBoolean() && System.nanoTime() < deadline) {
            Shadows.shadowOf(Looper.getMainLooper()).idle(); Thread.sleep(10);
        }
        assertTrue(check.getAsBoolean());
    }
}
