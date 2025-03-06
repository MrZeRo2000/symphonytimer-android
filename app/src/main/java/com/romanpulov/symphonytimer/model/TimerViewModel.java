package com.romanpulov.symphonytimer.model;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.romanpulov.symphonytimer.helper.db.DBHelper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TimerViewModel extends AndroidViewModel {
    private static final String TAG = TimerViewModel.class.getSimpleName();
    private final MutableLiveData<List<DMTimerRec>> mDMTimers = new MutableLiveData<>();

    public TimerViewModel(@NotNull Application application) {
        super(application);
        Log.d(TAG, "TimerViewModel created");
    }

    @Override
    protected void onCleared() {
        Log.d(TAG, "TimerViewModel cleared");
        super.onCleared();
    }

    private DBHelper getDBHelper() {
        return DBHelper.getInstance(getApplication());
    }

    public void loadTimers() {
        mDMTimers.postValue(getDBHelper().getTimers());
    }

    public LiveData<List<DMTimerRec>> getDMTimers() {
        return mDMTimers;
    }

    public void addTimer(DMTimerRec item) {
        getDBHelper().insertTimer(item);
        loadTimers();
    }
}
