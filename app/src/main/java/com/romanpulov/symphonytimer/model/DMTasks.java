package com.romanpulov.symphonytimer.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DMTasks implements Parcelable {
    public static final int STATUS_IDLE = 0;
    public static final int STATUS_PROCESSING = 1;
    public static final int STATUS_COMPLETED = 2;

    private final List<DMTaskItem> mDataItems = new ArrayList<>();

    public List<DMTaskItem> getItems() {
        return mDataItems;
    }

    public boolean add(DMTaskItem item) {
        return mDataItems.add(item);
    }

    public boolean remove(DMTaskItem item) {
        return mDataItems.remove(item);
    }

    public DMTaskItem remove(int location) {
        return mDataItems.remove(0);
    }

    public int size() {
        return mDataItems.size();
    }

    private boolean mLocked = false;

    public void lock() {
        mLocked = true;
    }

    public void unlock() {
        mLocked = false;
    }

    public boolean isLocked() {
        return mLocked;
    }

	public DMTaskItem getTaskItemById(long id) {
		for (DMTaskItem taskItem : mDataItems) {
			if (id == taskItem.getId()) {
				return taskItem;
			}
		}
		return null;
	}
	
	public DMTaskItem getFirstTaskItemCompleted() {
        DMTaskItem result = null;

		for (DMTaskItem taskItem : mDataItems) {
			if (taskItem.getCompleted()) {
                if (result == null)
                    result = taskItem;
				else if (taskItem.getTriggerAtTime() > result.getTriggerAtTime())
                    result = taskItem;
			}
		}
		return result;
	}
	
	public void updateProcess() {
		for (DMTaskItem dmTaskItem : mDataItems) {
			dmTaskItem.updateProcess();
		}
	}
		
	public DMTaskItem addTaskItem(DMTimerRec dmTimerRec) {
        DMTaskItem newItem = new DMTaskItem(dmTimerRec.mId, dmTimerRec.mTitle, dmTimerRec.mTimeSec, dmTimerRec.mSoundFile, dmTimerRec.mAutoTimerDisableInterval);
        newItem.setTaskItemCompleted(mTaskItemCompletedListener);
		return newItem;
	}
	
	public String getTaskTitles() {
		StringBuilder sb = new StringBuilder();
		String delimiter = "";
		for (DMTaskItem taskItem : mDataItems) {
			sb.append(delimiter);
			sb.append(taskItem.getTitle());
            sb.append("(");
            sb.append(taskItem.getExecutionPercent());
            sb.append("%)");
            delimiter = ",";
		}
		return sb.toString();
	}

    private DMTaskItem.OnTaskItemCompleted mTaskItemCompletedListener;

    public void setTasksCompleted(DMTaskItem.OnTaskItemCompleted taskItemCompletedListener) {
        mTaskItemCompletedListener = taskItemCompletedListener;
    }

	public int getExecutionPercent() {
        long currentTime = System.currentTimeMillis();
        long minStartTime = currentTime;
        long maxEndTime = minStartTime;

        for (DMTaskItem taskItem : mDataItems) {
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
            return (int)((currentTime - minStartTime) * 100/timeRange);
    }

    /**
     * Scans items and returns earliest trigger time
     * for uncompleted items
     * @return time
     */
    public long getFirstTriggerAtTime() {
        long result = Long.MAX_VALUE;
        for (DMTaskItem item : mDataItems) {
            if ((!item.getCompleted()) && (item.getTriggerAtTime() < result))
                result = item.getTriggerAtTime();
        }
        return result;
    }

    /**
     * Get items execution status
     * @return Idle, Processing or Completed status
     */
    public int getStatus() {
        if (size() == 0)
            return STATUS_IDLE;
        else if (getFirstTaskItemCompleted() == null)
            return STATUS_PROCESSING;
        else
            return STATUS_COMPLETED;
    }

    public DMTasks() {

    }

    /**
     * Creates a copy via Parcelable mechanism
     * @return new object cloned via Parcel
     */
    public DMTasks createParcelableCopy() {
        Parcel parcel = Parcel.obtain();
        try {
            writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            return CREATOR.createFromParcel(parcel);
        } finally {
            parcel.recycle();
        }
    }

    /**
     * Refreshes data items from external source preserving reference, for Adapter
     * @param newTasks new data items
     */
    public void replaceTasks(DMTasks newTasks) {
        mDataItems.clear();
        mDataItems.addAll(newTasks.mDataItems);
        for (DMTaskItem item : mDataItems) {
            item.setTaskItemCompleted(mTaskItemCompletedListener);
        }

    }

    private DMTasks(Parcel in) {
        in.readTypedList(mDataItems, DMTaskItem.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(mDataItems);
    }

    public static final Parcelable.Creator<DMTasks> CREATOR = new Parcelable.Creator<DMTasks>() {
        public DMTasks createFromParcel(Parcel in) {
            return new DMTasks(in);
        }

        public DMTasks[] newArray(int size) {
            return new DMTasks[size];
        }
    };

    @Override
    public @NotNull String toString() {
        int i = 0;
        StringBuilder sb = new StringBuilder();

        for (DMTaskItem item : mDataItems) {
            sb.append("(i=").append(i++);
            sb.append(item.toString());
            sb.append(")");
        }

        return sb.toString();
    }

    /**
     * Object serialization to JSON String
     * @return String
     */
    public String toJSONString() {
        JSONArray ja = new JSONArray();
        for (DMTaskItem item : mDataItems) {
            ja.put(item.toJSONObject());
        }
        JSONObject jo = new JSONObject();
        try {
            jo.put(DMTasks.class.getName(), ja);
            return jo.toString();
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Object deserialization from JSON String
     * @param data String
     * @return deserialized object
     */
    public static DMTasks fromJSONString(String data) {
        DMTasks result = new DMTasks();

        if ((data == null) || (data.isEmpty()))
            return result;
        else
            try {
                JSONObject jo = new JSONObject(data);
                JSONArray ja = (JSONArray) jo.get(DMTasks.class.getName());
                for (int i = 0; i < ja.length(); i++) {
                    result.add(DMTaskItem.fromJSONObject(ja.get(i)));
                }
                return result;
            } catch (JSONException e) {
                return result;
            }
    }
}
