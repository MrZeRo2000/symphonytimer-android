package com.romanpulov.symphonytimer.helper;

import android.content.Context;

import androidx.preference.PreferenceManager;
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

    /**
     *
     * @return Before time converted to milliseconds
     */
    public long getWakeBeforeTime() {
        return (long) mWakeBefore * 60 * 1000;
    }

    public boolean isValidConfig() {
        return  (mWakeBefore != -1) ;
    }

    public WakeConfigHelper(Context context) {
        String prefWakeBefore = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString("pref_wake_before", context.getString(R.string.pref_wake_before_default));
        try {
            mWakeBefore = Integer.parseInt(prefWakeBefore);
        } catch (NumberFormatException e) {
            mWakeBefore = -1;
        }
    }
}
