package com.romanpulov.symphonytimer.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class DMTasks implements Parcelable {

	private static final long serialVersionUID = -7435677773769357006L;

    private List<DMTaskItem> dataItems;

    public boolean add(DMTaskItem item) {
        return dataItems.add(item);
    }

    public boolean remove(DMTaskItem item) {
        return dataItems.remove(item);
    }

    public int size() {
        return dataItems.size();
    }

	public DMTaskItem getTaskItemById(long id) {
		for (DMTaskItem taskItem : dataItems) {
			if (id == taskItem.getId()) {
				return taskItem;
			}
		}
		return null;
	}
	
	public long getTaskItemProgress(long id){
		DMTaskItem taskItem = getTaskItemById(id);
		if(null != taskItem) {
			return taskItem.getProgressInSec();		
		} else {
			return 0;
		}
	}

	public DMTaskItem getFirstTaskItemCompleted() {
		for (DMTaskItem taskItem : dataItems) {
			if (taskItem.getCompleted()) {
				return taskItem;
			}
		}
		return null;
	}
	
	public void updateProcess() {
		for (DMTaskItem dmTaskItem : dataItems) {
			dmTaskItem.updateProcess();
		}
	}
		
	public DMTaskItem addTaskItem(DMTimerRec dmTimerRec) {
		return new DMTaskItem(dmTimerRec.mId, dmTimerRec.mTitle, dmTimerRec.mTimeSec, dmTimerRec.mSoundFile);
	}
	
	public String getTaskTitles() {
		StringBuilder sb = new StringBuilder();
		String delimiter = "";
		for (DMTaskItem taskItem : dataItems) {
			sb.append(delimiter);
			sb.append(taskItem.getTitle());
			delimiter = ",";
		}
		return sb.toString();
	}

    public void setTasksCompleted(DMTaskItem.OnTaskItemCompleted taskItemCompletedListener) {
        for (DMTaskItem item : dataItems) {
            item.setTaskItemCompleted(taskItemCompletedListener);
        }
    }

	public int getExecutionPercent() {
        long currentTime = System.currentTimeMillis();
        long minStartTime = currentTime;
        long maxEndTime = minStartTime;

        for (DMTaskItem taskItem : dataItems) {
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

    public DMTasks() {
        dataItems = new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    private DMTasks(Parcel in) {
        dataItems = in.readArrayList(DMTaskItem.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(dataItems);
    }

    public static final Parcelable.Creator<DMTasks> CREATOR = new Parcelable.Creator<DMTasks>() {
        public DMTasks createFromParcel(Parcel in) {
            return new DMTasks(in);
        }

        public DMTasks[] newArray(int size) {
            return new DMTasks[size];
        }
    };
}
