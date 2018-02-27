package com.romanpulov.symphonytimer.activity;

import java.util.List;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.romanpulov.symphonytimer.activity.actions.TimerAction;
import com.romanpulov.symphonytimer.activity.actions.TimerDeleteAction;
import com.romanpulov.symphonytimer.activity.actions.TimerInsertAction;
import com.romanpulov.symphonytimer.activity.actions.TimerMoveDown;
import com.romanpulov.symphonytimer.activity.actions.TimerMoveUp;
import com.romanpulov.symphonytimer.activity.actions.TimerUpdateAction;
import com.romanpulov.symphonytimer.adapter.ListViewSelector;
import com.romanpulov.symphonytimer.helper.LoggerHelper;
import com.romanpulov.symphonytimer.helper.MediaStorageHelper;
import com.romanpulov.symphonytimer.helper.VibratorHelper;
import com.romanpulov.symphonytimer.service.TaskService;
import com.romanpulov.symphonytimer.fragment.AlertOkCancelDialogFragment;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.adapter.SymphonyArrayAdapter;
import com.romanpulov.symphonytimer.helper.AssetsHelper;
import com.romanpulov.symphonytimer.helper.db.DBHelper;
import com.romanpulov.symphonytimer.model.DMTaskItem;
import com.romanpulov.symphonytimer.model.DMTasks;
import com.romanpulov.symphonytimer.model.DMTimerRec;
import com.romanpulov.symphonytimer.model.DMTimers;
import com.romanpulov.symphonytimer.service.TaskServiceManager;


public class MainActivity extends ActionBarActivity implements ActionMode.Callback {
    private void log(String message) {
        LoggerHelper.logContext(this, "MainActivity", message);
    }

	public static final int ADD_ITEM_RESULT_CODE = 1;
	public static final int EDIT_ITEM_RESULT_CODE = 2;
	
	public static final int CONTEXT_MENU_ADD = Menu.FIRST + 1;
	public static final int CONTEXT_MENU_EDIT = Menu.FIRST + 2;
	public static final int CONTEXT_MENU_DELETE = Menu.FIRST + 3;
	public static final int CONTEXT_MENU_MOVE_UP = Menu.FIRST + 4;
	public static final int CONTEXT_MENU_MOVE_DOWN = Menu.FIRST + 5;

    private static final long LIST_CLICK_DELAY = 1000;
	private static final int WINDOW_SCREEN_ON_FLAGS = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
	
	//data
	private final DMTimers mDMTimers = new DMTimers();
	private final DMTasks mDMTasks = new DMTasks();
    {
        mDMTasks.setTasksCompleted(new DMTaskItem.OnTaskItemCompleted() {
            @Override
            public void OnTaskItemCompletedEvent(DMTaskItem dmTaskItem) {
                performTaskCompleted(dmTaskItem);
            }
        });
    }

    //UI
	private ListView mTimersListView;
    private ListViewSelector mListViewSelector;
    private long mLastClickTime;
    private boolean mActivityVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        
        this.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        //WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);

        mActivityVisible = true;

        //setup actionbar icon
		/*
        ActionBar actionBar= this.getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME, ActionBar.DISPLAY_SHOW_HOME);
        actionBar.setIcon(R.drawable.tuba);
        */

		//setup toolbar
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setIcon(R.drawable.tuba);
        }

		final SymphonyArrayAdapter adapter = new SymphonyArrayAdapter(this, this, mDMTimers, mDMTasks, new OnDMTimerInteractionListener() {
            @Override
            public void onDMTimerInteraction(DMTimerRec item, int position) {
                long clickTime = System.currentTimeMillis();
                if ((!mDMTasks.isLocked()) && (clickTime - mLastClickTime > LIST_CLICK_DELAY)) {
                    VibratorHelper.shortVibrate(MainActivity.this);
                    performTimerAction(mDMTimers.get(position));
                }
                mLastClickTime = clickTime;
            }
        });
        mListViewSelector = adapter.getListViewSelector();

        mTimersListView = (ListView)findViewById(R.id.main_list_view);
        mTimersListView.setAdapter(adapter);

        mTimersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                log("onClick");
            }
        });

        // Update List
        loadTimers();
        updateTimers();

        AssetsHelper.listAssets(this, "pre_inst_images");
    }

    @Override
    protected void onDestroy() {
    	DBHelper.clearInstance();

        mTaskServiceManager.unbindService();
        mTaskServiceManager = null;

        LoggerHelper.clearInstance();

    	super.onDestroy();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	mActivityVisible = false;
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	mActivityVisible = true;

    	if (DBHelper.getInstance(this).getDBDataChanged()) {
    		loadTimers();
    		DBHelper.getInstance(this).resetDBDataChanged();
    	}

    	updateTimers();

        //waiting for data if the service is available
        if ((!mTaskServiceManager.isServiceBound()) && (mTaskServiceManager.isTaskServiceRunning())) {
            mDMTasks.lock();
            mTaskServiceManager.updateServiceTasks(mDMTasks);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_options, menu);
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
            case R.id.action_add:
                startAddItemActivity(new DMTimerRec());
                return true;
            case R.id.action_preferences:
                if (mDMTasks.getStatus() == DMTasks.STATUS_IDLE) {
                    startSettingsActivity();
                    return true;
                } else
                    return super.onOptionsItemSelected(item);
            case R.id.action_history:
                startHistoryActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
    	}
    }

    private void startAddItemActivity(DMTimerRec dmTimerRec) {
    	Intent startItemIntent = new Intent(this, AddItemActivity.class);
    	startItemIntent.putExtra(AddItemActivity.EDIT_REC_NAME, dmTimerRec);    	
    	startActivityForResult(startItemIntent,
                null == dmTimerRec.mTitle ? ADD_ITEM_RESULT_CODE : EDIT_ITEM_RESULT_CODE);
    }
    
    private void startHistoryActivity() {
    	Intent startHistoryIntent = new Intent(this, HistoryActivity.class);
    	startHistoryIntent.putExtra(HistoryActivity.TIMERS_NAME, mDMTimers);
        startActivity(startHistoryIntent);
    }

    private void startSettingsActivity() {
        Intent preferencesIntent = new Intent(this, SettingsActivity.class);
        startActivity(preferencesIntent);
    }

    //Timers interaction

    private void loadTimers() {
    	 DBHelper.getInstance(this).fillTimers(mDMTimers);
    }
    
    private void updateTimers() {
    	SymphonyArrayAdapter adapter = (SymphonyArrayAdapter)mTimersListView.getAdapter();
    	adapter.notifyDataSetChanged();
    	checkTimerSelection();
    }

    private void checkTimerSelection() {
        DMTaskItem taskItem = mDMTasks.getFirstTaskItemCompleted();
        if (null != taskItem) {
            DMTimerRec timerRec = mDMTimers.getItemById(taskItem.getId());
            if (null != timerRec) {
                int index = mDMTimers.indexOf(timerRec);
                if ( (index<mTimersListView.getFirstVisiblePosition()) || (index>mTimersListView.getLastVisiblePosition()) ) {
                    mTimersListView.setSelection(index);
                }
            }
        }
    }

    //Timer actions

    private void performMediaCleanup() {
        List<String> mediaNameList = DBHelper.getInstance(getApplicationContext()).getMediaFileNameList();
        MediaStorageHelper.getInstance(getApplicationContext()).cleanupMedia(mediaNameList);
    }

    private void executeTimerAction(DMTimerRec dmTimerRec, TimerAction timerAction) {
        try {
            long retVal = timerAction.execute(this, dmTimerRec);
            if (retVal > 0) {
                if (timerAction.getChangeType() == TimerAction.CHANGE_TYPE_DATA)
                    performMediaCleanup();

                loadTimers();
                updateTimers();

                if (timerAction.getChangeType() == TimerAction.CHANGE_TYPE_POSITION) {
                    int pos = mDMTimers.getPosById(dmTimerRec.mId);
                    if (pos != -1) {
                        mListViewSelector.setSelectedView(pos);
                        mTimersListView.smoothScrollToPositionFromTop(pos, 0);
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, String.format(getResources().getString(R.string.error_action_timer), e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    //Timer events

    private void performTaskCompleted(DMTaskItem dmTaskItem) {
        log("taskCompleted");
    	//bring activity to front
    	if (!mActivityVisible) {
    		Intent intent = new Intent(getApplicationContext(), this.getClass());
    		intent.setComponent(new ComponentName(this.getPackageName(), this.getClass().getName()));
    		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);    		
    		getApplicationContext().startActivity(intent);   
    	}
    	
    	//prevent from sleeping while not turned off
    	getWindow().addFlags(WINDOW_SCREEN_ON_FLAGS);

    	//save history
    	DBHelper.getInstance(this).insertTimerHistory(dmTaskItem);

        /*
    	//play sound
    	MediaPlayerHelper.getInstance(this).startSoundFile(dmTaskItem.getSoundFileName());
    	
    	//vibrate
  		VibratorHelper.vibrate(this);
  		*/
    }

    private void performTimerAction(DMTimerRec dmTimerRec) {
    	DMTaskItem taskItem = mDMTasks.getTaskItemById(dmTimerRec.mId);

    	if (null == taskItem) {
    		DMTaskItem newTaskItem = mDMTasks.addTaskItem(dmTimerRec);
    		//newTaskItem.setTaskItemCompleted(mTaskItemCompleted);
    		newTaskItem.startProcess();
    		
    		mDMTasks.add(newTaskItem);

            /*
            if (null == mAlarm)
                mAlarm = new ExactAlarmBroadcastReceiver();
            mAlarm.setOnetimeTimer(getApplicationContext(), newTaskItem.getId(), newTaskItem.getTriggerAtTime());
            */

    	} else {
            /*
            if (null != mAlarm)
                mAlarm.cancelAlarm(getApplicationContext(), taskItem.getId());
                */

    		updateTimers();

            int prevStatus = mDMTasks.getStatus();

    		mDMTasks.remove(taskItem);
    		
    		// inactive timer or no timers
    		if ((prevStatus == DMTasks.STATUS_COMPLETED) && (mDMTasks.getStatus() != DMTasks.STATUS_COMPLETED)) {
                /*
                //stop vibrating
                VibratorHelper.cancel(this);
    			//stop sound
        		MediaPlayerHelper.getInstance(this).stop();
        		*/
    			//enable screen fading
    			getWindow().clearFlags(WINDOW_SCREEN_ON_FLAGS);
    		}
    	}

        mTaskServiceManager.updateServiceTasks(mDMTasks);
    }
    
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	if (RESULT_OK == resultCode) {    		
    		if ((null != data) && (null != data.getExtras())) {
    		    //retrieve data
    			DMTimerRec newTimer = data.getExtras().getParcelable(AddItemActivity.EDIT_REC_NAME);
    			//action
    			switch (requestCode) {
    				case (ADD_ITEM_RESULT_CODE):
    					//performInsertTimer(newTimer);
                        executeTimerAction(newTimer, new TimerInsertAction());
    					break;
    				case (EDIT_ITEM_RESULT_CODE):
    					//performUpdateTimer(newTimer);
                        executeTimerAction(newTimer, new TimerUpdateAction());
    					break;    					
    			}
    		}
    	}
    }

    // Action mode support

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
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
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode actionMode, MenuItem menuItem) {
        int selectedItemPos = mListViewSelector.getSelectedItemPos();
        DMTimerRec actionTimer = (DMTimerRec)mTimersListView.getAdapter().getItem(selectedItemPos);

        switch (menuItem.getItemId()) {
            case R.id.action_edit:
                startAddItemActivity(actionTimer);
                return true;
            case R.id.action_delete:
                AlertOkCancelDialogFragment deleteDialog = AlertOkCancelDialogFragment.newAlertOkCancelDialog(actionTimer, R.string.question_are_you_sure);
                deleteDialog.setOkButtonClick(new AlertOkCancelDialogFragment.OnOkButtonClick() {
                    @Override
                    public void OnOkButtonClickEvent(DialogFragment dialog) {
                        DMTimerRec dmTimerRec = dialog.getArguments().getParcelable(DMTimerRec.class.toString());
                        if (null != dmTimerRec) {
                            executeTimerAction(dmTimerRec, new TimerDeleteAction());
                            //performDeleteTimer(dmTimerRec);
                            actionMode.finish();
                        }
                    }
                });
                deleteDialog.show(getFragmentManager(), null);
                return true;
            case R.id.action_move_up:
                executeTimerAction(actionTimer, new TimerMoveUp());
                return true;
            case R.id.action_move_down:
                executeTimerAction(actionTimer, new TimerMoveDown());
                return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        if (mListViewSelector != null)
            mListViewSelector.destroyActionMode();
    }

    public interface OnDMTimerInteractionListener {
        void onDMTimerInteraction(DMTimerRec item, int position);
    }

    //Service interaction support

    private TaskServiceManager mTaskServiceManager = new TaskServiceManager(this, new IncomingHandler(this));

    /**
     * Handler of incoming messages from service.
     */
    private static class IncomingHandler extends Handler {
        private final MainActivity mHostReference;

        IncomingHandler(MainActivity host) {
            mHostReference = host;
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity hostActivity = mHostReference;
            if (hostActivity != null) {
                switch (msg.what) {
                    case TaskService.MSG_UPDATE_DM_PROGRESS:
                        hostActivity.performUpdateDMProgress();
                        break;
                    case TaskService.MSG_UPDATE_DM_TASKS:
                        DMTasks newTasks = msg.getData().getParcelable(DMTasks.class.toString());
                        hostActivity.performUpdateDMTasks(newTasks);
                        break;
                    default:
                        super.handleMessage(msg);
                }
            } else
                super.handleMessage(msg);
        }
    }

    private void performUpdateDMProgress() {
        if (mActivityVisible)
            updateTimers();
        else
            mDMTasks.updateProcess();
    }

    private void performUpdateDMTasks(DMTasks newTasks) {
        mDMTasks.unlock();
        if (newTasks != null) {
            if (newTasks.size() != mDMTasks.size()) {
                log("performUpdateDMTasks");
                mDMTasks.replaceTasks(newTasks);
                updateTimers();

                // if tasks are expired by external reasons
                if ((mDMTasks.size() == 0) && (mTaskServiceManager != null))
                    mTaskServiceManager.updateServiceTasks(mDMTasks);
            }
        }
    }
}
