package com.romanpulov.symphonytimer.model;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.romanpulov.symphonytimer.helper.db.DBHelper;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Roman Pulov on 28.02.2025
 * ViewModel for main timer
 */
public class TimerViewModel extends AndroidViewModel {
    private static final String TAG = TimerViewModel.class.getSimpleName();

    private static final String TASKS_PREFS_NAME = "task_service_prefs";
    private static final String TASKS_VALUE_NAME = "tasks";
    private static final String TASKS_KEY = "key";
    private static final String TASKS_VALUE = "value";

    public static final int TASKS_STATUS_IDLE = 0;
    public static final int TASKS_STATUS_PROCESSING = 1;
    public static final int TASKS_STATUS_UPDATE_PROCESSING = 2;
    public static final int TASKS_STATUS_COMPLETED = 3;
    public static final int TASKS_STATUS_UPDATE_COMPLETED = 4;

    private static TimerViewModel instance;

    public static synchronized TimerViewModel getInstance(Application application) {
        if (instance == null) {
            instance = new TimerViewModel(application);
        }
        return instance;
    }

    private final MutableLiveData<List<DMTimerRec>> mDMTimers = new MutableLiveData<>();
    private final MutableLiveData<Map<Long, DMTaskItem>> mDMTaskMap = new MutableLiveData<>();
    private final MutableLiveData<Pair<Integer, Integer>> mTaskStatusChange =
            new MutableLiveData<>(Pair.create(TASKS_STATUS_IDLE, TASKS_STATUS_IDLE));

    public TimerViewModel(@NotNull Application application) {
        super(application);
        Log.d(TAG, "TimerViewModel created");

        SharedPreferences prefs = getApplication().getApplicationContext()
                .getSharedPreferences(TASKS_PREFS_NAME,  Context.MODE_PRIVATE);
        String storedTasksString = prefs.getString(TASKS_VALUE_NAME, "");
        if (!storedTasksString.isEmpty()) {
            try {
                Map<Long, DMTaskItem> storedTasks = tasksFromJSONString(storedTasksString);
                setTasks(storedTasks);
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing tasks json", e);
                prefs.edit().remove(TASKS_VALUE_NAME).apply();
            }
        }
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

    private void setTasks(@Nullable Map<Long, DMTaskItem> value) {
        // update progress
        Map<Long, DMTaskItem> updatedValue = null;
        if (value != null) {
            updatedValue = value.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().createUpdated()));
        }

        // check for changes
        int tasksStatus = calcTasksStatus(mDMTaskMap.getValue(), updatedValue);
        // post value
        mDMTaskMap.postValue(updatedValue);
        // post changes
        if (tasksStatus != getCurrentTasksStatus()) {
            mTaskStatusChange.postValue(Pair.create(getCurrentTasksStatus(), tasksStatus));
        }
        // perform actions on tasks update
        afterTasksUpdated(updatedValue);
    }

    public void afterTasksUpdated(Map<Long, DMTaskItem> tasks) {
        SharedPreferences prefs = getApplication().getApplicationContext()
                .getSharedPreferences(TASKS_PREFS_NAME,  Context.MODE_PRIVATE);
        if (tasks == null) {
            prefs.edit().remove(TASKS_VALUE_NAME).apply();
        } else {
            try {
                prefs.edit().putString(TASKS_VALUE_NAME, tasksToJSONString(tasks)).apply();
            } catch (JSONException e) {
                Log.e(TAG, "JSONException", e);
                prefs.edit().remove(TASKS_VALUE_NAME).apply();
            }
        }
    }

    public static String tasksToJSONString(@NotNull Map<Long, DMTaskItem> tasks) throws JSONException {
        JSONArray ja = new JSONArray();
        for (Map.Entry<Long, DMTaskItem> v: tasks.entrySet()) {
            JSONObject jvo = new JSONObject();
            jvo.put(TASKS_KEY, v.getKey());
            jvo.put(TASKS_VALUE, v.getValue().toJSONObject());
            ja.put(jvo);
        }
        JSONObject jo = new JSONObject();
        jo.put(TASKS_VALUE_NAME, ja);

        return jo.toString();
    }

    public static Map<Long, DMTaskItem> tasksFromJSONString(String json) throws JSONException {
        if ((json == null) || (json.isEmpty())) {
            return null;
        } else {
            Map<Long, DMTaskItem> result = new HashMap<>();

            JSONObject jo = new JSONObject(json);
            JSONArray ja = (JSONArray) jo.get(TASKS_VALUE_NAME);
            for (int i = 0; i < ja.length(); i++) {
                JSONObject jvo = (JSONObject)ja.get(i);
                result.put((long) jvo.getInt(TASKS_KEY), DMTaskItem.fromJSONObject(jvo.get(TASKS_VALUE)));
            }

            return result;
        }
    }

    public void updateTasks() {
        setTasks(mDMTaskMap.getValue());
    }

    public synchronized void addTask(DMTimerRec item) {
        Map<Long, DMTaskItem> tasks = mDMTaskMap.getValue();
        if (tasks == null) {
            tasks = new HashMap<>();
        }

        if (!tasks.containsKey(item.getId())) {
            DMTaskItem newTask = new DMTaskItem(
                    item.getId(),
                    item.getTitle(),
                    item.getTimeSec(),
                    item.getSoundFile(),
                    item.getAutoTimerDisableInterval());
            newTask.startProcess();

            tasks.put(item.getId(), newTask);
            setTasks(tasks);
        }
    }

    public synchronized void removeTask(long id) {
        Map<Long, DMTaskItem> tasks = mDMTaskMap.getValue();
        if (tasks != null) {
            Log.d(TAG, "Removing task: " + id);
            tasks.remove(id);
            setTasks(tasks.isEmpty() ? null : tasks);
        }
    }

    private int calcTasksStatus(Map<Long, DMTaskItem> oldValue, Map<Long, DMTaskItem> newValue) {
        DMTaskItem oldCompleted = getFirstTaskItemCompleted(oldValue);
        DMTaskItem newCompleted = getFirstTaskItemCompleted(newValue);
        if (newValue == null) {
            return TASKS_STATUS_IDLE;
        } else if ((oldCompleted == null) && (newCompleted == null)) {
            if ((oldValue == null) || (oldValue.size() != newValue.size())) {
                return TASKS_STATUS_UPDATE_PROCESSING;
            } else {
                return TASKS_STATUS_PROCESSING;
            }
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

    public long getFirstTriggerAtTime() {
        Map<Long, DMTaskItem> value = mDMTaskMap.getValue();
        if (value == null) {
            return Long.MAX_VALUE;
        } else {
            return value
                    .values()
                    .stream()
                    .filter(v -> !v.getCompleted())
                    .map(DMTaskItem::getTriggerAtTime)
                    .min(Long::compareTo)
                    .orElse(Long.MAX_VALUE);
        }
    }

    public int getCurrentTasksStatus() {
        return Optional.ofNullable(mTaskStatusChange.getValue())
                .orElse(Pair.create(TASKS_STATUS_IDLE, TASKS_STATUS_IDLE))
                .second;
    }

    public int getExecutionPercent() {
        Map<Long, DMTaskItem> tasks = mDMTaskMap.getValue();
        if (tasks == null) {
            return 0;
        } else {
            long currentTime = System.currentTimeMillis();
            long minStartTime = currentTime;
            long maxEndTime = minStartTime;

            for (DMTaskItem taskItem : tasks.values()) {
                long startTime = taskItem.getStartTime();
                if (startTime < minStartTime)
                    minStartTime = startTime;
                long endTime = taskItem.getTriggerAtTime();
                if (endTime > maxEndTime)
                    maxEndTime = endTime;
            }
            long timeRange = maxEndTime - minStartTime;
            if (timeRange == 0)
                return 0;
            else
                return (int) ((currentTime - minStartTime) * 100 / timeRange);
        }
    }

    public String getTaskTitles() {
        Map<Long, DMTaskItem> tasks = mDMTaskMap.getValue();
        if (tasks == null) {
            return "";
        } else {
            return tasks
                    .values()
                    .stream()
                    .sorted(Comparator.comparingLong(DMTaskItem::getId))
                    .map(v -> String.format(Locale.getDefault(), "%s(%d%%)", v.getTitle(), v.getExecutionPercent()))
                    .collect(Collectors.joining(","));
        }
    }
}
