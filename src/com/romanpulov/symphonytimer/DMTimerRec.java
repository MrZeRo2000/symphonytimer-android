package com.romanpulov.symphonytimer;

import android.os.Parcel;
import android.os.Parcelable;

public class DMTimerRec implements Parcelable{
	public long mId;
	public String mTitle;
	public long mTimeSec;
	public String mSoundFile;
	public String mImageName;
	public long mOrderId;
	
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
	}
	
	private DMTimerRec(Parcel in) {
		mId = in.readLong();
		mTitle = in.readString();
		mTimeSec = in.readLong();
		mSoundFile = in.readString();
		mImageName = in.readString();
		mOrderId = in.readLong();
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
}
