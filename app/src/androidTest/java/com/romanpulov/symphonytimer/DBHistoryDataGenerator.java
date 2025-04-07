package com.romanpulov.symphonytimer;

import android.content.Context;
import android.util.Log;

import androidx.test.filters.SmallTest;

import com.romanpulov.symphonytimer.helper.DateFormatterHelper;
import com.romanpulov.symphonytimer.helper.db.DBHelper;

import org.junit.Before;
import org.junit.Test;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

@SmallTest
public class DBHistoryDataGenerator {
    private static final String LOG_TAG = "DBHistoryDataGenerator";

    private static void log(String message) {
        Log.d(LOG_TAG, message);
    }

    private DBHelper mDBHelper;

    @Before
    public void setUp() {
        Context context = getApplicationContext();
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

            log(i + " - " + currentDate + " - " + DateFormatterHelper.formatLog(currentDate));

            String sql = "INSERT INTO timer_history (timer_id, start_time, end_time, real_time) VALUES(" +
            timerId + ", " + currentDate + ", " + (currentDate + 10000) + ", " + dummyRealTime + ")";

            mDBHelper.executeSQL(sql);
        }
    }

}
