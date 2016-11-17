package com.romanpulov.symphonytimer.model;

/**
 * Created by romanpulov on 17.11.2016.
 */

public class DMTasksStatus {
    public final static int STATUS_EVENT_TO_COMPLETED = 1;
    public final static int STATUS_EVENT_UPDATE_COMPLETED = 2;
    public final static int STATUS_EVENT_TO_NOT_COMPLETED = 3;

    private final DMTasks mDMTasks;

    private int mStatus;

    public int getStatus() {
        return mStatus;
    }

    private DMTaskItem mFirstTaskItemCompleted;

    public DMTaskItem getFirstTaskItemCompleted() {
        return mFirstTaskItemCompleted;
    }

    public DMTasksStatus(DMTasks dmTasks){
        mDMTasks = dmTasks;
        mStatus = mDMTasks.getStatus();
        mFirstTaskItemCompleted = mDMTasks.getFirstTaskItemCompleted();
    }

    public int getStatusChangeEvent(DMTasks refDMTasks) {
        int result;
        if ((mStatus != DMTasks.STATUS_COMPLETED) && (refDMTasks.getStatus() == DMTasks.STATUS_COMPLETED)) {
            result = STATUS_EVENT_TO_COMPLETED;
        } else if ((mStatus == DMTasks.STATUS_COMPLETED) && (refDMTasks.getStatus() == DMTasks.STATUS_COMPLETED) && (mFirstTaskItemCompleted.getId() != refDMTasks.getFirstTaskItemCompleted().getId())) {
            result = STATUS_EVENT_UPDATE_COMPLETED;
        } else if ((mStatus == DMTasks.STATUS_COMPLETED) && (refDMTasks.getStatus() != DMTasks.STATUS_COMPLETED)) {
            result = STATUS_EVENT_TO_NOT_COMPLETED;
        } else
            result = 0;

        mStatus = refDMTasks.getStatus();
        mFirstTaskItemCompleted = refDMTasks.getFirstTaskItemCompleted();

        return result;
    }
}
