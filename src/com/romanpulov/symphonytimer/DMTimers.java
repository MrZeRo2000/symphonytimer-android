package com.romanpulov.symphonytimer;

import java.util.ArrayList;

public class DMTimers extends ArrayList<DMTimerRec>{

	private static final long serialVersionUID = 5787816932299989884L;
	
	public DMTimerRec getItemById(long id) {
		for (DMTimerRec timerRec : this) {
			if (timerRec.id == id) {
				return timerRec;
			}
		}
		return null;
	}
	
}
