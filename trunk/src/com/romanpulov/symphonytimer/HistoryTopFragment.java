package com.romanpulov.symphonytimer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class HistoryTopFragment extends Fragment{
	
	private DMTimerHistTopList mDMTimerHistTopList = new DMTimerHistTopList();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootView = inflater.inflate(R.layout.history_top_frag, container, false);
		
		DBHelper.getInstance(this.getActivity()).fillHistTopList(mDMTimerHistTopList);
		mDMTimerHistTopList.calcMaxPerc();
		
		DMTimers dmTimers = ((HistoryActivity)this.getActivity()).getTimers();
	
		ArrayAdapter<?> adapter = new HistoryTopArrayAdapter(this.getActivity(), mDMTimerHistTopList, dmTimers);
		
		((ListView)rootView.findViewById(R.id.history_top_view)).setAdapter(adapter);
		
        return rootView;

	}

}
