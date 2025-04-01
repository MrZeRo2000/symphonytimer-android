package com.romanpulov.symphonytimer.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.adapter.ListViewSelector;
import com.romanpulov.symphonytimer.adapter.SymphonyArrayAdapter;
import com.romanpulov.symphonytimer.databinding.FragmentMainBinding;
import com.romanpulov.symphonytimer.helper.MediaStorageHelper;
import com.romanpulov.symphonytimer.helper.db.DBHelper;
import com.romanpulov.symphonytimer.model.DMTaskItem;
import com.romanpulov.symphonytimer.model.DMTimerRec;
import com.romanpulov.symphonytimer.model.TimerViewModel;
import androidx.appcompat.view.ActionMode;
import com.romanpulov.symphonytimer.service.TaskUpdateService;
import com.romanpulov.symphonytimer.utils.SpaceItemDecoration;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainFragment extends Fragment {
    public static final String TAG = MainFragment.class.getSimpleName();

    private FragmentMainBinding binding;
    private TimerViewModel model;

    private SymphonyArrayAdapter mAdapter;

    // UI
    private ListViewSelector mListViewSelector;

    private static final int WINDOW_SCREEN_ON_FLAGS = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;

    public MainFragment() {
        // Required empty public constructor
    }

    private void performMediaCleanup() {
        List<String> mediaNameList = DBHelper.getInstance(requireContext().getApplicationContext()).getMediaFileNameList();
        MediaStorageHelper.getInstance(requireContext().getApplicationContext()).cleanupMedia(mediaNameList);
    }

    public class ActionBarCallBack implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            if (model.getCurrentTasksStatus() != TimerViewModel.TASKS_STATUS_IDLE) {
                return false;
            } else {
                actionMode.getMenuInflater().inflate(R.menu.main_actions, menu);

                int pos;
                List<DMTimerRec> dmTimers;
                if ((mListViewSelector != null) &&
                        ((pos = mListViewSelector.getSelectedItemPos()) != -1) &&
                        ((dmTimers = model.getDMTimers().getValue()) != null)) {
                    actionMode.setTitle(dmTimers.get(pos).getTitle());
                }
                return true;
            }
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode actionMode, MenuItem menuItem) {
            int selectedItemPos = mListViewSelector.getSelectedItemPos();
            DMTimerRec actionTimer = Objects.requireNonNull(model.getDMTimers().getValue()).get(selectedItemPos);

            int itemId = menuItem.getItemId();

            if (itemId == R.id.action_edit) {
                NavHostFragment.findNavController(MainFragment.this).navigate(
                        MainFragmentDirections.actionMainToTimerEdit().setEditItem(actionTimer));
                actionMode.finish();
                return true;
            } else if (itemId == R.id.action_delete) {
                AlertOkCancelDialogFragment deleteDialog = AlertOkCancelDialogFragment.newAlertOkCancelDialog(actionTimer, R.string.question_are_you_sure);
                deleteDialog.setOkButtonClick(dialog -> {
                    Bundle dialogBundle = dialog.getArguments();
                    if (dialogBundle != null) {
                        DMTimerRec dmTimerRec = dialog.getArguments().getParcelable(DMTimerRec.class.toString());
                        if (null != dmTimerRec) {
                            model.deleteTimer(dmTimerRec);
                            actionMode.finish();
                            performMediaCleanup();
                        }
                    }
                });
                deleteDialog.show(MainFragment.this.getParentFragmentManager(), null);
                return true;
            } else if (itemId == R.id.action_move_up) {
                model.moveTimerUp(actionTimer);
                return true;
            } else if (itemId == R.id.action_move_down) {
                model.moveTimerDown(actionTimer);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            if (mListViewSelector != null)
                mListViewSelector.destroyActionMode();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            requireActivity().setTurnScreenOn(true);
            requireActivity().setShowWhenLocked(true);
        } else {
            requireActivity().getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);

        binding.mainListView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.mainListView.addItemDecoration(new SpaceItemDecoration((int)requireContext().getResources().getDimension(R.dimen.list_divider_height)));

        model = TimerViewModel.getInstance(requireActivity().getApplication());
        model.getDMTimers().observe(this, dmTimers -> {
            if (mAdapter == null) {
                mAdapter = new SymphonyArrayAdapter(
                        requireContext(),
                        new ActionBarCallBack(),
                        dmTimers,
                        null,
                        this::onTimerInteraction);
            } else {
                mAdapter.updateValues(dmTimers, binding.mainListView);
            }
            mListViewSelector = mAdapter.getListViewSelector();
            binding.mainListView.setAdapter(mAdapter);
        });
        model.getDMTaskMap().observe(this, dmTasks -> {
            Log.d(TAG, "tasks updated");
            if (mAdapter != null) {
                if (dmTasks != null) {
                    Log.d(TAG, "first task execution percent:" + dmTasks.values().iterator().next().getExecutionPercent());
                }
                mAdapter.updateTasks(dmTasks);
            }
        });
        model.getTaskStatusChange().observe(this, taskStatus -> {
           if ((taskStatus.first == TimerViewModel.TASKS_STATUS_IDLE) &&
                   (taskStatus.second != TimerViewModel.TASKS_STATUS_IDLE)) {
               Log.d(TAG, "Task status changed from idle, need to start the service");

               requireContext().startService(new Intent(requireContext(), TaskUpdateService.class));
               // prevent flickering on task status update
               Objects.requireNonNull(binding.mainListView.getItemAnimator()).setChangeDuration(0L);
            } else if ((taskStatus.first != TimerViewModel.TASKS_STATUS_IDLE) &&
                   (taskStatus.second == TimerViewModel.TASKS_STATUS_IDLE)) {
               Log.d(TAG, "Task status changed to idle, need to stop the service");

               // restore default animation
               Objects.requireNonNull(binding.mainListView.getItemAnimator()).setChangeDuration(250L);
           }

           if (taskStatus.second == TimerViewModel.TASKS_STATUS_COMPLETED) {
               //prevent from sleeping while not turned off
               requireActivity().getWindow().addFlags(WINDOW_SCREEN_ON_FLAGS);
           } else {
               requireActivity().getWindow().clearFlags(WINDOW_SCREEN_ON_FLAGS);
           }
        });

        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {

            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                Log.d(TAG, "Menu created");
                menuInflater.inflate(R.menu.main_options, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.action_add) {
                    NavHostFragment.findNavController(MainFragment.this).navigate(
                            MainFragmentDirections.actionMainToTimerEdit());
                    return true;
                } else if (itemId == R.id.action_preferences) {
                    NavHostFragment.findNavController(MainFragment.this).navigate(
                            MainFragmentDirections.actionMainToSettings());
                    return true;
                } else {
                    return false;
                }
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        model.loadTimers();

        getParentFragmentManager().setFragmentResultListener(
                TimerEditFragment.RESULT_KEY, this, (requestKey, bundle) -> {
                    DMTimerRec item = bundle.getParcelable(TimerEditFragment.RESULT_VALUE_KEY);
                    if (item != null) {
                        Log.d(TAG, "Timer edit result: " + item);
                        if (item.getId() == 0) {
                            model.addTimer(item);
                        } else {
                            model.editTimer(item);
                        }
                        performMediaCleanup();
                    }
                });
    }

    private void onTimerInteraction(DMTimerRec item, int position) {
        Map<Long, DMTaskItem> taskValue = model.getDMTaskMap().getValue();
        DMTaskItem task = taskValue == null ? null : taskValue.get(item.getId());
        if (task == null) {
            model.addTask(item);
        } else {
            model.removeTask(item.getId());
        }
    }
}