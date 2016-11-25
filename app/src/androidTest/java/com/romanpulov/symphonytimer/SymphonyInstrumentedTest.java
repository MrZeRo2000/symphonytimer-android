package com.romanpulov.symphonytimer;

import android.support.test.runner.AndroidJUnit4;

import com.romanpulov.symphonytimer.helper.LoggerHelper;
import com.romanpulov.symphonytimer.model.DMTaskItem;
import com.romanpulov.symphonytimer.model.DMTasks;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by rpulov on 04.01.2016.
 */

@RunWith(AndroidJUnit4.class)
public class SymphonyInstrumentedTest {

    @Test
    public void first() {
        assertTrue(1==1);
    }

    public DMTasks getTestDMTasks() {
        DMTasks result = new DMTasks();

        result.add(new DMTaskItem(1, "Test1", 111, 7777777, null));
        result.add(new DMTaskItem(2, "Test2", 222, 8888888, "Sound 2"));

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

        LoggerHelper.log("testJSON", ja.toString());
        jo.put("DMTasks", ja);
        LoggerHelper.log("testJSON", jo.toString());

        String jsonString = jo.toString();

        JSONObject joo = new JSONObject(jsonString);
        JSONArray joa = joo.optJSONArray("DMTasks");
        for (int i = 0; i< joa.length(); i++) {
            LoggerHelper.log("testJSON", joa.get(i).toString());
            LoggerHelper.log("testJSON", "id=" +((JSONObject)joa.get(i)).optInt("id"));
            LoggerHelper.log("testJSON", "title=" +((JSONObject)joa.get(i)).optString("title"));
        }

    }

    @Test
    public void testDMTasksJSON() {
        DMTasks tasks = getTestDMTasks();

        //writing
        String tasksString = tasks.toJSONString();
        LoggerHelper.log("testDMTasksJSON", "DMTasks=" + tasks);
        LoggerHelper.log("testDMTasksJSON", "JSON=" + tasksString);

        //reading
        DMTasks newTasks = DMTasks.fromJSONString(tasksString);
        LoggerHelper.log("testDMTasksJSON", "Restored DMTasks=" + newTasks);

        //validation
        assertEquals(tasks.size(), newTasks.size());

        //empty dm tasks test
        DMTasks emptyTasks = new DMTasks();

        //writing
        String emptyTasksString = emptyTasks.toString();
        LoggerHelper.log("testDMTasksJSON", "Empty DMTasks=" + emptyTasks);
        LoggerHelper.log("testDMTasksJSON", "Empty JSON=" + emptyTasksString);

        //reading
        DMTasks newEmptyTasks = DMTasks.fromJSONString(emptyTasksString);
        LoggerHelper.log("testDMTasksJSON", "Restored Empty DMTasks=" + newEmptyTasks);

        //validation
        assertEquals(emptyTasks.size(), newEmptyTasks.size());
    }
}
