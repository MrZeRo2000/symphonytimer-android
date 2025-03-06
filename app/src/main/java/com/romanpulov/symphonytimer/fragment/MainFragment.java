package com.romanpulov.symphonytimer.fragment;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.adapter.ListViewSelector;
import com.romanpulov.symphonytimer.adapter.SymphonyArrayAdapter;
import com.romanpulov.symphonytimer.databinding.FragmentMainBinding;
import com.romanpulov.symphonytimer.model.DMTaskItem;
import com.romanpulov.symphonytimer.model.DMTimerRec;
import com.romanpulov.symphonytimer.model.TimerViewModel;
import androidx.appcompat.view.ActionMode;

public class MainFragment extends Fragment {
    public static final String TAG = MainFragment.class.getSimpleName();

    private FragmentMainBinding binding;
    private TimerViewModel model;

    private SymphonyArrayAdapter mAdapter;

    // UI
    private ListViewSelector mListViewSelector;

    public MainFragment() {
        // Required empty public constructor
    }

    public class ActionBarCallBack implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            /*
            if (mDMTasks.getStatus() != DMTasks.STATUS_IDLE)
                return false;
            else {
                actionMode.getMenuInflater().inflate(R.menu.main_actions, menu);
                int pos;
                if ((mListViewSelector != null) && ((pos = mListViewSelector.getSelectedItemPos()) != -1)) {
                    actionMode.setTitle(mDMTimers.get(pos).mTitle);
                }
                return true;
            }

             */
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode actionMode, MenuItem menuItem) {
            /*
            int selectedItemPos = mListViewSelector.getSelectedItemPos();
            DMTimerRec actionTimer = (DMTimerRec)mTimersListView.getAdapter().getItem(selectedItemPos);

            int itemId = menuItem.getItemId();
            if (itemId == R.id.action_edit) {
                startAddItemActivity(actionTimer);
                return true;
            } else if (itemId == R.id.action_delete) {
                AlertOkCancelDialogFragment deleteDialog = AlertOkCancelDialogFragment.newAlertOkCancelDialog(actionTimer, R.string.question_are_you_sure);
                deleteDialog.setOkButtonClick(dialog -> {
                    Bundle dialogBundle = dialog.getArguments();
                    if (dialogBundle != null) {
                        DMTimerRec dmTimerRec = dialog.getArguments().getParcelable(DMTimerRec.class.toString());
                        if (null != dmTimerRec) {
                            executeTimerAction(dmTimerRec, new TimerDeleteAction());
                            //performDeleteTimer(dmTimerRec);
                            actionMode.finish();
                        }
                    }
                });
                deleteDialog.show(getSupportFragmentManager(), null);
                return true;
            } else if (itemId == R.id.action_move_up) {
                executeTimerAction(actionTimer, new TimerMoveUp());
                return true;
            } else if (itemId == R.id.action_move_down) {
                executeTimerAction(actionTimer, new TimerMoveDown());
                return true;
            }
            return false;

             */
            return false;
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

        model = new ViewModelProvider(requireActivity()).get(TimerViewModel.class);
        model.getDMTimers().observe(this, dmTimers -> {
            mAdapter = new SymphonyArrayAdapter(
                    requireContext(),
                    new ActionBarCallBack(),
                    dmTimers,
                    null,
                    this::onTimerInteraction);
            mListViewSelector = mAdapter.getListViewSelector();
            binding.mainListView.setAdapter(mAdapter);
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
                        model.addTimer(item);
                    }
                });
    }

    private void onTimerInteraction(DMTaskItem item, int position) {

    }
}