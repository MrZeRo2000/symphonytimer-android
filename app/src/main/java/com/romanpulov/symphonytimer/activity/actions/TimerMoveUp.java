package com.romanpulov.symphonytimer.activity.actions;

import android.content.Context;

import com.romanpulov.symphonytimer.helper.db.DBHelper;
import com.romanpulov.symphonytimer.model.DMTimerRec;

/**
 * Created by romanpulov on 16.11.2016.
 */

public class TimerMoveUp implements TimerAction {
    @Override
    public long execute(Context context, DMTimerRec data) {
        return DBHelper.getInstance(context).moveTimerUp(data.getOrderId()) ? 1 : 0;
    }

    @Override
    public int getChangeType() {
        return CHANGE_TYPE_POSITION;
    }
}
