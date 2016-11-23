package com.romanpulov.symphonytimer.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class DMTaskItem implements Parcelable {
    public final static String FIELD_NAME_ID = "id";
    public final static String FIELD_NAME_TITLE = "title";
    public final static String FIELD_NAME_MAX_TIME_SEC = "max_time_sec";
    public final static String FIELD_NAME_START_TIME = "start_time";
    public final static String FIELD_NAME_SOUND_FILE = "sound_file";
	
	public interface OnTaskItemCompleted {
		void OnTaskItemCompletedEvent (DMTaskItem dmTaskItem);
	}	
	
	private final long mId;
	private final String mTitle;
	private final long mMaxTimeSec;
	private final String mSoundFile;
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

	public DMTaskItem(long id, String title, long timeSec, long startTime, String soundFile) {
        this(id, title, timeSec, soundFile);
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
            jo.put(FIELD_NAME_SOUND_FILE, mSoundFile);
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
            return new DMTaskItem(id, title, maxTimeSec, startTime, soundFile);
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
