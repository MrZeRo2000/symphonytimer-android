package com.romanpulov.symphonytimer.model;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.romanpulov.symphonytimer.helper.db.DBHelper;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TimerHistoryViewModel extends AndroidViewModel {

    private final MutableLiveData<Integer> mFilterId = new MutableLiveData<>(0);

    public LiveData<Integer> getFilterId() {
        return mFilterId;
    }

    public void setFilterId(int filterId) {
        if (!Objects.equals(mFilterId.getValue(), filterId)) {
            mFilterId.postValue(filterId);
        }
    }

    private int getCurrentFilterId() {
        return Optional.ofNullable(mFilterId.getValue()).orElse(0);
    }

    private MutableLiveData<List<DMTimerHistRec>> mDMTimerHistList;

    public LiveData<List<DMTimerHistRec>> getDMTimerHistList() {
        if (mDMTimerHistList == null) {
            mDMTimerHistList = new MutableLiveData<>();
        }
        return mDMTimerHistList;
    }

    public void loadDMTimerHistList() {
        List<DMTimerHistRec> histList = DBHelper
                .getInstance(getApplication().getApplicationContext())
                .getHistList(getCurrentFilterId());
        if (mDMTimerHistList == null) {
            mDMTimerHistList = new MutableLiveData<>();
        }
        mDMTimerHistList.postValue(histList);
    }

    private MutableLiveData<List<DMTimerExecutionRec>> mDMTimerExecutionList;

    public LiveData<List<DMTimerExecutionRec>> getDMTimerExecutionList() {
        if (mDMTimerExecutionList == null) {
            mDMTimerExecutionList = new MutableLiveData<>();
        }
        return mDMTimerExecutionList;
    }

    public void loadDMTimerExecutionList() {
        List<DMTimerExecutionRec> histList = DBHelper
                .getInstance(getApplication().getApplicationContext())
                .getHistTopList(getCurrentFilterId());
        if (mDMTimerExecutionList == null) {
            mDMTimerExecutionList = new MutableLiveData<>();
        }
        mDMTimerExecutionList.postValue(histList);
    }

    private MutableLiveData<List<LinkedHashMap<Long, Long>>> mHistList;

    public LiveData<List<LinkedHashMap<Long, Long>>> getHistList() {
        if (mHistList == null) {
            mHistList = new MutableLiveData<>();
        }
        return mHistList;
    }

    public void loadHistList() {
        List<LinkedHashMap<Long, Long>> histList = DBHelper
                .getInstance(getApplication().getApplicationContext())
                .getHistList(getCurrentFilterId(), 2);
        if (mHistList == null) {
            mHistList = new MutableLiveData<>();
        }
        mHistList.postValue(histList);
    }

    public TimerHistoryViewModel(@NotNull Application application) {
        super(application);
    }
}
