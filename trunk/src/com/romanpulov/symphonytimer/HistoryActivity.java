package com.romanpulov.symphonytimer;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.view.WindowManager;
import android.widget.ListView;

public class HistoryActivity extends Activity {
	
	public static String TIMERS_NAME = "timers";
	
	private DMTimerHistList mDMimerHistList = new DMTimerHistList();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
		
		DBHelper.getInstance(this).updateHistList(mDMimerHistList);		
			
		ArrayList<DMTimerRec> timers = getIntent().getExtras().getParcelableArrayList(HistoryActivity.TIMERS_NAME);
		DMTimers dmTimers = new DMTimers();
		for (DMTimerRec timer : timers) {
			dmTimers.add(timer);
		}
		
		HistoryArrayAdapter adapter = new HistoryArrayAdapter(this, mDMimerHistList, dmTimers);
		((ListView)findViewById(R.id.history_list_view)).setAdapter(adapter);		
		    
	}

}
