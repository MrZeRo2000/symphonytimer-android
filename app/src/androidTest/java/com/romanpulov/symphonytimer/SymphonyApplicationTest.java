package com.romanpulov.symphonytimer;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.romanpulov.symphonytimer.helper.db.DBHelper;
import com.romanpulov.symphonytimer.model.DMTimerExecutionList;
import com.romanpulov.symphonytimer.model.DMTimerExecutionRec;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        List<LinkedHashMap<Long, Long>> list = DBHelper.getInstance(getContext()).getHistList(0, 2);
        assertEquals(2, list.size());

        LinkedHashMap<Long, Long> uList = new LinkedHashMap<>();

        for (int i = list.size() - 1; i >= 0; i--) {
            for (Map.Entry<Long, Long> item : list.get(i).entrySet()) {
                uList.put(item.getKey(), item.getValue());
            }
        }

        Log.d(TAG, "uList=" + uList);

        for (HashMap<Long, Long> item : list) {
            Log.d(TAG, "next series");
            for (Map.Entry<Long, Long> argumentItem : uList.entrySet()) {
                Long key = argumentItem.getKey();
                Long value = item.get(argumentItem.getKey());
                long lValue = value == null ? 0 : value;
                Log.d(TAG, "Key=" + key + ", Value=" + lValue);
            }
        }

        /*
        for (int i = 0; i < list.length; i++) {
            Log.d(TAG, "i=" + i);
            int position = 0;
            for (DMTimerExecutionRec item : list[i]) {
                Log.d(TAG, "(" + position ++ + ") " + item.toString());
            }
        }
        */
        DBHelper.clearInstance();
    }

}
