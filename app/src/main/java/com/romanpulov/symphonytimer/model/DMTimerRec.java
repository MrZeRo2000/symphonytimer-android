package com.romanpulov.symphonytimer.model;

import android.os.Parcel;
import android.os.Parcelable;
import org.jetbrains.annotations.NotNull;

public class DMTimerRec implements Parcelable{
	private final long mId;
	private final String mTitle;
	private final long mTimeSec;
	private final String mSoundFile;
	private final String mImageName;
	private final long mOrderId;
	private final int mAutoTimerDisableInterval;

	public long getId() {
		return mId;
	}

	public String getTitle() {
		return mTitle;
	}

	public long getTimeSec() {
		return mTimeSec;
	}

	public String getSoundFile() {
		return mSoundFile;
	}

	public String getImageName() {
		return mImageName;
	}

	public long getOrderId() {
		return mOrderId;
	}

	public int getAutoTimerDisableInterval() {
		return mAutoTimerDisableInterval;
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
		dest.writeLong(mTimeSec);
		dest.writeString(mSoundFile);
		dest.writeString(mImageName);
		dest.writeLong(mOrderId);
		dest.writeInt(mAutoTimerDisableInterval);
	}
	
	private DMTimerRec(Parcel in) {
		mId = in.readLong();
		mTitle = in.readString();
		mTimeSec = in.readLong();
		mSoundFile = in.readString();
		mImageName = in.readString();
		mOrderId = in.readLong();
		mAutoTimerDisableInterval = in.readInt();
	}

	public DMTimerRec(long id, String title, long timeSec, String soundFile, String imageName, long orderId, int autoTimerDisableInterval) {
		this.mId = id;
		this.mTitle = title;
		this.mTimeSec = timeSec;
		this.mSoundFile = soundFile;
		this.mImageName = imageName;
		this.mOrderId = orderId;
		this.mAutoTimerDisableInterval = autoTimerDisableInterval;
	}

	public static final Parcelable.Creator<DMTimerRec> CREATOR = new Parcelable.Creator<DMTimerRec>() {
		public DMTimerRec createFromParcel(Parcel in) {
			return new DMTimerRec(in);
		}

		public DMTimerRec[] newArray(int size) {
			return new DMTimerRec[size];
		}
	};

	@Override
	public @NotNull String toString() {
		return "DMTimerRec{" +
				"mId=" + mId +
				", mTitle='" + mTitle + '\'' +
				", mTimeSec=" + mTimeSec +
				", mSoundFile='" + mSoundFile + '\'' +
				", mImageName='" + mImageName + '\'' +
				", mOrderId=" + mOrderId +
				", mAutoTimerDisableInterval=" + mAutoTimerDisableInterval +
				'}';
	}
}
