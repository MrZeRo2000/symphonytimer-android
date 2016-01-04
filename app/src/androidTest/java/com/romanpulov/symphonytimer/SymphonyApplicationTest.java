package com.romanpulov.symphonytimer;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.romanpulov.symphonytimer.helper.db.DBHelper;
import com.romanpulov.symphonytimer.model.DMTimerExecutionList;
import com.romanpulov.symphonytimer.model.DMTimerExecutionRec;

/**
 * Created by rpulov on 04.01.2016.
 */
public class SymphonyApplicationTest extends ApplicationTestCase<Application> {
    private final static String TAG = "ApplicationTest";
    public SymphonyApplicationTest() {
        super(Application.class);
    }

    public void testCase1() {
        assertTrue(true);
    }

    public void testCase2() {
        DBHelper.getInstance(getContext()).openDB();
        DMTimerExecutionList[] list = DBHelper.getInstance(getContext()).getHistList(0, 2);
        assertEquals(2, list.length);
        for (int i = 0; i < list.length; i++) {
            Log.d(TAG, "i=" + i);
            for (DMTimerExecutionRec item : list[i]) {
                Log.d(TAG, item.toString());
            }
        }
        DBHelper.getInstance(getContext()).closeDB();
    }

}
