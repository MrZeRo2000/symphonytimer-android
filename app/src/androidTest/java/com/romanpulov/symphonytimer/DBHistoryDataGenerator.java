package com.romanpulov.symphonytimer;

import android.content.Context;
import android.support.test.filters.SmallTest;
import android.util.Log;

import com.romanpulov.symphonytimer.helper.DateFormatterHelper;
import com.romanpulov.symphonytimer.helper.db.DBHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getTargetContext;

@SmallTest
public class DBHistoryDataGenerator {
    private static final String LOG_TAG = "DBHistoryDataGenerator";

    private static void log(String message) {
        Log.d(LOG_TAG, message);
    }

    private DBHelper mDBHelper;

    @Before
    public void setUp() {
        Context context = getTargetContext();
        mDBHelper = DBHelper.getInstance(context);
    }

    @Test
    public void displayTimerId() {

        int timerId = (int)mDBHelper.getLongSQL("SELECT MAX(_id) FROM timer");
        log("Timer id = " + timerId);
    }

    @Test
    public void populateTimerHistory() {
        int dummyRealTime = 7777;

        mDBHelper.executeSQL("DELETE FROM timer_history WHERE real_time = " + dummyRealTime);

        int timerId = (int)mDBHelper.getLongSQL("SELECT MAX(_id) FROM timer");

        long startDate = System.currentTimeMillis();

        for (long i = 0; i < 400; i++) {
            long currentDate = startDate - i * 1000 * 60 * 60 * 24;

            log(String.valueOf(i) + " - " + String.valueOf(currentDate) + " - " + DateFormatterHelper.formatLog(currentDate));

            String sql = "INSERT INTO timer_history (timer_id, start_time, end_time, real_time) VALUES(" +
            timerId + ", " + currentDate + ", " + (currentDate + 10000) + ", " + dummyRealTime + ")";

            mDBHelper.executeSQL(sql);
        }
    }

}
