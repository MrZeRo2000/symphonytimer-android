package com.romanpulov.symphonytimer.adapter;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.View;
import android.widget.BaseAdapter;

import com.romanpulov.symphonytimer.model.DMTimerRec;

/**
 * Created by romanpulov on 11.11.2016.
 */

public class ListViewSelector {
    private final BaseAdapter mAdapter;
    private final ActionMode.Callback mActionModeCallback;
    private ActionMode mActionMode;
    private int mSelectedItemPos = -1;


    public ListViewSelector(BaseAdapter adapter, ActionMode.Callback actionModeCallback) {
        mAdapter = adapter;
        mActionModeCallback = actionModeCallback;
    }

    public void startActionMode(View v, int position) {
        if (mSelectedItemPos == -1) {
            setSelectedView(position);
            mSelectedItemPos = position;
            mAdapter.notifyDataSetChanged();

            ActionBarActivity activity = (ActionBarActivity) v.getContext();
            mActionMode = activity.startSupportActionMode(mActionModeCallback);
        } else
            setSelectedView(position);
    }

    public void setSelectedView(int position) {
        if ((mSelectedItemPos != position) && (mSelectedItemPos != -1)) {
            mSelectedItemPos = position;
            mAdapter.notifyDataSetChanged();
        }
    }
}
