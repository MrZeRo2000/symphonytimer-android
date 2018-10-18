package com.romanpulov.symphonytimer.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.romanpulov.symphonytimer.fragment.HistoryDynamicsChartFragment;
import com.romanpulov.symphonytimer.fragment.HistoryFragment;
import com.romanpulov.symphonytimer.fragment.HistoryListFragment;
import com.romanpulov.symphonytimer.fragment.HistoryTopChartFragment;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.model.DMTimers;

import java.util.Arrays;
import java.util.List;

public class HistoryActivity extends AppCompatActivity implements ActionBar.OnNavigationListener, AdapterView.OnItemSelectedListener {
    //storage
    public final static String TIMERS_NAME = "timers";
    private final static String HISTORY_NAVIGATION_INDEX = "history_navigation_index";
    //underlying fragments for creation
    private final static List<Class<? extends HistoryFragment>> HISTORY_FRAGMENT_CLASS_LIST =
            Arrays.asList(
                    HistoryListFragment.class,
                    HistoryTopChartFragment.class,
                    HistoryDynamicsChartFragment.class
            );

    private DMTimers mDMTimers;

    private ViewPager mViewPager;
    private HistoryPagerAdapter mAdapter;

    private int mSelectedHistoryIndex;

    private void setSelectedHistoryIndex(int position) {
        mSelectedHistoryIndex = position;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            HistoryFragment historyFragment = (HistoryFragment)getSupportFragmentManager().findFragmentByTag(mAdapter.mFragmentTags[i]);
            if (historyFragment != null)
                historyFragment.setHistoryFilterId(position);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        setSelectedHistoryIndex(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private class HistoryPagerAdapter extends FragmentPagerAdapter {
        final String[] mFragmentTags;

        HistoryPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentTags = new String[getCount()];
        }

        @Override
        public Fragment getItem(int index) {
            //HistoryFragment historyFragment = HistoryFragment.newInstance(HISTORY_FRAGMENT_CLASS_LIST.get(index), mDMTimers,  mActionBar.getSelectedNavigationIndex());
            HistoryFragment historyFragment = HistoryFragment.newInstance(HISTORY_FRAGMENT_CLASS_LIST.get(index), mDMTimers,  mSelectedHistoryIndex);
            if (historyFragment != null)
                historyFragment.setHistoryFilterId(mViewPager.getCurrentItem());
            return historyFragment;
        }

        @Override
        public int getCount() {
            return HISTORY_FRAGMENT_CLASS_LIST.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.tab_history_list);
                case 1:
                    return getResources().getString(R.string.tab_history_top);
                case 2:
                    return getResources().getString(R.string.tab_history_dynamics);
            default:
                return null;
            }
        }

        @Override
        @NonNull
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
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

        //setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME, ActionBar.DISPLAY_SHOW_HOME);
            actionBar.setIcon(R.drawable.tuba);
        }

        /*
        mActionBar = this.getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME, ActionBar.DISPLAY_SHOW_HOME);
            mActionBar.setHomeButtonEnabled(true);
            //mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setIcon(R.drawable.tuba);
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        }
        */

        //ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.history_filter, android.R.layout.simple_spinner_item);
        //spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        /*
        mActionBar.setListNavigationCallbacks(spinnerAdapter, this);  
        if (null != savedInstanceState) {
            mActionBar.setSelectedNavigationItem(savedInstanceState.getInt(HISTORY_NAVIGATION_INDEX));
        }
        */

        if (null != savedInstanceState) {
            mSelectedHistoryIndex = savedInstanceState.getInt(HISTORY_NAVIGATION_INDEX);
        }

        mViewPager = findViewById(R.id.pager);
        mAdapter = new HistoryPagerAdapter(getSupportFragmentManager()); 
        mViewPager.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history_options, menu);

        MenuItem item = menu.findItem(R.id.spinner);
        Spinner spinner = (Spinner) item.getActionView();
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.history_filter, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(this);
        spinner.setSelection(mSelectedHistoryIndex);

        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(HISTORY_NAVIGATION_INDEX, mSelectedHistoryIndex);
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
