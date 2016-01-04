package com.romanpulov.symphonytimer.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DMTimerExecutionList implements Iterable<DMTimerExecutionRec> {
		
	private static final long serialVersionUID = 4191447399203845533L;

    private List<DMTimerExecutionRec> dataItems = new ArrayList<>();
	private long mMaxExecPercent;

    public void clear() {
        dataItems.clear();;
    }

    public boolean add(DMTimerExecutionRec rec) {
        return dataItems.add(rec);
    }

    public int size() {
        return dataItems.size();
    }

    public DMTimerExecutionRec get(int location) {
        return dataItems.get(location);
    }

	public void calcPercent() {
		long sumCnt = 0;
		for (DMTimerExecutionRec rec : dataItems) {
			sumCnt += rec.mExecCnt;
		}
		
		mMaxExecPercent = 0;
		for  (DMTimerExecutionRec rec : dataItems) {
			rec.mExecPerc = (long)(rec.mExecCnt * 100 / sumCnt);
			if (rec.mExecPerc > mMaxExecPercent)
				mMaxExecPercent = rec.mExecPerc;
		}
	}	
	
	public long getMaxExecPerc() {
        return mMaxExecPercent;
	}

	@Override
	public Iterator<DMTimerExecutionRec> iterator() {
		return dataItems.iterator();
	}
}
