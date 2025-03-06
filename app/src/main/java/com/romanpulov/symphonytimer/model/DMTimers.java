package com.romanpulov.symphonytimer.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class DMTimers implements Parcelable {

	private static final long serialVersionUID = 5787816932299989884L;

    private final List<DMTimerRec> dataItems;

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
			if (timerRec.getId() == id) {
				return timerRec;
			}
		}
		return null;
	}

    public int getPosById(long id) {
        for (DMTimerRec timerRec : dataItems) {
            if (timerRec.getId() == id) {
                return dataItems.indexOf(timerRec);
            }
        }
        return -1;
    }

    public DMTimers() {
        dataItems = new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    private DMTimers(Parcel in) {
        dataItems = in.readArrayList(DMTimerRec.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
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
