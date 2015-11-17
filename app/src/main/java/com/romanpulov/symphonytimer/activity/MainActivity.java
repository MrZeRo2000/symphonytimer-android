package com.romanpulov.symphonytimer.activity;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.DialogFragment;
import android.app.Notification;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.romanpulov.symphonytimer.helper.MediaStorageHelper;
import com.romanpulov.symphonytimer.utils.AlarmManagerBroadcastReceiver;
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


public class MainActivity extends ActionBarActivity {
	public static final int ADD_ITEM_RESULT_CODE = 1;
	public static final int EDIT_ITEM_RESULT_CODE = 2;
	
	public static final int CONTEXT_MENU_ADD = Menu.FIRST + 1;
	public static final int CONTEXT_MENU_EDIT = Menu.FIRST + 2;
	public static final int CONTEXT_MENU_DELETE = Menu.FIRST + 3;
	public static final int CONTEXT_MENU_MOVE_UP = Menu.FIRST + 4;
	public static final int CONTEXT_MENU_MOVE_DOWN = Menu.FIRST + 5;
	
	private boolean activityVisible = false;
	
	private DMTimers dmTimers = new DMTimers();
	private DMTasks dmTasks = new DMTasks();
	private ScheduledThreadPoolExecutor scheduleExecutor = new ScheduledThreadPoolExecutor(2);
	
	private AlarmManagerBroadcastReceiver mAlarm;
	
	private ListView mLV = null;
	
	private final ScheduleHelper mScheduleHelper = new ScheduleHelper();
	
	private Runnable taskRunnable = new Runnable () {
		@Override
		public void run() {
			//updateTimers();
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (activityVisible) {
						updateTimers();
					} else {
						dmTasks.updateProcess();
					}						
				}
			});			
		}
	};
	
	private DMTaskItem.OnTaskItemCompleted mTaskItemCompleted = new DMTaskItem.OnTaskItemCompleted() {
		@Override
		public void OnTaskItemCompletedEvent(
				DMTaskItem dmTaskItem) {
			performTaskCompleted(dmTaskItem);
		}
	};
	
	private class ScheduleHelper {
		
		private ScheduledFuture<?> scheduleExecutorTask;
		
		public void startScheduler() {
			if (null == scheduleExecutorTask) {
				scheduleExecutorTask = scheduleExecutor.scheduleWithFixedDelay(taskRunnable, 0, 1, TimeUnit.SECONDS);
			}			
		}
		
		public void stopScheduler() {
			if (null != scheduleExecutorTask) {
				scheduleExecutorTask.cancel(false);
				scheduleExecutorTask = null;
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
		
        // Register context menu
        registerForContextMenu(getTimersListView());
        
        activityVisible = true;

        //setup actionbar icon
        ActionBar actionBar= this.getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME, ActionBar.DISPLAY_SHOW_HOME);
        actionBar.setIcon(R.drawable.tuba);
        
        /*
        // Set background wallpaper
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        
        RelativeLayout layout = (RelativeLayout) findViewById (R.id.main_layout);        
        layout.setBackgroundDrawable(wallpaperDrawable);
        */       
        
		final SymphonyArrayAdapter adapter = new SymphonyArrayAdapter(this, dmTimers, dmTasks);
		
		getTimersListView().setAdapter(adapter);
		getTimersListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				//action
				performTimerAction((DMTimerRec)parent.getItemAtPosition(position));
			}			
		});
		
        // Update List
        loadTimers();
        updateTimers();

        AssetsHelper.listAssets(this, "pre_inst_images");
    }
    
    @Override
    public void onBackPressed() {
    	if (0 == dmTasks.size()) {
    		super.onBackPressed();
    	}
    }
    
    @Override
    protected void onDestroy() {
    	DBHelper.getInstance(this).closeDB();
    	MediaPlayerHelper.getInstance(this).release();
    	scheduleExecutor.shutdown();  
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
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
        outState.putParcelable(dmTasks.getClass().toString(), dmTasks);
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);

        //restore tasks
        dmTasks = savedInstanceState.getParcelable(dmTasks.getClass().toString());

        //restore code references
        ((SymphonyArrayAdapter)getTimersListView().getAdapter()).setTasks(dmTasks);
        dmTasks.setTasksCompleted(mTaskItemCompleted);

        //update UI
		dmTasks.updateProcess();
		updateTimers();

		//update scheduler
		if (dmTasks.size() > 0) {
            mScheduleHelper.startScheduler();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.action_add:
    		startAddItemActivity(new DMTimerRec());
    		return true;
    	case R.id.action_preferences:
        	if (0 != dmTasks.size()) {
        		Toast.makeText(getApplicationContext(), this.getString(R.string.action_not_allowed), Toast.LENGTH_SHORT).show();
        		return super.onOptionsItemSelected(item);
        	}    		
    		Intent perferencesIntent = new Intent(this, SettingsActivity.class);
    		startActivity(perferencesIntent);
    		return true;
    	case R.id.action_history:
    		startHistoryActivity();
    		return true;
    	default:
  			return super.onOptionsItemSelected(item);
    	}
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
    		ContextMenuInfo menuInfo) {
    	menu.add(Menu.NONE, CONTEXT_MENU_EDIT, Menu.NONE, R.string.action_edit);
    	menu.add(Menu.NONE, CONTEXT_MENU_DELETE, Menu.NONE, R.string.action_delete);
        menu.add(Menu.NONE, CONTEXT_MENU_MOVE_UP, Menu.NONE, R.string.action_move_up);
        menu.add(Menu.NONE, CONTEXT_MENU_MOVE_DOWN, Menu.NONE, R.string.action_move_down);
    	super.onCreateContextMenu(menu, v, menuInfo);
    }
    
    public AlertOkCancelDialogFragment.OnOkButtonClick onDeleteOkButtonClick = new AlertOkCancelDialogFragment.OnOkButtonClick() {
		@Override
		public void OnOkButtonClickEvent(DialogFragment dialog) {
			DMTimerRec dmTimerRec = dialog.getArguments().getParcelable(DMTimerRec.class.toString());
			if (null != dmTimerRec) {
				performDeleteTimer(dmTimerRec);
			}
		}
	};		
   
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	if (0 != dmTasks.size()) {
    		Toast.makeText(getApplicationContext(), this.getString(R.string.action_not_allowed), Toast.LENGTH_SHORT).show();
    		return super.onContextItemSelected(item);
    	}
    	
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	DMTimerRec actionTimerRec = (DMTimerRec)getTimersListView().getAdapter().getItem(info.position);
    	switch (item.getItemId()) {
    		case (CONTEXT_MENU_EDIT):
    			editTimer(actionTimerRec);
    			return true;    			
    		case (CONTEXT_MENU_DELETE):
    			AlertOkCancelDialogFragment deleteDialog = AlertOkCancelDialogFragment.newAlertOkCancelDialog(actionTimerRec, R.string.question_are_you_sure);
    			deleteDialog.setOkButtonClick(onDeleteOkButtonClick);
    			deleteDialog.show(getFragmentManager(), null);
    			return true;
    		case (CONTEXT_MENU_MOVE_UP):
    			performMoveUpTimer(actionTimerRec);
    			return true;    			
    		case (CONTEXT_MENU_MOVE_DOWN):
    			performMoveDownTimer(actionTimerRec);
    			return true; 			
    		default:
    			return super.onContextItemSelected(item);
    	}
    }
    
    private void editTimer(DMTimerRec dmTimerRec) {
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
    	startHistoryIntent.putExtra(HistoryActivity.TIMERS_NAME, dmTimers);
        startActivity(startHistoryIntent);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void loadTimers() {
    	 DBHelper.getInstance(this).fillTimers(dmTimers);    	
    }
    
    private void checkTimerSelection() {
    	DMTaskItem taskItem = dmTasks.getFirstTaskItemCompleted();
    	if (null != taskItem) {
    		DMTimerRec timerRec = dmTimers.getItemById(taskItem.getId());
    		if (null != timerRec) {
    			int index = dmTimers.indexOf(timerRec);
    			if ( (index<getTimersListView().getFirstVisiblePosition()) || (index>getTimersListView().getLastVisiblePosition()) ) {
    				getTimersListView().setSelection(index);
    			}
    		}    		
    	}
    }
    
    private void updateTimers() {    	
    	SymphonyArrayAdapter adapter = (SymphonyArrayAdapter)getTimersListView().getAdapter();
    	adapter.notifyDataSetChanged();
    	checkTimerSelection();
    }
    
    private ListView getTimersListView () {
    	if (null == mLV) {
    		mLV = (ListView)findViewById(R.id.main_list_view);
    	}
    	return mLV;
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
    	//bring activity to front
    	if (!activityVisible) {
    		Intent intent = new Intent(getApplicationContext(), this.getClass());
    		intent.setComponent(new ComponentName(this.getPackageName(), this.getClass().getName()));
    		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);    		
    		getApplicationContext().startActivity(intent);   
    		//Log.d("MainActivity", "Activity was not visible, bringing to front");
    	}
    	
    	//prevent from sleeping while not turned off
    	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    	
    	//save history
    	DBHelper.getInstance(this).insertTimerHistory(dmTaskItem);

    	//play sound
    	MediaPlayerHelper.getInstance(this).startSoundFile(dmTaskItem.getSoundFile());
    	
    	//vibrate
    	boolean preferencesVibrate = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_vibrate", false);
    	if (preferencesVibrate)
    		VibratorHelper.getInstance(this).vibrate();
    }

    private void updateNotification() {
    	NotificationManager notificationManager = 
				  (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
			     notificationIntent, 0);    			
		Notification.Builder mBuilder =
				new Notification.Builder(this)
        			.setSmallIcon(R.drawable.wait_notification)
                    .setAutoCancel(false)
                    .setOngoing(true)
        			.setContentTitle(this.getTitle())
        			.setContentText(dmTasks.getTaskTitles())
        			.setContentIntent(contentIntent);        		
		notificationManager.notify(0, mBuilder.build());		
    }
    
    private void performTimerAction(DMTimerRec dmTimerRec) {
    	
    	DMTaskItem taskItem = dmTasks.getTaskItemById(dmTimerRec.mId);
    	
    	if (null == taskItem) {
    		DMTaskItem newTaskItem = dmTasks.addTaskItem(dmTimerRec);//new DMTaskItem(dmTimerRec.id, dmTimerRec.time_sec);
    		newTaskItem.setTaskItemCompleted(mTaskItemCompleted);
    		newTaskItem.startProcess();
    		
    		dmTasks.add(newTaskItem);    		
    		
    		updateNotification();

            mScheduleHelper.startScheduler();

    		if (null == mAlarm) {
    			mAlarm = new AlarmManagerBroadcastReceiver();
    		}
    		mAlarm.setOnetimeTimer(getApplicationContext(), newTaskItem.getId(), newTaskItem.getTriggerAtTime());
    	} else {
    		//finalize
    		if (null != mAlarm) {
    			mAlarm.cancelAlarm(getApplicationContext(), taskItem.getId());
    		}
    		updateTimers();
    		
    		dmTasks.remove(taskItem);
    		
    		// inactive timer or no timers
    		if (null == dmTasks.getFirstTaskItemCompleted()) {
    			//stop sound
        		MediaPlayerHelper.getInstance(this).stop();
        		//stop vibrating
        		VibratorHelper.getInstance(this).cancel();
    			//enable screen fading
    			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);        		
    		}
    		
    		// no timers
    		if (0 == dmTasks.size()) {		
    			//cancel scheduler  			
    			mScheduleHelper.stopScheduler();

    			// cancel notifications
    			NotificationManager notificationManager = 
    					  (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    			notificationManager.cancel(0);    			
    		} else {
    			// update notification if any active timers still exist
    			updateNotification();
    		}
    	}
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
