package com.romanpulov.symphonytimer;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class HistoryActivity extends Activity {
	
	public static String TIMERS_NAME = "timers";
	
	private DMTimerHistList mDMimerHistList = new DMTimerHistList();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		
		DBHelper.getInstance(this).updateHistList(mDMimerHistList);
		
		DMTimers dmTimers = getIntent().getExtras().getParcelable(HistoryActivity.TIMERS_NAME);
		
		HistoryArrayAdapter adapter = new HistoryArrayAdapter(this, mDMimerHistList, dmTimers);
		((ListView)findViewById(R.id.history_list_view)).setAdapter(adapter);		
		    
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.history, menu);
		return true;
	}

}
