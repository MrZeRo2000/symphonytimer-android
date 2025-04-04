package com.romanpulov.symphonytimer.model;

public class DMTimerHistRec {
	public final long id;
	public final long timerId;
	public final long startTime;
	public final long endTime;
	public final long realTime;

	public DMTimerHistRec(long id, long timerId, long startTime, long endTime, long realTime) {
		this.id = id;
		this.timerId = timerId;
		this.startTime = startTime;
		this.endTime = endTime;
		this.realTime = realTime;
	}
}
