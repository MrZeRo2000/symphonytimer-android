package com.romanpulov.symphonytimer.adapter;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.View;
import android.widget.BaseAdapter;

/**
 * Created by romanpulov on 11.11.2016.
 */

public class ListViewSelector {
    private final BaseAdapter mAdapter;
    private final ActionMode.Callback mActionModeCallback;
    private ActionMode mActionMode;

    public ActionMode getActionMode() {
        return mActionMode;
    }

    private int mSelectedItemPos = -1;

    public int getSelectedItemPos() {
        return mSelectedItemPos;
    }

    public ListViewSelector(BaseAdapter adapter, ActionMode.Callback actionModeCallback) {
        mAdapter = adapter;
        mActionModeCallback = actionModeCallback;
    }

    public void startActionMode(View v, int position) {
        if (mSelectedItemPos == -1) {
            // for title to show correctly
            mSelectedItemPos = position;
            ActionBarActivity activity = (ActionBarActivity) v.getContext();
            mActionMode = activity.startSupportActionMode(mActionModeCallback);

            if (mActionMode != null) {
                setSelectedView(position);
                mAdapter.notifyDataSetChanged();
            } else
                //rejected creation
                mSelectedItemPos = - 1;

        } else
            setSelectedView(position);
    }

    public void setSelectedView(int position) {
        if ((mSelectedItemPos != position) && (mSelectedItemPos != -1)) {
            mSelectedItemPos = position;
            mAdapter.notifyDataSetChanged();
        }
    }

    public void destroyActionMode() {
        mSelectedItemPos = -1;
        mActionMode = null;
        mAdapter.notifyDataSetChanged();
    }
}
