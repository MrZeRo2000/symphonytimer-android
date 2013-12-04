package com.romanpulov.symphonytimer;

import android.os.Parcel;
import android.os.Parcelable;

public class DMTaskItem implements Parcelable {
	
	public interface OnTaskItemCompleted {
		public void OnTaskItemCompletedEvent (DMTaskItem dmTaskItem); 
	}	
	
	private long id;
	private String title; 
	private long maxTimeSec;	
	private String soundFile;
	private OnTaskItemCompleted taskItemCompletedListener;
	
	private long startTime;
	private long currentTime;
	private boolean completedFlag = false;
	
	
	public DMTaskItem(long id, String title, long timeSec, String soundFile) {
		this.id = id;
		this.title = title;
		this.maxTimeSec = timeSec;		
		this.soundFile = soundFile;
	}
	
	public void setTaskItemCompleted(OnTaskItemCompleted taskItemCompletedListener) {
		this.taskItemCompletedListener = taskItemCompletedListener;
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeLong(id);
		dest.writeString(title);
		dest.writeLong(maxTimeSec);
		dest.writeLong(startTime);
		dest.writeString(soundFile);
	}
	
	private DMTaskItem(Parcel in) {
		id = in.readLong();
		title = in.readString();
		maxTimeSec = in.readLong();
		startTime = in.readLong();
		soundFile = in.readString();
		updateProcess();
	}	
	
	public static final Parcelable.Creator<DMTaskItem> CREATOR = new Parcelable.Creator<DMTaskItem>() {
		public DMTaskItem createFromParcel(Parcel in) {
			return new DMTaskItem(in);
		}

		public DMTaskItem[] newArray(int size) {
			return new DMTaskItem[size];
		}
	};
	
	
	public long getId() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getSoundFile(){
		return soundFile;
	}
	
	public long getProgressInSec() {
		updateProcess();
		return (long) (currentTime - startTime) / 1000;
	}
	
	public boolean getCompleted() {
		updateProcess();
		return completedFlag;
	}
	
	public long getTriggerAtTime() {
		return startTime + maxTimeSec * 1000;
	}
	
	public void startProcess() {
		startTime = System.currentTimeMillis();
		completedFlag = false;
	}
	
	public void updateProcess() {
		if (!completedFlag) {
			currentTime = System.currentTimeMillis();
			long triggerAtTime = getTriggerAtTime();
			if (currentTime > triggerAtTime) {
				currentTime = triggerAtTime;
				completedFlag = true;
				if (null != taskItemCompletedListener) {
					taskItemCompletedListener.OnTaskItemCompletedEvent(this);
				}
			}
		}
	}	
	
	public void resetProcess() {
		startTime = currentTime = 0;
		completedFlag = false;
	}
	
	public boolean isCompleted() {
		return completedFlag;
	}
	
}
