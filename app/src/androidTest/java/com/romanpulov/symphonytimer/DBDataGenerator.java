package com.romanpulov.symphonytimer;

import android.content.Context;
import android.util.Log;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.romanpulov.symphonytimer.helper.db.DBHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class DBDataGenerator {
    private static final String TAG = DBDataGenerator.class.getSimpleName();
    private static final int NUMBER_OF_TIMERS = 20;
    private static final int[] DURATIONS = new int[]{5, 10, 60};

    private DBHelper mDBHelper;

    @Before
    public void setUp() {
        mDBHelper = DBHelper.getInstance(getApplicationContext());
        mDBHelper.executeSQL("DELETE FROM timer_history");
        mDBHelper.executeSQL("DELETE FROM timer");
    }

    @Test
    public void generate() {
        for (int i = 0; i < NUMBER_OF_TIMERS; i++) {
            int index = (int)Math.floor(Math.random() * DURATIONS.length);
            int duration = DURATIONS[index];
            String sql = String.format("INSERT INTO timer (title, time_sec, order_id) " +
                    "VALUES ('%s', %d, %d)", String.format("Title %02d", i), duration, i);
            Log.d(TAG, sql);
            mDBHelper.executeSQL(sql);
        }
    }

}
