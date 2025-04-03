package com.romanpulov.symphonytimer.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

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

public class HistoryActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    //storage
    public final static String TIMERS_NAME = "timers";
    private final static String HISTORY_NAVIGATION_INDEX = "history_navigation_index";

    private DMTimers mDMTimers;

    private HistoryPagerAdapter mAdapter;

    private int mSelectedHistoryIndex;

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //setSelectedHistoryIndex(position);
        mSelectedHistoryIndex = position;
        mAdapter.setSelectedHistoryIndex(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private class HistoryPagerAdapter extends FragmentStatePagerAdapter {
        final String[] mFragmentTags;
        final HistoryFragment[] mFragments;

        HistoryPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            mFragmentTags = new String[getCount()];
            mFragments = new HistoryFragment[getCount()];
        }

        @Override
        @NonNull
        public Fragment getItem(int index) {
            /*
            HistoryFragment historyFragment = HistoryFragment.newInstance(HISTORY_FRAGMENT_CLASS_LIST.get(index), mDMTimers,  mSelectedHistoryIndex);
            mFragments[index] = historyFragment;
            //historyFragment.setHistoryFilterId(mViewPager.getCurrentItem());
            historyFragment.setHistoryFilterId(mSelectedHistoryIndex);
            return historyFragment;

             */
            return null;
        }

        @Override
        public int getCount() {
            //return HISTORY_FRAGMENT_CLASS_LIST.size();
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
                    return getResources().getString(R.string.tab_history_dynamics);
            default:
                return null;
            }
        }

        private void setSelectedHistoryIndex(int position) {
            for (Fragment fragment: mFragments) {
                if (fragment != null) {
                    ((HistoryFragment) fragment).setHistoryFilterId(position);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            mDMTimers = bundle.getParcelable(HistoryActivity.TIMERS_NAME);
        }

        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);

        //setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME, ActionBar.DISPLAY_SHOW_HOME);
        }

        if (null != savedInstanceState) {
            mSelectedHistoryIndex = savedInstanceState.getInt(HISTORY_NAVIGATION_INDEX);
        }

        ViewPager viewPager = findViewById(R.id.pager);
        mAdapter = new HistoryPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(HISTORY_NAVIGATION_INDEX, mSelectedHistoryIndex);
    }
}
