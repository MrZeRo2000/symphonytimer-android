package com.romanpulov.symphonytimer.helper;

import android.content.Context;
import android.preference.PreferenceManager;

import com.romanpulov.symphonytimer.R;

/**
 * Wake parameters handling helper
 * Created by romanpulov on 16.06.2017.
 */

public class WakeConfigHelper {
    /**
     * Wake before time in minutes
     */
    private int mWakeBefore;

    public int getWakeBefore() {
        return mWakeBefore;
    }

    /**
     *
     * @return Before time converted to milliseconds
     */
    public long getWakeBeforeTime() {
        return mWakeBefore * 60 * 1000;
    }

    /**
     * Wake interval time in seconds
     */
    private int mWakeInterval;

    public int getWakeInterval() {
        return mWakeInterval;
    }

    /**
     *
     * @return Wake interval converted to milliseconds
     */
    public long getWakeIntervalTime () {
        return mWakeInterval * 1000;
    }

    public boolean isValidConfig() {
        return  (mWakeBefore != -1) && (mWakeInterval != -1);
    }

    public WakeConfigHelper(Context context) {
        String prefWakeBefore = PreferenceManager.getDefaultSharedPreferences(context).getString("pref_wake_before", context.getString(R.string.pref_wake_before_default));
        String prefWakeInterval = PreferenceManager.getDefaultSharedPreferences(context).getString("pref_wake_interval", context.getString(R.string.pref_wake_interval_default));

        try {
            mWakeBefore = Integer.valueOf(prefWakeBefore);
            mWakeInterval = Integer.valueOf(prefWakeInterval);
        } catch (NumberFormatException e) {
            mWakeBefore = -1;
            mWakeInterval = -1;
        }
    }
}
