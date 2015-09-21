package com.romanpulov.symphonytimer;

import java.util.ArrayList;
import java.util.List;

public class DMTimerHistTopList{
		
	private static final long serialVersionUID = 4191447399203845533L;

    private List<DMTimerHistTopRec> dataItems = new ArrayList<DMTimerHistTopRec>();
	private long mMaxExecPercent;

    public void clear() {
        dataItems.clear();;
    }

    public boolean add(DMTimerHistTopRec rec) {
        return dataItems.add(rec);
    }

    public int size() {
        return dataItems.size();
    }

    public DMTimerHistTopRec get(int location) {
        return dataItems.get(location);
    }

	public void calcPercent() {
		long sumCnt = 0;
		for (DMTimerHistTopRec rec : dataItems) {
			sumCnt += rec.mExecCnt;
		}
		
		mMaxExecPercent = 0;
		for  (DMTimerHistTopRec rec : dataItems) {
			rec.mExecPerc = (long)(rec.mExecCnt * 100 / sumCnt);
			if (rec.mExecPerc > mMaxExecPercent)
				mMaxExecPercent = rec.mExecPerc;
		}
	}	
	
	public long getMaxExecPerc() {
        return mMaxExecPercent;
	}
}
