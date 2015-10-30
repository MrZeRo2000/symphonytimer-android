package com.romanpulov.symphonytimer.activity;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;

import com.romanpulov.symphonytimer.fragment.HistoryFragment;
import com.romanpulov.symphonytimer.fragment.HistoryListFragment;
import com.romanpulov.symphonytimer.fragment.HistoryTopChartFragment;
import com.romanpulov.symphonytimer.fragment.HistoryTopFragment;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.model.DMTimers;

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
        void onHistoryFilterChange(int filterId);
    }

    private class HistoryPagerAdapter extends FragmentPagerAdapter {

        public HistoryListFragment mHistoryListFragment;
        public HistoryTopFragment mHistoryTopFragment;
        public HistoryTopChartFragment mHistoryTopChartFragment;

        public String[] mFragmentTags;

        public HistoryPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentTags = new String[getCount()];
        }

        @Override
        public Fragment getItem(int index) {
            HistoryFragment historyFragment;

            switch (index) {
            case 0:
                mHistoryListFragment = new HistoryListFragment();
                historyFragment = mHistoryListFragment;
                break;
            case 1:
                mHistoryTopFragment = new HistoryTopFragment();
                historyFragment = mHistoryTopFragment;
                break;
                case 2:
                    mHistoryTopChartFragment = HistoryTopChartFragment.newInstance(mDMTimers,  mActionBar.getSelectedNavigationIndex());
                    historyFragment = mHistoryTopChartFragment;
                    break;
                default:
                    return null;
            }

            historyFragment.setHistoryFilterId(mViewPager.getCurrentItem());
            return historyFragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.tab_history_list);
                case 1:
                    return getResources().getString(R.string.tab_history_top);
                case 2:
                    return getResources().getString(R.string.tab_history_top_chart);
            default:
                return null;
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            HistoryFragment historyFragment = (HistoryFragment)super.instantiateItem(container, position);
            mFragmentTags[position] = historyFragment.getTag();
            return historyFragment;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mDMTimers = this.getIntent().getExtras().getParcelable(HistoryActivity.TIMERS_NAME);

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
        for (int i = 0; i < mAdapter.getCount(); i++) {
            HistoryFragment historyFragment = (HistoryFragment)getSupportFragmentManager().findFragmentByTag(mAdapter.mFragmentTags[i]);
            if (historyFragment != null)
                historyFragment.setHistoryFilterId(itemPosition);
        }
        return false;
    }
}
