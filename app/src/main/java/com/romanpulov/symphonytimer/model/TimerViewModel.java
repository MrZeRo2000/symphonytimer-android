package com.romanpulov.symphonytimer.model;

import android.app.Application;
import android.util.Log;
import android.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.romanpulov.symphonytimer.helper.db.DBHelper;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TimerViewModel extends AndroidViewModel {
    private static final String TAG = TimerViewModel.class.getSimpleName();

    public static final int TASKS_STATUS_IDLE = 0;
    public static final int TASKS_STATUS_PROCESSING = 1;
    public static final int TASKS_STATUS_COMPLETED = 2;
    public static final int TASKS_STATUS_UPDATE_COMPLETED = 3;

    private final MutableLiveData<List<DMTimerRec>> mDMTimers = new MutableLiveData<>();
    private final MutableLiveData<Map<Long, DMTaskItem>> mDMTaskMap = new MutableLiveData<>();
    private final MutableLiveData<Pair<Integer, Integer>> mTaskStatusChange =
            new MutableLiveData<>(Pair.create(TASKS_STATUS_IDLE, TASKS_STATUS_IDLE));

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

    public LiveData<Pair<Integer, Integer>> getTaskStatusChange() {
        return mTaskStatusChange;
    }

    public void setDMTaskMap(Map<Long, DMTaskItem> map) {
        mDMTaskMap.postValue(map);
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

    public void moveTimerUp(DMTimerRec item) {
        getDBHelper().moveTimerUp(item.getOrderId());
        loadTimers();
    }

    public void moveTimerDown(DMTimerRec item) {
        getDBHelper().moveTimerDown(item.getOrderId());
        loadTimers();
    }

    public void setTasks(Map<Long, DMTaskItem> value) {
        if ((value != null) && !value.isEmpty()) {
            // update progress
            value.values().forEach(DMTaskItem::updateProcess);
            // check for changes
            int tasksStatus = calcTasksStatus(mDMTaskMap.getValue(), value);
            // post value
            mDMTaskMap.postValue(value);
            // post changes
            if (tasksStatus != getCurrentTasksStatus()) {
                mTaskStatusChange.postValue(Pair.create(getCurrentTasksStatus(), tasksStatus));
            }
        }
    }

    public void updateTasks() {
        setTasks(mDMTaskMap.getValue());
    }

    public synchronized void addTask(DMTimerRec item) {
        Map<Long, DMTaskItem> tasks = mDMTaskMap.getValue();
        if (tasks != null) {
            Log.d(TAG, "Adding task: " + item.getId());
            DMTaskItem newTask = tasks.computeIfAbsent(item.getId(), id -> new DMTaskItem(
                    item.getId(),
                    item.getTitle(),
                    item.getTimeSec(),
                    item.getSoundFile(),
                    item.getAutoTimerDisableInterval()));
            newTask.startProcess();
            setTasks(tasks);
        }
    }

    public synchronized void removeTask(DMTimerRec item) {
        Map<Long, DMTaskItem> tasks = mDMTaskMap.getValue();
        if (tasks != null) {
            Log.d(TAG, "Removing task: " + item.getId());
            tasks.remove(item.getId());
            setTasks(tasks);
        }
    }

    private int calcTasksStatus(Map<Long, DMTaskItem> oldValue, Map<Long, DMTaskItem> newValue) {
        DMTaskItem oldCompleted = getFirstTaskItemCompleted(oldValue);
        DMTaskItem newCompleted = getFirstTaskItemCompleted(newValue);
        if (newValue.isEmpty()) {
            return TASKS_STATUS_IDLE;
        } else if ((oldCompleted == null) && (newCompleted == null)) {
            return TASKS_STATUS_PROCESSING;
        } else if ((oldCompleted != null) && (newCompleted != null) && (oldCompleted.getId() != newCompleted.getId())) {
            return TASKS_STATUS_UPDATE_COMPLETED;
        } else {
            return TASKS_STATUS_COMPLETED;
        }
    }

    public DMTaskItem getFirstTaskItemCompleted(Map<Long, DMTaskItem> value) {
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

    public int getCurrentTasksStatus() {
        return Optional.ofNullable(mTaskStatusChange.getValue())
                .orElse(Pair.create(TASKS_STATUS_IDLE, TASKS_STATUS_IDLE))
                .second;
    }
}
