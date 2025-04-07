package com.romanpulov.symphonytimer.model;

import org.jetbrains.annotations.NotNull;

public class DMTimerExecutionRec {
	public long mTimerId;
	public long mExecCnt;
	public long mExecPerc;

	@Override
    public @NotNull String toString() {
        return "{[TimerId=" + mTimerId + "], [ExecCnt=" + mExecCnt + "], [ExecPerc=" + mExecPerc + "]}";
    }
}
