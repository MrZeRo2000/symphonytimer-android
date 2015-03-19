package com.romanpulov.symphonytimer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

public class HistoryTopArrayAdapter extends ArrayAdapter<DMTimerHistTopRec>{
	
	DMTimerHistTopList mDMTimerHistTopList;
	DMTimers mDMTimers;
	
	private class ViewHolder {
		public TextView mTitle;
		public ProgressBar mProgress;
		
		public ViewHolder(View view) {
			mTitle = (TextView)view.findViewById(R.id.history_top_title);
			mProgress = (ProgressBar)view.findViewById(R.id.history_top_progress);			
		}
		
	}
	
	public HistoryTopArrayAdapter(Context context, DMTimerHistTopList dmTimerHistTopList, DMTimers dmTimers) {
		super(context, R.layout.history_row_view);
		mDMTimerHistTopList = dmTimerHistTopList;
		mDMTimers = dmTimers;		
	}
	
	@Override
	public int getCount() {
		return mDMTimerHistTopList.size();
	}
	
	@Override
	public DMTimerHistTopRec getItem(int position) {
		return mDMTimerHistTopList.get(position);
	}	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView;
		
		if (convertView == null) {
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
			rowView = inflater.inflate(R.layout.history_top_row_view, parent, false);			
		}
		else 
			rowView = convertView;
		
		ViewHolder viewHolder = (ViewHolder)rowView.getTag();
		if (null == viewHolder) {
			viewHolder = new ViewHolder(rowView);
			rowView.setTag(viewHolder);
		}
		
		DMTimerHistTopRec rec = mDMTimerHistTopList.get(position);
		DMTimerRec dmTimerRec = mDMTimers.getItemById(rec.timerId);
		
		StringBuilder sb = new StringBuilder();
		sb
		  .append(dmTimerRec.title)
		  .append(" (")
		  .append(String.valueOf(rec.execCnt))
		  .append(")");		
		viewHolder.mTitle.setText(sb.toString());
		viewHolder.mProgress.setProgress((int)(rec.execPerc * viewHolder.mProgress.getMax() / mDMTimerHistTopList.getMaxExecPerc()));

		return rowView;
	}

}
