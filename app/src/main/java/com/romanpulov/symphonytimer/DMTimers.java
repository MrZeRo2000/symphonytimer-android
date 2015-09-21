package com.romanpulov.symphonytimer;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class DMTimers implements Parcelable {

	private static final long serialVersionUID = 5787816932299989884L;

    private List<DMTimerRec> dataItems;

    public void clear() {
        dataItems.clear();
    }

    public boolean add(DMTimerRec item) {
        return dataItems.add(item);
    }

    public int indexOf(DMTimerRec item) {
        return dataItems.indexOf(item);
    }

    public int size() {
        return dataItems.size();
    }

    public DMTimerRec get(int index) {
        return  dataItems.get(index);
    }
	
	public DMTimerRec getItemById(long id) {
		for (DMTimerRec timerRec : dataItems) {
			if (timerRec.mId == id) {
				return timerRec;
			}
		}
		return null;
	}

    public DMTimers() {
        dataItems = new ArrayList<>();
    }

    private DMTimers(Parcel in) {
        dataItems = in.readArrayList(DMTimerRec.class.getClassLoader());
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(dataItems);
    }

    public static final Parcelable.Creator<DMTimers> CREATOR = new Parcelable.Creator<DMTimers>() {
        public DMTimers createFromParcel(Parcel in) {
            return new DMTimers(in);
        }

        public DMTimers[] newArray(int size) {
            return new DMTimers[size];
        }
    };
}
