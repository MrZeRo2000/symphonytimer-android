package com.romanpulov.symphonytimer;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import androidx.test.filters.SmallTest;
import com.romanpulov.symphonytimer.model.DMTaskItem;
import com.romanpulov.symphonytimer.model.TimerViewModel;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

@SmallTest
public class TimerViewModelTest {
    private static final String TAG = TimerViewModelTest.class.getSimpleName();

    private static void setModelTasks(TimerViewModel viewModel, Map<Long, DMTaskItem> tasks) {
        Assert.assertNull(viewModel.getDMTaskMap().getValue());
        ((MutableLiveData<Map<Long, DMTaskItem>>) viewModel.getDMTaskMap()).postValue(tasks);

        // Wait to allow postDelayed to execute
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        Assert.assertEquals(tasks.size(), viewModel.getDMTaskMap().getValue().size());

        viewModel.updateTasks();

        // Wait to allow postDelayed to execute
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Test
    public void testGetFirstTaskItemCompletedNull() {
        TimerViewModel viewModel = new TimerViewModel(getApplicationContext());

        // prepare tasks
        Map<Long, DMTaskItem> tasks = new HashMap<>();

        // set
        setModelTasks(viewModel, tasks);

        // test
        Assert.assertNull(viewModel.getFirstTaskItemCompleted(tasks));
    }

    @Test
    public void testGetFirstTaskItemCompletedTwoNotCompleted() {
        TimerViewModel viewModel = new TimerViewModel(getApplicationContext());

        // prepare tasks
        Map<Long, DMTaskItem> tasks = new HashMap<>();
        tasks.put(3L, new DMTaskItem(3L, "Task 3", 10L, System.currentTimeMillis(), null, 0));
        tasks.put(4L, new DMTaskItem(4L, "Task 4", 20L, System.currentTimeMillis(), null, 0));

        // set
        setModelTasks(viewModel, tasks);
        viewModel.updateTasks();

        // test
        Assert.assertNull(viewModel.getFirstTaskItemCompleted(tasks));
    }

    @Test
    public void testGetFirstTaskItemCompletedTwoOneCompleted() {
        TimerViewModel viewModel = new TimerViewModel(getApplicationContext());

        // prepare tasks
        Map<Long, DMTaskItem> tasks = new HashMap<>();
        tasks.put(3L, new DMTaskItem(3L, "Task 3", 10L, System.currentTimeMillis() - 15 * 1000, null, 0));
        tasks.put(4L, new DMTaskItem(4L, "Task 4", 20L, System.currentTimeMillis(), null, 0));

        // set
        setModelTasks(viewModel, tasks);

        // test
        Map<Long, DMTaskItem> updatedTasks = viewModel.getDMTaskMap().getValue();
        DMTaskItem completed = viewModel.getFirstTaskItemCompleted(updatedTasks);
        Assert.assertNotNull(completed);
        Assert.assertEquals(3L, completed.getId());
    }

    @Test
    public void testGetFirstTaskItemCompletedFourTwoCompleted() {
        TimerViewModel viewModel = new TimerViewModel(getApplicationContext());

        // prepare tasks
        Map<Long, DMTaskItem> tasks = new HashMap<>();
        tasks.put(3L, new DMTaskItem(3L, "Task 3", 10L, System.currentTimeMillis() - 15 * 1000, null, 0));
        tasks.put(4L, new DMTaskItem(4L, "Task 4", 20L, System.currentTimeMillis(), null, 0));
        tasks.put(5L, new DMTaskItem(5L, "Task 5", 20L, System.currentTimeMillis(), null, 0));
        tasks.put(6L, new DMTaskItem(6L, "Task 6", 5L, System.currentTimeMillis() - 15 * 1000, null, 0));

        // set
        setModelTasks(viewModel, tasks);

        // test
        Map<Long, DMTaskItem> updatedTasks = viewModel.getDMTaskMap().getValue();
        DMTaskItem completed = viewModel.getFirstTaskItemCompleted(updatedTasks);
        Assert.assertNotNull(completed);
        Assert.assertEquals(6L, completed.getId());
    }

    @Test
    public void testGetFirstTriggerAtTime() {
        TimerViewModel viewModel = new TimerViewModel(getApplicationContext());

        // no tasks
        Assert.assertEquals(Long.MAX_VALUE, viewModel.getFirstTriggerAtTime());

        // prepare tasks
        Map<Long, DMTaskItem> tasks = new HashMap<>();
        long currentTime = System.currentTimeMillis();
        tasks.put(3L, new DMTaskItem(3L, "Task 3", 100L, currentTime - 25 * 1000, null, 0));
        tasks.put(4L, new DMTaskItem(4L, "Task 4", 100L, currentTime - 15 * 1000, null, 0));

        // set
        setModelTasks(viewModel, tasks);

        // test
        Assert.assertEquals(currentTime - 25 * 1000 + 100 * 1000, viewModel.getFirstTriggerAtTime());
    }

    @Test
    public void testTasksToFromJSONString() throws JSONException {
        // prepare tasks
        Map<Long, DMTaskItem> tasks = new HashMap<>();
        long currentTime = System.currentTimeMillis();
        tasks.put(1L, new DMTaskItem(1L, "Task 01", 100L, currentTime - 25 * 1000, null, 0));
        tasks.put(2L, new DMTaskItem(1L, "Task 22", 20L, currentTime - 15 * 1000, null, 5));

        // write json
        String tasksJSONString = TimerViewModel.tasksToJSONString(tasks);
        // display
        Log.d(TAG, "tasks json =========");
        Log.d(TAG, tasksJSONString);
        Log.d(TAG, "=====================");

        // unpack
        Map<Long, DMTaskItem> unpackedTasks = TimerViewModel.tasksFromJSONString(tasksJSONString);

        // display
        Log.d(TAG, "unpacked tasks ======");
        Log.d(TAG, unpackedTasks.toString());
        Log.d(TAG, "=====================");

        // test
        Assert.assertNotNull(unpackedTasks);
        Assert.assertEquals(2, unpackedTasks.size());
        Assert.assertEquals(1L, Objects.requireNonNull(unpackedTasks.get(1L)).getId());
        Assert.assertEquals("Task 01", Objects.requireNonNull(unpackedTasks.get(1L)).getTitle());
    }
}