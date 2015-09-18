package com.romanpulov.symphonytimer;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class HistoryListFragment extends HistoryFragment {
	
	private final static String tag = "HistoryListFragment"; 
	
	private DMTimerHistList mDMimerHistList = new DMTimerHistList();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootView = inflater.inflate(R.layout.history_list_frag, container, false);
		
		//DBHelper.getInstance(this.getActivity()).fillHistList(mDMimerHistList);		
		
		DMTimers dmTimers = ((HistoryActivity)this.getActivity()).getTimers();
	
		mAdapter = new HistoryArrayAdapter(this.getActivity(), mDMimerHistList, dmTimers);
		
		((ListView)rootView.findViewById(R.id.history_list_view)).setAdapter(mAdapter);
		Log.d(tag, "setting adapter");
		
        return rootView;
	}	
	
	@Override
	public void setHistoryFilterId(int historyFilterId) {
		Log.d(tag, "setHistoryFilterId=" + String.valueOf(historyFilterId));
		
		super.setHistoryFilterId(historyFilterId);
		DBHelper.getInstance(this.getActivity()).fillHistList(mDMimerHistList, historyFilterId);
		
		if (null != mAdapter) {
			Log.d(tag, "adapter notifyDataSetChanged");
			mAdapter.notifyDataSetChanged();
		}
	}
}
