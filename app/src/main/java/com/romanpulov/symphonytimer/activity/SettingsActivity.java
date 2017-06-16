package com.romanpulov.symphonytimer.activity;

import com.romanpulov.symphonytimer.R;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

public class SettingsActivity extends ActionBarActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME, ActionBar.DISPLAY_SHOW_HOME);
            actionBar.setIcon(R.drawable.tuba);
        }
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        View view  = super.onCreateView(parent, name, context, attrs);

        if (view != null) {
            ListView preferencesListView = (ListView) view.findViewById(android.R.id.list);
            preferencesListView.setPadding(0, 0, 0, 0);
        }

        return view;
    }
}