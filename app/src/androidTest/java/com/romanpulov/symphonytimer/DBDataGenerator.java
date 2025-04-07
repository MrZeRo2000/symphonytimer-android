package com.romanpulov.symphonytimer;

import android.util.Log;
import androidx.test.filters.SmallTest;
import com.romanpulov.symphonytimer.helper.DateFormatterHelper;
import com.romanpulov.symphonytimer.helper.db.DBHelper;
import org.junit.Before;
import org.junit.Test;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

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

        populateTimerHistory();
    }

    public void populateTimerHistory() {
        int dummyRealTime = 7777;

        mDBHelper.executeSQL("DELETE FROM timer_history WHERE real_time = " + dummyRealTime);

        int timerId = (int)mDBHelper.getLongSQL("SELECT MIN(_id) FROM timer");

        long startDate = System.currentTimeMillis();

        for (long i = 0; i < 400; i++) {
            long currentDate = startDate - i * 1000 * 60 * 60 * 24;

            Log.d(TAG, i + " - " + currentDate + " - " + DateFormatterHelper.formatLog(currentDate));

            String sql = "INSERT INTO timer_history (timer_id, start_time, end_time, real_time) VALUES(" +
                    timerId + ", " + currentDate + ", " + (currentDate + 10000) + ", " + dummyRealTime + ")";

            mDBHelper.executeSQL(sql);
        }
        mDBHelper.closeDB();
        Log.d(TAG, "Completed");
    }
}
