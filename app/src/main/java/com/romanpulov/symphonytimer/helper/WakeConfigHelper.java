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

    public boolean isValidConfig() {
        return  (mWakeBefore != -1) ;
    }

    public WakeConfigHelper(Context context) {
        String prefWakeBefore = PreferenceManager.getDefaultSharedPreferences(context).getString("pref_wake_before", context.getString(R.string.pref_wake_before_default));

        try {
            mWakeBefore = Integer.valueOf(prefWakeBefore);
        } catch (NumberFormatException e) {
            mWakeBefore = -1;
        }
    }
}
