package com.romanpulov.symphonytimer.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class DMTasks implements Parcelable {
    public static final int STATUS_IDLE = 0;
    public static final int STATUS_PROCESSING = 1;
    public static final int STATUS_COMPLETED = 2;

	private static final long serialVersionUID = -7435677773769357006L;

    private final List<DMTaskItem> mDataItems = new ArrayList<>();

    public boolean add(DMTaskItem item) {
        return mDataItems.add(item);
    }

    public boolean remove(DMTaskItem item) {
        return mDataItems.remove(item);
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
		for (DMTaskItem taskItem : mDataItems) {
			if (taskItem.getCompleted()) {
				return taskItem;
			}
		}
		return null;
	}
	
	public void updateProcess() {
		for (DMTaskItem dmTaskItem : mDataItems) {
			dmTaskItem.updateProcess();
		}
	}
		
	public DMTaskItem addTaskItem(DMTimerRec dmTimerRec) {
        DMTaskItem newItem = new DMTaskItem(dmTimerRec.mId, dmTimerRec.mTitle, dmTimerRec.mTimeSec, dmTimerRec.mSoundFile);
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
    public String toString() {
        String s = "";
        int i = 0;
        for (DMTaskItem item : mDataItems) {
            s += "(i=" + i++;
            s += item.toString();
            s += ")";
        }
        return s;
    }
}
