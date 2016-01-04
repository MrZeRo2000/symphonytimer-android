package com.romanpulov.symphonytimer.model;

public class DMTimerExecutionRec {
	public long mTimerId;
	public long mExecCnt;
	public long mExecPerc;

	@Override
    public String toString() {
        return "{[TimerId=" + mTimerId + "], [ExecCnt=" + mExecCnt + "], [ExecPerc=" + mExecPerc + "]}";
    }
}
