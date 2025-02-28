package com.romanpulov.symphonytimer.model;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.romanpulov.symphonytimer.helper.db.DBHelper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TimerViewModel extends AndroidViewModel {
    private final DBHelper mDBHelper;
    private final MutableLiveData<List<DMTimerRec>> mDMTimers = new MutableLiveData<>();

    public TimerViewModel(@NotNull Application application) {
        super(application);
        mDBHelper = DBHelper.getInstance(application.getApplicationContext());
    }

    public void loadTimers() {
        mDMTimers.postValue(mDBHelper.getTimers());
    }

    public LiveData<List<DMTimerRec>> getDMTimers() {
        return mDMTimers;
    }
}
