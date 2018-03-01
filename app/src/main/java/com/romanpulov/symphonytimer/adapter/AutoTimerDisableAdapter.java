package com.romanpulov.symphonytimer.adapter;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.romanpulov.symphonytimer.R;

import java.util.Arrays;
import java.util.List;

/**
 * Adapter class for auto timer disable
 * Created by romanpulov on 28.02.2018.
 */

public class AutoTimerDisableAdapter extends ArrayAdapter<CharSequence> {

    private final List<CharSequence> mEntryValues;

    public AutoTimerDisableAdapter(Context context, int resId) {
        super(context, resId);

        CharSequence[] entries = context.getResources().getTextArray(R.array.auto_timer_disable_entries);
        mEntryValues = Arrays.asList(context.getResources().getTextArray(R.array.auto_timer_disable_entry_values));
        addAll(entries);
    }

    public int getPositionByValue(int value) {
        return mEntryValues.indexOf(String.valueOf(value));
    }

    public int getValueBySelection(String selection) {
        int selectionIndex = getPosition(selection);
        if (selectionIndex >= 0)
            return Integer.valueOf(mEntryValues.get(selectionIndex).toString());
        else
            return 0;
    }
}
