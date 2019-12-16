package com.romanpulov.symphonytimer;


import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.romanpulov.symphonytimer.helper.LoggerHelper;
import com.romanpulov.symphonytimer.model.DMTaskItem;
import com.romanpulov.symphonytimer.model.DMTasks;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by rpulov on 04.01.2016.
 */

@RunWith(AndroidJUnit4.class)
public class SymphonyInstrumentedTest {

    private void unconditionalLog(String tag, String message) {
        LoggerHelper.unconditionalLogContext(getApplicationContext(), tag, message);
    }

    @Test
    public void first() {
        assertTrue(1==1);
    }

    public DMTasks getTestDMTasks() {
        DMTasks result = new DMTasks();

        result.add(new DMTaskItem(1, "Test1", 111, 7777777, null, 0));
        result.add(new DMTaskItem(2, "Test2", 222, 8888888, "Sound 2", 0));

        return result;
    }

    @Test
    public void testJSON() throws Exception {
        JSONObject jo = new JSONObject();
        JSONArray ja = new JSONArray();

        DMTasks tasks = getTestDMTasks();

        for (DMTaskItem item : tasks.getItems()) {
            JSONObject jio = new JSONObject();
            jio.put("id", item.getId());
            jio.put("title", item.getTitle());
            ja.put(jio);
        }

        unconditionalLog("testJSON", ja.toString());
        jo.put("DMTasks", ja);
        unconditionalLog("testJSON", jo.toString());

        String jsonString = jo.toString();

        JSONObject joo = new JSONObject(jsonString);
        JSONArray joa = joo.optJSONArray("DMTasks");
        for (int i = 0; i< joa.length(); i++) {
            unconditionalLog("testJSON", joa.get(i).toString());
            unconditionalLog("testJSON", "id=" +((JSONObject)joa.get(i)).optInt("id"));
            unconditionalLog("testJSON", "title=" +((JSONObject)joa.get(i)).optString("title"));
        }

    }

    @Test
    public void testDMTasksJSON() {
        DMTasks tasks = getTestDMTasks();

        //writing
        String tasksString = tasks.toJSONString();
        unconditionalLog("testDMTasksJSON", "DMTasks=" + tasks);
        unconditionalLog("testDMTasksJSON", "JSON=" + tasksString);

        //reading
        DMTasks newTasks = DMTasks.fromJSONString(tasksString);
        unconditionalLog("testDMTasksJSON", "Restored DMTasks=" + newTasks);

        //validation
        assertEquals(tasks.size(), newTasks.size());

        //empty dm tasks test
        DMTasks emptyTasks = new DMTasks();

        //writing
        String emptyTasksString = emptyTasks.toString();
        unconditionalLog("testDMTasksJSON", "Empty DMTasks=" + emptyTasks);
        unconditionalLog("testDMTasksJSON", "Empty JSON=" + emptyTasksString);

        //reading
        DMTasks newEmptyTasks = DMTasks.fromJSONString(emptyTasksString);
        unconditionalLog("testDMTasksJSON", "Restored Empty DMTasks=" + newEmptyTasks);

        //validation
        assertEquals(emptyTasks.size(), newEmptyTasks.size());
    }
}
