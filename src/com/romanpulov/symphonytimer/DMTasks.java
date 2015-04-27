package com.romanpulov.symphonytimer;

import java.util.ArrayList;

import android.content.Context;

public class DMTasks extends ArrayList<DMTaskItem> {

	private static final long serialVersionUID = -7435677773769357006L;

	public DMTaskItem getTaskItemById(long id) {
		for (DMTaskItem taskItem : this) {
			if (id == taskItem.getId()) {
				return taskItem;
			}
		}
		return null;
	}
	
	public long getTaskItemProgress(long id){
		DMTaskItem taskItem = getTaskItemById(id);
		
		if(null != taskItem) {
			return taskItem.getProgressInSec();		
		} else {
			return 0;
		}
	}
	
	public boolean getTaskItemCompleted(long id) {
		DMTaskItem taskItem = getTaskItemById(id);
		
		if(null != taskItem) {
			return taskItem.getCompleted();		
		} else {
			return false;
		}		
	}
	
	public DMTaskItem getFirstTaskItemCompleted() {
		for (DMTaskItem taskItem : this) {
			if (taskItem.getCompleted()) {
				return taskItem;
			}
		}
		return null;
	}
	
	public long getNearestTriggerAtTime() {
		long res = this.get(0).getTriggerAtTime();
		for (DMTaskItem dmTaskItem : this) {
			res = java.lang.Math.min(res, dmTaskItem.getTriggerAtTime());			
		}
		return res;
	}
	
	public void updateProcess() {
		for (DMTaskItem dmTaskItem : this) {
			dmTaskItem.updateProcess();
		}
	}
		
	public DMTaskItem addTaskItem(Context context, DMTimerRec dmTimerRec) {
		DMTaskItem newTaskItem = new DMTaskItem(dmTimerRec.mId, dmTimerRec.mTitle, dmTimerRec.mTimeSec, dmTimerRec.mSoundFile);
		return newTaskItem;
	}
	
	public String getTaskTitles() {
		StringBuilder sb = new StringBuilder();
		String delimiter = "";
		for (DMTaskItem taskItem : this) {
			sb.append(delimiter);
			sb.append(taskItem.getTitle());
			delimiter = ",";
		}
		return sb.toString();
	}
	
}
