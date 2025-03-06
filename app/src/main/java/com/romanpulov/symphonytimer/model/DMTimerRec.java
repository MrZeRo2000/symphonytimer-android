package com.romanpulov.symphonytimer.model;

import android.os.Parcel;
import android.os.Parcelable;
import org.jetbrains.annotations.NotNull;

public class DMTimerRec implements Parcelable{
	public long mId;
	public String mTitle;
	public long mTimeSec;
	public String mSoundFile;
	public String mImageName;
	public long mOrderId;
	public int mAutoTimerDisableInterval;
	
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
	
	public DMTimerRec() {
		
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
