package com.romanpulov.symphonytimer.model;

/**
 * Created by romanpulov on 17.11.2016.
 */

public class DMTasksStatus {
    public final static int STATUS_EVENT_NO_EVENT = 0;
    public final static int STATUS_EVENT_TO_COMPLETED = 1;
    public final static int STATUS_EVENT_UPDATE_COMPLETED = 2;
    public final static int STATUS_EVENT_TO_NOT_COMPLETED = 3;

    public static String statusEventAsString(int statusChangeEvent) {
        switch (statusChangeEvent) {
            case 0:
                return "STATUS_EVENT_NO_EVENT";
            case STATUS_EVENT_TO_COMPLETED:
                return "STATUS_EVENT_TO_COMPLETED";
            case STATUS_EVENT_UPDATE_COMPLETED:
                return "STATUS_EVENT_UPDATE_COMPLETED";
            case STATUS_EVENT_TO_NOT_COMPLETED:
                return "STATUS_EVENT_TO_NOT_COMPLETED";
            default:
                return "UNKNOWN";
        }
    }



    private int mStatus;

    public int getStatus() {
        return mStatus;
    }

    private DMTaskItem mFirstTaskItemCompleted;

    public DMTaskItem getFirstTaskItemCompleted() {
        return mFirstTaskItemCompleted;
    }

    public DMTasksStatus(DMTasks dmTasks){
        mStatus = dmTasks.getStatus();
        mFirstTaskItemCompleted = dmTasks.getFirstTaskItemCompleted();
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
            result = STATUS_EVENT_NO_EVENT;

        mStatus = refDMTasks.getStatus();
        mFirstTaskItemCompleted = refDMTasks.getFirstTaskItemCompleted();

        return result;
    }

    @Override
    public String toString() {
        return "{(status=" + mStatus + "), FirstTaskItemCompleted=(" + mFirstTaskItemCompleted + ")}";
    }
}
