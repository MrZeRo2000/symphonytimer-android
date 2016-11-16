package com.romanpulov.symphonytimer.activity.actions;

import android.content.Context;

import com.romanpulov.symphonytimer.model.DMTimerRec;

/**
 * Created by romanpulov on 16.11.2016.
 */

public interface TimerAction {
    int CHANGE_TYPE_POSITION = 0;
    int CHANGE_TYPE_DATA = 1;

    long execute(Context context, DMTimerRec data);
    int getChangeType();
}
