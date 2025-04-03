package com.romanpulov.symphonytimer.model;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.romanpulov.symphonytimer.helper.db.DBHelper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class TimerHistoryViewModel extends AndroidViewModel {

    private final MutableLiveData<Integer> mFilterId = new MutableLiveData<>(0);

    public LiveData<Integer> getFilterId() {
        return mFilterId;
    }

    private MutableLiveData<List<DMTimerHistRec>> mDMTimerHistList;

    public LiveData<List<DMTimerHistRec>> getDMTimerHistList() {
        if (mDMTimerHistList == null) {
            mDMTimerHistList = new MutableLiveData<>();
            loadDMTimerHistList();
        }
        return mDMTimerHistList;
    }

    public void loadDMTimerHistList() {
        List<DMTimerHistRec> histList = DBHelper
                .getInstance(getApplication().getApplicationContext())
                .getHistList(Optional.ofNullable(mFilterId.getValue()).orElse(0));
        mDMTimerHistList.postValue(histList);
    }

    public TimerHistoryViewModel(@NotNull Application application) {
        super(application);
    }
}
