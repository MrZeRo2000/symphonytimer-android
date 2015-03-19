package com.romanpulov.symphonytimer;

import android.os.Parcel;
import android.os.Parcelable;

public class DMTimerRec implements Parcelable{
	public long id;
	public String title;
	public long time_sec;
	public String sound_file;
	public String image_name;
	public long order_id;
	
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
		dest.writeLong(time_sec);
		dest.writeString(sound_file);
		dest.writeString(image_name);
		dest.writeLong(order_id);
	}
	
	private DMTimerRec(Parcel in) {
		id = in.readLong();
		title = in.readString();
		time_sec = in.readLong();
		sound_file = in.readString();
		image_name = in.readString();
		order_id = in.readLong();
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
