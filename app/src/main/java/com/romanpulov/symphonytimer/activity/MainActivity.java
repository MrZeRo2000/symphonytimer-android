package com.romanpulov.symphonytimer.activity;

import java.lang.ref.WeakReference;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;

import com.romanpulov.symphonytimer.adapter.ListViewSelector;
import com.romanpulov.symphonytimer.helper.MediaStorageHelper;
import com.romanpulov.symphonytimer.service.TaskService;
import com.romanpulov.symphonytimer.fragment.AlertOkCancelDialogFragment;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.adapter.SymphonyArrayAdapter;
import com.romanpulov.symphonytimer.helper.AssetsHelper;
import com.romanpulov.symphonytimer.helper.MediaPlayerHelper;
import com.romanpulov.symphonytimer.helper.VibratorHelper;
import com.romanpulov.symphonytimer.helper.db.DBHelper;
import com.romanpulov.symphonytimer.model.DMTaskItem;
import com.romanpulov.symphonytimer.model.DMTasks;
import com.romanpulov.symphonytimer.model.DMTimerRec;
import com.romanpulov.symphonytimer.model.DMTimers;
import com.romanpulov.symphonytimer.service.TaskServiceManager;


public class MainActivity extends ActionBarActivity implements ActionMode.Callback {
    private static void log(String message) {
        Log.d("MainActivity", message);
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
    private boolean activityVisible = false;

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
                performEditTimer(actionTimer);
                return true;
            case R.id.action_delete:
                AlertOkCancelDialogFragment deleteDialog = AlertOkCancelDialogFragment.newAlertOkCancelDialog(actionTimer, R.string.question_are_you_sure);
                deleteDialog.setOkButtonClick(new AlertOkCancelDialogFragment.OnOkButtonClick() {
                    @Override
                    public void OnOkButtonClickEvent(DialogFragment dialog) {
                        DMTimerRec dmTimerRec = dialog.getArguments().getParcelable(DMTimerRec.class.toString());
                        if (null != dmTimerRec) {
                            performDeleteTimer(dmTimerRec);
                            actionMode.finish();
                        }
                    }
                });
                deleteDialog.show(getFragmentManager(), null);
                return true;
            case R.id.action_move_up:
                performMoveUpTimer(actionTimer);
                return true;
            case R.id.action_move_down:
                performMoveDownTimer(actionTimer);
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
	
    private TaskServiceManager mTaskServiceManager = new TaskServiceManager(this, new IncomingHandler(this));

    /**
     * Handler of incoming messages from service.
     */
    private static class IncomingHandler extends Handler {
        private final WeakReference<MainActivity> mHost;

        IncomingHandler(MainActivity host) {
            mHost = new WeakReference<>(host);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TaskService.MSG_UPDATE_DM_PROGRESS:
                    mHost.get().performUpdateDMProgress();
                    break;
                case TaskService.MSG_UPDATE_DM_TASKS:
                    DMTasks newTasks = msg.getData().getParcelable(DMTasks.class.toString());
                    mHost.get().performUpdateDMTasks(newTasks);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void performUpdateDMProgress() {
        log("performUpdateDMProgress progress:" + mDMTasks);
        if (activityVisible)
            updateTimers();
        else
            mDMTasks.updateProcess();
    }

    private void performUpdateDMTasks(DMTasks newTasks) {
        if (newTasks != null) {
            log("performUpdateDMTasks: obtaining service DM Tasks : " + newTasks.size());
            if (newTasks.size() > mDMTasks.size()) {
                log("performUpdateDMTasks: refreshing tasks:" + newTasks);
                mDMTasks.replaceTasks(newTasks);
                //mDMTasks.setTasksCompleted(mTaskItemCompleted);
                updateTimers();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        
        this.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        //WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);

        activityVisible = true;

        //setup actionbar icon
		/*
        ActionBar actionBar= this.getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME, ActionBar.DISPLAY_SHOW_HOME);
        actionBar.setIcon(R.drawable.tuba);
        */

		//setup toolbar
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		//toolbar.setTitle(mBasicNoteData.getNote().getTitle());
		setSupportActionBar(toolbar);
		getSupportActionBar().setIcon(R.drawable.tuba);

		final SymphonyArrayAdapter adapter = new SymphonyArrayAdapter(this, this, mDMTimers, mDMTasks, new OnDMTimerInteractionListener() {
            @Override
            public void onDMTimerInteraction(DMTimerRec item, int position) {
                log("OnDMTimerInteractionListener: received event");
                long clickTime = System.currentTimeMillis();
                if (clickTime - mLastClickTime > LIST_CLICK_DELAY) {
                    mLastClickTime = clickTime;
                    VibratorHelper.getInstance(MainActivity.this).shortVibrate();
                    performTimerAction(mDMTimers.get(position));
                } else
                    log("OnDMTimerInteractionListener: skipped event");
            }
        });
        mListViewSelector = adapter.getListViewSelector();

        mTimersListView = (ListView)findViewById(R.id.main_list_view);
        mTimersListView.setAdapter(adapter);

        // Update List
        loadTimers();
        updateTimers();

        AssetsHelper.listAssets(this, "pre_inst_images");
    }

    @Override
    protected void onDestroy() {
    	DBHelper.clearInstance();
    	MediaPlayerHelper.getInstance(this).release();
        mTaskServiceManager.unbindService();
        mTaskServiceManager = null;

    	super.onDestroy();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	activityVisible = false;
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	activityVisible = true;

    	if (DBHelper.getInstance(this).getDBDataChanged()) {
    		loadTimers();
    		DBHelper.getInstance(this).resetDBDataChanged();
    	}

    	updateTimers();
        mTaskServiceManager.updateServiceTasks(mDMTasks);

        //prevent immediate click after displaying
        mLastClickTime = System.currentTimeMillis();
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
                    Intent preferencesIntent = new Intent(this, SettingsActivity.class);
                    startActivity(preferencesIntent);
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

    private void performEditTimer(DMTimerRec dmTimerRec) {
    	startAddItemActivity(dmTimerRec);
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

    private void loadTimers() {
    	 DBHelper.getInstance(this).fillTimers(mDMTimers);
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

    private void updateTimers() {
    	SymphonyArrayAdapter adapter = (SymphonyArrayAdapter)mTimersListView.getAdapter();
    	adapter.notifyDataSetChanged();
    	checkTimerSelection();
    }
    
    private void performMediaCleanup() {
        List<String> mediaNameList = DBHelper.getInstance(getApplicationContext()).getMediaFileNameList();
        MediaStorageHelper.getInstance(getApplicationContext()).cleanupMedia(mediaNameList);
    }

    private void performInsertTimer(DMTimerRec dmTimerRec) {
		try {
			long retVal = DBHelper.getInstance(this).insertTimer(dmTimerRec);
			if (retVal > 0) {
                performMediaCleanup();
				loadTimers();
				updateTimers();  
			}
		} catch (Exception e) {			
			Toast.makeText(this, String.format(getResources().getString(R.string.error_saving_timer), e.getMessage()), Toast.LENGTH_SHORT).show();
		}
    }
    
    private void performDeleteTimer(DMTimerRec dmTimerRec) {    	
		try {
			long retVal = DBHelper.getInstance(this).deleteTimer(dmTimerRec.mId);
			if (retVal > 0) {
                performMediaCleanup();
				loadTimers();
				updateTimers();  
			}
		} catch (Exception e) {			
			Toast.makeText(this, String.format(getResources().getString(R.string.error_saving_timer), e.getMessage()), Toast.LENGTH_SHORT).show();
		}
    }
    
    private void performMoveUpTimer(DMTimerRec dmTimerRec) {
		try {
			boolean retVal = DBHelper.getInstance(this).moveTimerUp(dmTimerRec.mOrderId);
			if (retVal) {
				loadTimers();
				updateTimers();

                int pos = mDMTimers.getPosById(dmTimerRec.mId);
                if (pos != -1) {
                    mListViewSelector.setSelectedView(pos);
                    mTimersListView.smoothScrollToPositionFromTop(pos, 0);
                }
            }
		} catch (Exception e) {			
			Toast.makeText(this, String.format(getResources().getString(R.string.error_saving_timer), e.getMessage()), Toast.LENGTH_SHORT).show();
		}
    }
    
    private void performMoveDownTimer(DMTimerRec dmTimerRec) {
		try {
			boolean retVal = DBHelper.getInstance(this).moveTimerDown(dmTimerRec.mOrderId);
			if (retVal) {
				loadTimers();
				updateTimers();

                int pos = mDMTimers.getPosById(dmTimerRec.mId);
                if (pos != -1) {
                    mListViewSelector.setSelectedView(pos);
                    mTimersListView.smoothScrollToPositionFromTop(pos, 0);
                }
			}
		} catch (Exception e) {			
			Toast.makeText(this, String.format(getResources().getString(R.string.error_saving_timer), e.getMessage()), Toast.LENGTH_SHORT).show();
		}
    }
    
    private void performUpdateTimer(DMTimerRec dmTimerRec) {
		try {
			long retVal = DBHelper.getInstance(this).updateTimer(dmTimerRec);
			if (retVal > 0) {
				loadTimers();
				updateTimers();  
			}
		} catch (Exception e) {    				
			Toast.makeText(this, String.format(getResources().getString(R.string.error_saving_timer), e.getMessage()), Toast.LENGTH_SHORT).show();
		}
    }
    
    
    private void performTaskCompleted(DMTaskItem dmTaskItem) {
        log("performTaskCompleted");
    	//bring activity to front
    	if (!activityVisible) {
    		Intent intent = new Intent(getApplicationContext(), this.getClass());
    		intent.setComponent(new ComponentName(this.getPackageName(), this.getClass().getName()));
    		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);    		
    		getApplicationContext().startActivity(intent);   
    		//Log.d("MainActivity", "Activity was not visible, bringing to front");
    	}
    	
    	//prevent from sleeping while not turned off
    	getWindow().addFlags(WINDOW_SCREEN_ON_FLAGS);

    	//save history
    	DBHelper.getInstance(this).insertTimerHistory(dmTaskItem);

    	//play sound
    	MediaPlayerHelper.getInstance(this).startSoundFile(dmTaskItem.getSoundFile());
    	
    	//vibrate
    	boolean preferencesVibrate = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_vibrate", false);
    	if (preferencesVibrate)
    		VibratorHelper.getInstance(this).vibrate();
    }

    private void performTimerAction(DMTimerRec dmTimerRec) {
    	DMTaskItem taskItem = mDMTasks.getTaskItemById(dmTimerRec.mId);
        log("performTimerAction");
    	
    	if (null == taskItem) {
    		DMTaskItem newTaskItem = mDMTasks.addTaskItem(dmTimerRec);
    		//newTaskItem.setTaskItemCompleted(mTaskItemCompleted);
    		newTaskItem.startProcess();
    		
    		mDMTasks.add(newTaskItem);
    	} else {
    		updateTimers();
    		
    		mDMTasks.remove(taskItem);
    		
    		// inactive timer or no timers
    		if (mDMTasks.getStatus() != DMTasks.STATUS_COMPLETED) {
                log("no more timter on inactive timer:" + mDMTasks);
    			//stop sound
        		MediaPlayerHelper.getInstance(this).stop();
        		//stop vibrating
        		VibratorHelper.getInstance(this).cancel();
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
    					performInsertTimer(newTimer);
    					break;
    				case (EDIT_ITEM_RESULT_CODE):
    					performUpdateTimer(newTimer);
    					break;    					
    			}
    		}
    	}
    }
}
