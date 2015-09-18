package com.romanpulov.symphonytimer;


import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;

public class HistoryActivity extends ActionBarActivity implements ActionBar.OnNavigationListener {

    private static String HISTORY_NAVIGATION_INDEX;

    public static String TIMERS_NAME = "timers";

    private DMTimers mDMTimers;
    private ActionBar mActionBar;
    private ViewPager mViewPager;
    private HistoryPagerAdapter mAdapter;

    public DMTimers getTimers() {
        return mDMTimers;
    }

    public interface HistoryFilterHandler {
        public abstract void onHistoryFilterChange(int filterId);
    }

    private class HistoryPagerAdapter extends FragmentPagerAdapter {

        public HistoryListFragment mHistoryListFragment;
        public HistoryTopFragment mHistoryTopFragment;

        public String[] mFragmentTags;

        public HistoryPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentTags = new String[getCount()];
        }

        @Override
        public Fragment getItem(int index) {

            HistoryFragment historyFragment = null;

            switch (index) {
            case 0:
                mHistoryListFragment = new HistoryListFragment();
                historyFragment = mHistoryListFragment;
                break;
            case 1:
                mHistoryTopFragment = new HistoryTopFragment();
                historyFragment = mHistoryTopFragment;
                break;
            }

            historyFragment.setHistoryFilterId(mViewPager.getCurrentItem());
            return historyFragment;
        }

        @Override
        public int getCount() {
            // get item count - equal to number of tabs
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // TODO Auto-generated method stub
            switch (position) {
            case 0:
                return getResources().getString(R.string.tab_history_list);
            case 1:
                return getResources().getString(R.string.tab_history_top);
            default:
                return null;
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // TODO Auto-generated method stub
            HistoryFragment historyFragment = (HistoryFragment)super.instantiateItem(container, position);
            mFragmentTags[position] = historyFragment.getTag();
            return historyFragment;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        final ArrayList<DMTimerRec> timers = this.getIntent().getExtras().getParcelableArrayList(HistoryActivity.TIMERS_NAME);
        mDMTimers = new DMTimers();
        for (DMTimerRec timer : timers) {
            mDMTimers.add(timer);
        }

        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);

        mActionBar = this.getSupportActionBar();
        mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME, ActionBar.DISPLAY_SHOW_HOME);
        mActionBar.setHomeButtonEnabled(true);
        //mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setIcon(R.drawable.tuba);
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.history_filter, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mActionBar.setListNavigationCallbacks(spinnerAdapter, this);  
        //mActionBar.setSelectedNavigationItem(0);		
        if (null != savedInstanceState) {
            mActionBar.setSelectedNavigationItem(savedInstanceState.getInt(HISTORY_NAVIGATION_INDEX));
        }

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new HistoryPagerAdapter(getSupportFragmentManager()); 
        mViewPager.setAdapter(mAdapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
        outState.putInt(HISTORY_NAVIGATION_INDEX, mActionBar.getSelectedNavigationIndex());
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        // TODO Auto-generated method stub
        //Fragment fr = getSupportFragmentManager().findFragmentByTag("android:switcher:"+R.id.pager+":0");
        for (int i = 0; i < mAdapter.getCount(); i++) {
            HistoryFragment historyFragment = (HistoryFragment)getSupportFragmentManager().findFragmentByTag(mAdapter.mFragmentTags[i]);
            historyFragment.setHistoryFilterId(itemPosition);
        }
        return false;
    }

}
