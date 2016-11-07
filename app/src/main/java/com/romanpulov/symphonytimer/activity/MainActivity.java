package com.romanpulov.symphonytimer.activity;

import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
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


public class MainActivity extends ActionBarActivity {
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
	private DMTimers mDMTimers = new DMTimers();
	private DMTasks mDMTasks = new DMTasks();

    //UI
	private ListView mTimersListView = null;
    private long mLastClickTime;
    private boolean activityVisible = false;
	
	private DMTaskItem.OnTaskItemCompleted mTaskItemCompleted = new DMTaskItem.OnTaskItemCompleted() {
		@Override
		public void OnTaskItemCompletedEvent(
				DMTaskItem dmTaskItem) {
			performTaskCompleted(dmTaskItem);
		}
	};

    private void updateServiceDMTasks() {
        Message msg = Message.obtain(null, TaskService.MSG_UPDATE_DM_TASKS, 0, 0);
        msg.replyTo = mMessenger;
        Bundle bundle = new Bundle();
        bundle.putParcelable(DMTasks.class.toString(), mDMTasks.createParcelableCopy());
        msg.setData(bundle);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** Messenger for communicating with the service. */
    Messenger mService = null;

    /** Flag indicating whether we have called bind on the service. */
    boolean mServiceBound;

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = new Messenger(service);
            mServiceBound = true;

            log ("onServiceConnected");

            updateServiceDMTasks();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mServiceBound = false;

            log ("onServiceDisconnected");
        }
    };

    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TaskService.MSG_UPDATE_DM_PROGRESS:
                    if (activityVisible) {
                        updateTimers();
                    } else {
                        mDMTasks.updateProcess();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());


    private void updateServiceTasks() {
        Intent serviceIntent = new Intent(this, TaskService.class);

        //no more tasks
        if (mServiceBound && mDMTasks.size() == 0) {
            if (mConnection != null)
                unbindService(mConnection);
            mServiceBound = false;
            stopService(serviceIntent);
            return;
        }

        //tasks, not bound
        if ((!mServiceBound) && mDMTasks.size() > 0) {
            serviceIntent.putExtra(DMTasks.class.toString(), mDMTasks.createParcelableCopy());
            startService(serviceIntent);
            bindService(new Intent(this, TaskService.class), mConnection, Context.BIND_AUTO_CREATE);
            return;
        }

        //tasks, bound
        if (mServiceBound && mDMTasks.size() > 0) {
            updateServiceDMTasks();
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
        
		final SymphonyArrayAdapter adapter = new SymphonyArrayAdapter(this, mDMTimers, mDMTasks);
		
		getTimersListView().setAdapter(adapter);
		getTimersListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				//action
                long clickTime = System.currentTimeMillis();
                if (clickTime - mLastClickTime > LIST_CLICK_DELAY) {
                    mLastClickTime = clickTime;
                    VibratorHelper.getInstance(MainActivity.this).shortVibrate();
                    performTimerAction((DMTimerRec) parent.getItemAtPosition(position));
                }
			}			
		});
		
        // Update List
        loadTimers();
        updateTimers();

        AssetsHelper.listAssets(this, "pre_inst_images");
    }
    
    @Override
    public void onBackPressed() {
    	if (0 == mDMTasks.size()) {
    		super.onBackPressed();
    	}
    }
    
    @Override
    protected void onDestroy() {
    	DBHelper.clearInstance();
    	MediaPlayerHelper.getInstance(this).release();
        if (mServiceBound)
            unbindService(mConnection);
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
        updateServiceTasks();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
        outState.putParcelable(mDMTasks.getClass().toString(), mDMTasks);
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);

        //restore tasks
        mDMTasks = savedInstanceState.getParcelable(mDMTasks.getClass().toString());

        //restore code references
        ((SymphonyArrayAdapter)getTimersListView().getAdapter()).setTasks(mDMTasks);
        mDMTasks.setTasksCompleted(mTaskItemCompleted);

        //update UI
		mDMTasks.updateProcess();
		updateTimers();
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
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
    		ContextMenuInfo menuInfo) {
    	menu.add(Menu.NONE, CONTEXT_MENU_EDIT, Menu.NONE, R.string.action_edit);
    	menu.add(Menu.NONE, CONTEXT_MENU_DELETE, Menu.NONE, R.string.action_delete);
        menu.add(Menu.NONE, CONTEXT_MENU_MOVE_UP, Menu.NONE, R.string.action_move_up);
        menu.add(Menu.NONE, CONTEXT_MENU_MOVE_DOWN, Menu.NONE, R.string.action_move_down);
    	super.onCreateContextMenu(menu, v, menuInfo);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	if (mDMTasks.getStatus() != DMTasks.STATUS_IDLE)
    		return super.onContextItemSelected(item);
    	
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	DMTimerRec actionTimerRec = (DMTimerRec)getTimersListView().getAdapter().getItem(info.position);
    	switch (item.getItemId()) {
    		case (CONTEXT_MENU_EDIT):
    			editTimer(actionTimerRec);
    			return true;    			
    		case (CONTEXT_MENU_DELETE):
    			AlertOkCancelDialogFragment deleteDialog = AlertOkCancelDialogFragment.newAlertOkCancelDialog(actionTimerRec, R.string.question_are_you_sure);
    			deleteDialog.setOkButtonClick(new AlertOkCancelDialogFragment.OnOkButtonClick() {
                    @Override
                    public void OnOkButtonClickEvent(DialogFragment dialog) {
                        DMTimerRec dmTimerRec = dialog.getArguments().getParcelable(DMTimerRec.class.toString());
                        if (null != dmTimerRec) {
                            performDeleteTimer(dmTimerRec);
                        }
                    }
                });
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
    	if (null == mTimersListView) {
    		mTimersListView = (ListView)findViewById(R.id.main_list_view);
    	}
    	return mTimersListView;
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
    	
    	if (null == taskItem) {
    		DMTaskItem newTaskItem = mDMTasks.addTaskItem(dmTimerRec);
    		newTaskItem.setTaskItemCompleted(mTaskItemCompleted);
    		newTaskItem.startProcess();
    		
    		mDMTasks.add(newTaskItem);
    	} else {
    		updateTimers();
    		
    		mDMTasks.remove(taskItem);
    		
    		// inactive timer or no timers
    		if (mDMTasks.getStatus() == DMTasks.STATUS_IDLE) {
    			//stop sound
        		MediaPlayerHelper.getInstance(this).stop();
        		//stop vibrating
        		VibratorHelper.getInstance(this).cancel();
    			//enable screen fading
    			getWindow().clearFlags(WINDOW_SCREEN_ON_FLAGS);
    		}
    	}

        updateServiceTasks();
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
