package com.romanpulov.symphonytimer.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.adapter.HistoryArrayAdapter;
import com.romanpulov.symphonytimer.helper.db.DBHelper;
import com.romanpulov.symphonytimer.model.DMTimerHistRec;

import java.util.ArrayList;
import java.util.List;

public class HistoryListFragment extends HistoryFragment {
	private final List<DMTimerHistRec> mDMTimerHistList = new ArrayList<>();

    @Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_history_list, container, false);
		mAdapter = new HistoryArrayAdapter(this.getActivity(), mDMTimerHistList, mDMTimers);
		((ListView)rootView.findViewById(R.id.history_list_view)).setAdapter(mAdapter);
        return rootView;
	}	
	
	@Override
	public void setHistoryFilterId(int historyFilterId) {
		super.setHistoryFilterId(historyFilterId);
		DBHelper.getInstance(this.getActivity()).fillHistList(mDMTimerHistList, historyFilterId);
		if (null != mAdapter) {
			mAdapter.notifyDataSetChanged();
		}
	}
}
