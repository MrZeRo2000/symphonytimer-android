package com.romanpulov.symphonytimer.model;

import android.os.Parcel;
import android.os.Parcelable;

public class DMTaskItem implements Parcelable {
	
	public interface OnTaskItemCompleted {
		void OnTaskItemCompletedEvent (DMTaskItem dmTaskItem);
	}	
	
	private long mId;
	private String mTitle; 
	private long mMaxTimeSec;	
	private String mSoundFile;
	private OnTaskItemCompleted mTaskItemCompletedListener;
	
	private long mStartTime;
	private long mCurrentTime;
	private boolean mCompletedFlag = false;
	
	DMTaskItem(long id, String title, long timeSec, String soundFile) {
		this.mId = id;
		this.mTitle = title;
		this.mMaxTimeSec = timeSec;		
		this.mSoundFile = soundFile;
	}
	
	void setTaskItemCompleted(OnTaskItemCompleted taskItemCompletedListener) {
		this.mTaskItemCompletedListener = taskItemCompletedListener;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mId);
		dest.writeString(mTitle);
		dest.writeLong(mMaxTimeSec);
		dest.writeLong(mStartTime);
		dest.writeString(mSoundFile);
	}
	
	private DMTaskItem(Parcel in) {
		mId = in.readLong();
		mTitle = in.readString();
		mMaxTimeSec = in.readLong();
		mStartTime = in.readLong();
		mSoundFile = in.readString();
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
		return mId;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	public String getSoundFile(){
		return mSoundFile;
	}
	
	public long getProgressInSec() {
		updateProcess();
		return (mCurrentTime - mStartTime) / 1000;
	}
	
	boolean getCompleted() {
		updateProcess();
		return mCompletedFlag;
	}
	
	public long getTriggerAtTime() {
		return mStartTime + mMaxTimeSec * 1000;
	}
	
	public long getStartTime() {
		return mStartTime;
	}
	
	public long getCurrentTime() {
		return mCurrentTime;
	}	
	
	public void startProcess() {
		mStartTime = System.currentTimeMillis();
		mCompletedFlag = false;
	}
	
	void updateProcess() {
		if (!mCompletedFlag) {
			mCurrentTime = System.currentTimeMillis();
			long triggerAtTime = getTriggerAtTime();
			if (mCurrentTime > triggerAtTime) {
				mCurrentTime = triggerAtTime;
				mCompletedFlag = true;
				if (null != mTaskItemCompletedListener) {
					mTaskItemCompletedListener.OnTaskItemCompletedEvent(this);
				}
			}
		}
	}

	int getExecutionPercent() {
        if (mCompletedFlag)
            return 100;
        else
            return (int)((System.currentTimeMillis() - mStartTime) * 100 / (mMaxTimeSec * 1000));
    }

	@Override
	public String toString() {
		return "(id = " + mId + "," +
				"title = " + mTitle + "," +
                "maxTimeSec=" + mMaxTimeSec + "," +
                "startTime=" + mStartTime + "," +
                "currentTime=" + mCurrentTime + "," +
				"mTaskItemCompletedListener=" + mTaskItemCompletedListener + ")"
				;
	}
}
