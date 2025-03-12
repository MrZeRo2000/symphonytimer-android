package com.romanpulov.symphonytimer.model;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.romanpulov.symphonytimer.helper.db.DBHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class TimerViewModel extends AndroidViewModel {
    private static final String TAG = TimerViewModel.class.getSimpleName();

    public static final int TASKS_STATUS_IDLE = 0;
    public static final int TASKS_STATUS_PROCESSING = 1;
    public static final int TASKS_STATUS_COMPLETED = 2;

    private final MutableLiveData<List<DMTimerRec>> mDMTimers = new MutableLiveData<>();
    private final MutableLiveData<Map<Long,DMTaskItem>> mDMTaskMap = new MutableLiveData<>();

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

    public LiveData<Map<Long, DMTaskItem>> getDMTaskMap() {
        return mDMTaskMap;
    }

    public void setDMTaskMap(Map<Long, DMTaskItem> map) {
        mDMTaskMap.setValue(map);
    }

    public void addTimer(DMTimerRec item) {
        getDBHelper().insertTimer(item);
        loadTimers();
    }

    public void editTimer(DMTimerRec item) {
        getDBHelper().updateTimer(item);
        loadTimers();
    }

    public void deleteTimer(DMTimerRec item) {
        getDBHelper().deleteTimer(item.getId());
        loadTimers();
    }

    public void updateTasks() {
        Map<Long,DMTaskItem> value = mDMTaskMap.getValue();
        if ((value != null) && !value.isEmpty()) {
            value.values().forEach(DMTaskItem::updateProcess);
            mDMTaskMap.setValue(value);
        }
    }

    public void updateTasksAsync() {
        Map<Long,DMTaskItem> value = mDMTaskMap.getValue();
        if ((value != null) && !value.isEmpty()) {
            value.values().forEach(DMTaskItem::updateProcess);
            mDMTaskMap.postValue(value);
        }
    }

    public DMTaskItem getFirstTaskItemCompleted() {
        Map<Long,DMTaskItem> value = mDMTaskMap.getValue();
        if (value == null || value.isEmpty()) {
            return null;
        } else {
            return value
                    .values()
                    .stream()
                    .filter(DMTaskItem::getCompleted)
                    .min(Comparator.comparingLong(DMTaskItem::getTriggerAtTime))
                    .orElse(null);
        }

    }

    public int getTasksStatus() {
        Map<Long,DMTaskItem> value = mDMTaskMap.getValue();
        if (value == null || value.isEmpty()) {
            return TASKS_STATUS_IDLE;
        } else if (getFirstTaskItemCompleted() == null)
            return TASKS_STATUS_PROCESSING;
        else
            return TASKS_STATUS_COMPLETED;
    }
}
