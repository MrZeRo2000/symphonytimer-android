package com.romanpulov.symphonytimer;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.MutableLiveData;
import androidx.test.filters.SmallTest;
import com.romanpulov.symphonytimer.model.DMTaskItem;
import com.romanpulov.symphonytimer.model.TimerViewModel;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

@SmallTest
public class TimerViewModelTest {

    private static void setModelTasks(TimerViewModel viewModel, Map<Long, DMTaskItem> tasks) {
        ((MutableLiveData<Map<Long, DMTaskItem>>) viewModel.getDMTaskMap()).postValue(tasks);
    }

    @Test
    public void testGetFirstTaskItemCompletedNull() {
        TimerViewModel viewModel = new TimerViewModel(getApplicationContext());

        // prepare tasks
        Map<Long, DMTaskItem> tasks = new HashMap<>();

        // set
        setModelTasks(viewModel, tasks);
        viewModel.updateTasks();

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
        viewModel.updateTasks();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // test
            DMTaskItem completed = viewModel.getFirstTaskItemCompleted(tasks);
            Assert.assertNotNull(completed);
            Assert.assertEquals(3L, completed.getId());
        }, 100);
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
        viewModel.updateTasks();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // test
            DMTaskItem completed = viewModel.getFirstTaskItemCompleted(tasks);
            Assert.assertNotNull(completed);
            Assert.assertEquals(6L, completed.getId());
        }, 100);
    }

    @Test
    public void testGetFirstTriggerAtTime() {
        TimerViewModel viewModel = new TimerViewModel(getApplicationContext());

        // no tasks
        new Handler(Looper.getMainLooper()).postDelayed(() ->
                Assert.assertEquals(Long.MAX_VALUE, viewModel.getFirstTriggerAtTime()),
                0);

        // prepare tasks
        Map<Long, DMTaskItem> tasks = new HashMap<>();
        tasks.put(3L, new DMTaskItem(3L, "Task 3", 10L, System.currentTimeMillis() - 25 * 1000, null, 0));
        tasks.put(4L, new DMTaskItem(4L, "Task 4", 10L, System.currentTimeMillis() - 15 * 1000, null, 0));

        // set
        setModelTasks(viewModel, tasks);
        viewModel.updateTasks();

        // test
        new Handler(Looper.getMainLooper()).postDelayed(() ->
                Assert.assertEquals(4L, viewModel.getFirstTriggerAtTime()),
                100);
    }
}