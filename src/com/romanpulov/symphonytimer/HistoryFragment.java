package com.romanpulov.symphonytimer;

import android.support.v4.app.Fragment;
import android.widget.ArrayAdapter;

public class HistoryFragment extends Fragment {
	
	protected int mHistoryFilterId = -1;
	protected ArrayAdapter<?> mAdapter;
	
	public void setHistoryFilterId(int historyFilterId) {
		mHistoryFilterId = historyFilterId;
	}	
}
