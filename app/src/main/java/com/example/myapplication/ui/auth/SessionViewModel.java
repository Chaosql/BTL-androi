package com.example.myapplication.ui.auth;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplication.ui.base.BaseViewModel;
import com.example.myapplication.ui.base.UiEvent;
import com.example.myapplication.ui.notification.PushRegistration;

public final class SessionViewModel extends BaseViewModel {
    private final MutableLiveData<UiEvent<Boolean>> signedOut = new MutableLiveData<>();
    private boolean ending;
    public SessionViewModel(Application application) { super(application); }
    public LiveData<UiEvent<Boolean>> signedOut() { return signedOut; }
    public void signOut() {
        if (ending) return;
        ending = true;
        PushRegistration.endSession(getApplication(), () -> signedOut.setValue(new UiEvent<>(true)));
    }
}
