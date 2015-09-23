package com.romanpulov.symphonytimer.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.activity.HistoryActivity;
import com.romanpulov.symphonytimer.adapter.HistoryTopArrayAdapter;
import com.romanpulov.symphonytimer.fragment.HistoryFragment;
import com.romanpulov.symphonytimer.helper.db.DBHelper;
import com.romanpulov.symphonytimer.model.DMTimerHistTopList;
import com.romanpulov.symphonytimer.model.DMTimers;

public class HistoryTopFragment extends HistoryFragment {
	private final static String tag = "HistoryTopFragment"; 
	
	private DMTimerHistTopList mDMTimerHistTopList = new DMTimerHistTopList();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootView = inflater.inflate(R.layout.history_top_frag, container, false);
		
		//DBHelper.getInstance(this.getActivity()).fillHistTopList(mDMTimerHistTopList);
		//mDMTimerHistTopList.calcMaxPerc();
		
		DMTimers dmTimers = ((HistoryActivity)this.getActivity()).getTimers();
	
		mAdapter = new HistoryTopArrayAdapter(this.getActivity(), mDMTimerHistTopList, dmTimers);
		
		((ListView)rootView.findViewById(R.id.history_top_view)).setAdapter(mAdapter);
		
        return rootView;

	}
	
	@Override
	public void setHistoryFilterId(int historyFilterId) {
		Log.d(tag, "setHistoryFilterId=" + String.valueOf(historyFilterId));
		
		super.setHistoryFilterId(historyFilterId);
		DBHelper.getInstance(this.getActivity()).fillHistTopList(mDMTimerHistTopList, historyFilterId);
		mDMTimerHistTopList.calcPercent();
		
		if (null != mAdapter) {
			Log.d(tag, "adapter notifyDataSetChanged");
			mAdapter.notifyDataSetChanged();
		}
	}


}
