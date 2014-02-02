package com.romanpulov.symphonytimer;

import java.util.ArrayList;

public class DMTimerHistTopList extends ArrayList<DMTimerHistTopRec>{
		
	private static final long serialVersionUID = 4191447399203845533L;
	
	private long mMaxExecPerc;
	
	public void calcPerc() {
		
		long sumCnt = 0;
		
		for (DMTimerHistTopRec rec : this) {
			sumCnt += rec.execCnt;
		}
		
		mMaxExecPerc = 0;
		
		for  (DMTimerHistTopRec rec : this) {
			rec.execPerc = (long)(rec.execCnt * 100 / sumCnt);
			if (rec.execPerc > mMaxExecPerc)
				mMaxExecPerc = rec.execPerc;
		}
	}	
	
	public long getMaxExecPerc() {
		return mMaxExecPerc;
	}

}
