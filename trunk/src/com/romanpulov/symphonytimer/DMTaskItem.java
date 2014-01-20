package com.romanpulov.symphonytimer;

import android.os.Parcel;
import android.os.Parcelable;

public class DMTaskItem implements Parcelable {
	
	public interface OnTaskItemCompleted {
		public void OnTaskItemCompletedEvent (DMTaskItem dmTaskItem); 
	}	
	
	private long mId;
	private String mTitle; 
	private long mMaxTimeSec;	
	private String mSoundFile;
	private OnTaskItemCompleted mTaskItemCompletedListener;
	
	private long mStartTime;
	private long mCurrentTime;
	private boolean mCompletedFlag = false;
	
	
	public DMTaskItem(long id, String title, long timeSec, String soundFile) {
		this.mId = id;
		this.mTitle = title;
		this.mMaxTimeSec = timeSec;		
		this.mSoundFile = soundFile;
	}
	
	public void setTaskItemCompleted(OnTaskItemCompleted taskItemCompletedListener) {
		this.mTaskItemCompletedListener = taskItemCompletedListener;
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
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
		return (long) (mCurrentTime - mStartTime) / 1000;
	}
	
	public boolean getCompleted() {
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
	
	public void updateProcess() {
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
	
	public void resetProcess() {
		mStartTime = mCurrentTime = 0;
		mCompletedFlag = false;
	}
	
	public boolean isCompleted() {
		return mCompletedFlag;
	}
	
}
