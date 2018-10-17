package com.romanpulov.symphonytimer;

import android.support.test.filters.SmallTest;

import com.romanpulov.symphonytimer.helper.LoggerHelper;
import com.romanpulov.symphonytimer.helper.db.DBHelper;

import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static android.support.test.InstrumentationRegistry.getContext;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by rpulov on 04.01.2016.
 */
@SmallTest
public class SymphonyApplicationTest {
    private final static String TAG = "ApplicationTest";

    @Test
    public void testCase1() {
        assertTrue(true);
    }

    @Test
    public void testCase2() {

        List<LinkedHashMap<Long, Long>> list = DBHelper.getInstance(getTargetContext()).getHistList(0, 2);
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
