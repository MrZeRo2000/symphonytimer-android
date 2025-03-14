package com.romanpulov.symphonytimer;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.test.filters.SmallTest;
import com.romanpulov.symphonytimer.model.DMTaskItem;
import com.romanpulov.symphonytimer.model.TimerViewModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

@SmallTest
public class TimerViewModelTest {
    private TimerViewModel viewModel;

    @Before
    public void setup() {
        Application application = getApplicationContext();
        viewModel = new TimerViewModel(application);
    }

    @Test
    public void testGetFirstTaskItemCompletedNull() {
        // prepare tasks
        Map<Long, DMTaskItem> tasks = new HashMap<>();

        // set
        new Handler(Looper.getMainLooper()).post(() -> viewModel.setDMTaskMap(tasks));
        viewModel.updateTasks();

        // test
        Assert.assertNull(viewModel.getFirstTaskItemCompleted(tasks));
    }

    @Test
    public void testGetFirstTaskItemCompletedTwoNotCompleted() {
        // prepare tasks
        Map<Long, DMTaskItem> tasks = new HashMap<>();
        tasks.put(3L, new DMTaskItem(3L, "Task 3", 10L, System.currentTimeMillis(), null, 0));
        tasks.put(4L, new DMTaskItem(4L, "Task 4", 20L, System.currentTimeMillis(), null, 0));

        // set
        new Handler(Looper.getMainLooper()).post(() -> viewModel.setDMTaskMap(tasks));
        viewModel.updateTasks();

        // test
        Assert.assertNull(viewModel.getFirstTaskItemCompleted(tasks));
    }

    @Test
    public void testGetFirstTaskItemCompletedTwoOneCompleted() {
        // prepare tasks
        Map<Long, DMTaskItem> tasks = new HashMap<>();
        tasks.put(3L, new DMTaskItem(3L, "Task 3", 10L, System.currentTimeMillis() - 15 * 1000, null, 0));
        tasks.put(4L, new DMTaskItem(4L, "Task 4", 20L, System.currentTimeMillis(), null, 0));

        // set
        viewModel.setDMTaskMap(tasks);
        viewModel.updateTasks();

        // test
        DMTaskItem completed = viewModel.getFirstTaskItemCompleted(tasks);
        Assert.assertNotNull(completed);
        Assert.assertEquals(3L, completed.getId());
    }

    @Test
    public void testGetFirstTaskItemCompletedFourTwoCompleted() {
        // prepare tasks
        Map<Long, DMTaskItem> tasks = new HashMap<>();
        tasks.put(3L, new DMTaskItem(3L, "Task 3", 10L, System.currentTimeMillis() - 15 * 1000, null, 0));
        tasks.put(4L, new DMTaskItem(4L, "Task 4", 20L, System.currentTimeMillis(), null, 0));
        tasks.put(5L, new DMTaskItem(5L, "Task 5", 20L, System.currentTimeMillis(), null, 0));
        tasks.put(6L, new DMTaskItem(6L, "Task 6", 5L, System.currentTimeMillis() - 15 * 1000, null, 0));

        // set
        viewModel.setDMTaskMap(tasks);
        viewModel.updateTasks();

        // test
        DMTaskItem completed = viewModel.getFirstTaskItemCompleted(tasks);
        Assert.assertNotNull(completed);
        Assert.assertEquals(6L, completed.getId());
    }
}
