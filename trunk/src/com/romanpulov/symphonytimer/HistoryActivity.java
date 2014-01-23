package com.romanpulov.symphonytimer;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;

public class HistoryActivity extends ActionBarActivity {
	
	public static String TIMERS_NAME = "timers";
	
	private DMTimerHistList mDMimerHistList = new DMTimerHistList();
	
	private ViewPager mViewPager;
	private TabsPagerAdapter mAdapter;
	
	private ActionBar.TabListener mTabListener = new ActionBar.TabListener() {

		@Override
		public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction arg1) {
			// TODO Auto-generated method stub
			mViewPager.setCurrentItem(tab.getPosition());
		}

		@Override
		public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	private class TabsPagerAdapter extends FragmentPagerAdapter {
		 
	    public TabsPagerAdapter(FragmentManager fm) {
	        super(fm);
	    }
	 
	    @Override
	    public Fragment getItem(int index) {
	 
	        switch (index) {
	        case 0:
	            // Top Rated fragment activity
	            return new HistoryListFragment();
	        case 1:
	            // Games fragment activity
	        	return new HistoryListFragment();
	        }
	 
	        return null;
	    }
	 
	    @Override
	    public int getCount() {
	        // get item count - equal to number of tabs
	        return 2;
	    }
	 
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
		
		ActionBar actionBar = this.getSupportActionBar();
		
		// Initilization
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
 
        mViewPager.setAdapter(mAdapter);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        
 
        // Adding Tabs
        actionBar.addTab(actionBar.newTab().setText("Tab 1").setTabListener(mTabListener));
        actionBar.addTab(actionBar.newTab().setText("Tab 2").setTabListener(mTabListener));
        
        

//
//		DBHelper.getInstance(this).updateHistList(mDMimerHistList);		
//			
//		ArrayList<DMTimerRec> timers = getIntent().getExtras().getParcelableArrayList(HistoryActivity.TIMERS_NAME);
//		DMTimers dmTimers = new DMTimers();
//		for (DMTimerRec timer : timers) {
//			dmTimers.add(timer);
//		}
//		
//		HistoryArrayAdapter adapter = new HistoryArrayAdapter(this, mDMimerHistList, dmTimers);
//		((ListView)findViewById(R.id.history_list_view)).setAdapter(adapter);		
		    
	}

}
