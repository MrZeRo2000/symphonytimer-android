package com.romanpulov.symphonytimer.adapter;

import android.support.v7.view.ActionMode;
import android.widget.BaseAdapter;

/**
 * Created by romanpulov on 11.11.2016.
 */

public class ListViewSelector {
    private final BaseAdapter mAdapter;
    private final ActionMode.Callback mActionModeCallback;
    private ActionMode mActionMode;


    public ListViewSelector(BaseAdapter adapter, ActionMode.Callback actionModeCallback) {
        mAdapter = adapter;
        mActionModeCallback = actionModeCallback;
    }
}
