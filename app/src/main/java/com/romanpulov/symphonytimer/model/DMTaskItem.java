package com.romanpulov.symphonytimer.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class DMTaskItem implements Parcelable {
    public final static String FIELD_NAME_ID = "id";
    public final static String FIELD_NAME_TITLE = "title";
    public final static String FIELD_NAME_MAX_TIME_SEC = "max_time_sec";
    public final static String FIELD_NAME_START_TIME = "start_time";
    public final static String FIELD_NAME_SOUND_FILE = "sound_file";
    public final static String FIELD_NAME_AUTO_TIMER_DISABLE = "auto_timer_disable";
	
	public interface OnTaskItemCompleted {
		void OnTaskItemCompletedEvent (DMTaskItem dmTaskItem);
	}	
	
	private final long mId;
	private final String mTitle;
	private final long mMaxTimeSec;
	private final String mSoundFileName;
	private final int mAutoTimerDisableInterval;
	private OnTaskItemCompleted mTaskItemCompletedListener;
	
	private long mStartTime;
	private long mCurrentTime;
	private boolean mCompletedFlag = false;
	
	public DMTaskItem(long id, String title, long timeSec, String soundFile, int autoTimerDisable) {
		this.mId = id;
		this.mTitle = title;
		this.mMaxTimeSec = timeSec;		
		this.mSoundFileName = soundFile;
		this.mAutoTimerDisableInterval = autoTimerDisable;
	}

	public DMTaskItem(long id, String title, long timeSec, long startTime, String soundFile, int autoTimerDisable) {
        this(id, title, timeSec, soundFile, autoTimerDisable);
        mStartTime = startTime;
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
		dest.writeString(mSoundFileName);
		dest.writeInt(mAutoTimerDisableInterval);
	}
	
	private DMTaskItem(Parcel in) {
		mId = in.readLong();
		mTitle = in.readString();
		mMaxTimeSec = in.readLong();
		mStartTime = in.readLong();
		mSoundFileName = in.readString();
		mAutoTimerDisableInterval = in.readInt();
	}	
	
	public static final Parcelable.Creator<DMTaskItem> CREATOR = new Parcelable.Creator<DMTaskItem>() {
		public DMTaskItem createFromParcel(Parcel in) {
			return new DMTaskItem(in);
		}

		public DMTaskItem[] newArray(int size) {
			return new DMTaskItem[size];
		}
	};

    /**
     * Object serialization to JSON
     * @return JSONObject
     */
    public Object toJSONObject() {
        try {
            JSONObject jo = new JSONObject();
            jo.put(FIELD_NAME_ID, mId);
            jo.put(FIELD_NAME_TITLE, mTitle);
            jo.put(FIELD_NAME_MAX_TIME_SEC, mMaxTimeSec);
            jo.put(FIELD_NAME_START_TIME, mStartTime);
            jo.put(FIELD_NAME_SOUND_FILE, mSoundFileName);
            jo.put(FIELD_NAME_AUTO_TIMER_DISABLE, mAutoTimerDisableInterval);
            return jo;
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Object deserialization from JSON
     * @param data JSONObject
     * @return deserialized object
     */
    public static DMTaskItem fromJSONObject(Object data) {
        try {
            JSONObject jo = (JSONObject) data;
            long id = jo.getLong(FIELD_NAME_ID);
            String title = jo.getString(FIELD_NAME_TITLE);
            long maxTimeSec = jo.getLong(FIELD_NAME_MAX_TIME_SEC);
            long startTime = jo.getLong(FIELD_NAME_START_TIME);
            String soundFile = jo.optString(FIELD_NAME_SOUND_FILE);
            int autoTimerDisable = jo.getInt(FIELD_NAME_AUTO_TIMER_DISABLE);
            return new DMTaskItem(id, title, maxTimeSec, startTime, soundFile, autoTimerDisable);
        } catch (JSONException e) {
            return null;
        }
    }
	
	public long getId() {
		return mId;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	public String getSoundFileName(){
		return mSoundFileName;
	}
	
	public long getProgressInSec() {
		return (mCurrentTime - mStartTime) / 1000;
	}
	
	public boolean getCompleted() {
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

	public int getAutoTimerDisableInterval() {
	    return mAutoTimerDisableInterval;
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

	public DMTaskItem createUpdated() {
		if (mCompletedFlag) {
			return this;
		} else {
			DMTaskItem newItem = new DMTaskItem(this.mId, this.mTitle, this.mMaxTimeSec, this.mSoundFileName, this.mAutoTimerDisableInterval);
			newItem.mStartTime = this.mStartTime;
			newItem.mCurrentTime = System.currentTimeMillis();
			long triggerAtTime = newItem.getTriggerAtTime();
			if (newItem.mCurrentTime > triggerAtTime) {
				newItem.mCurrentTime = triggerAtTime;
				newItem.mCompletedFlag = true;
			}
			return newItem;
		}
	}

	public int getExecutionPercent() {
        if (mCompletedFlag)
            return 100;
        else
            return (int)((System.currentTimeMillis() - mStartTime) * 100 / (mMaxTimeSec * 1000));
    }

	@Override
	public @NotNull String toString() {
		return "(id = " + mId + "," +
				"title = " + mTitle + "," +
                "maxTimeSec=" + mMaxTimeSec + "," +
                "startTime=" + mStartTime + "," +
                "currentTime=" + mCurrentTime + "," +
                "autoTimerDisableInterval=" + mAutoTimerDisableInterval + "," +
				"mTaskItemCompletedListener=" + mTaskItemCompletedListener + ")"
				;
	}
}
