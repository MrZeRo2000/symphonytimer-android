package com.romanpulov.symphonytimer;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.romanpulov.symphonytimer.helper.LoggerHelper;
import com.romanpulov.symphonytimer.helper.db.DBHelper;

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

        LoggerHelper.unconditionalLog(TAG, "uList=" + uList);

        for (HashMap<Long, Long> item : list) {
            LoggerHelper.unconditionalLog(TAG, "next series");
            for (Map.Entry<Long, Long> argumentItem : uList.entrySet()) {
                Long key = argumentItem.getKey();
                Long value = item.get(argumentItem.getKey());
                long lValue = value == null ? 0 : value;
                LoggerHelper.unconditionalLog(TAG, "Key=" + key + ", Value=" + lValue);
            }
        }

        DBHelper.clearInstance();
    }

}
