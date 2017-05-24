package com.romanpulov.symphonytimer.adapter;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.helper.DateFormatterHelper;
import com.romanpulov.symphonytimer.model.DMTimerHistRec;
import com.romanpulov.symphonytimer.model.DMTimerRec;
import com.romanpulov.symphonytimer.model.DMTimers;


public class HistoryArrayAdapter extends ArrayAdapter<DMTimerHistRec> {
	private final DMTimers mDMTimers;
	private final List<DMTimerHistRec> mDMTimerHistList;
	
	private class ViewHolder {
		public final TextView mTitle;
		public final ImageView mImage;
		public final TextView mTime;
		
		public ViewHolder(View view) {
			mTitle = (TextView)view.findViewById(R.id.history_text_view);
			mImage = (ImageView)view.findViewById(R.id.history_image_view);
			mTime = (TextView)view.findViewById(R.id.history_time_view);
		}
	}

	public HistoryArrayAdapter(Context context, List<DMTimerHistRec> dmTimerHistList, DMTimers dmTimers) {
		super(context, R.layout.history_row_view);
		mDMTimerHistList = dmTimerHistList;
		mDMTimers = dmTimers;		
	}
	
	@Override
	public int getCount() {
		return mDMTimerHistList.size();
	}
	
	@Override
	public DMTimerHistRec getItem(int position) {
		return mDMTimerHistList.get(position);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView;
		ViewHolder viewHolder;
	
		if (convertView == null) {
			//inflate view
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
			rowView = inflater.inflate(R.layout.history_row_view, parent, false);
			//setup viewholder
			viewHolder = new ViewHolder(rowView);
			rowView.setTag(viewHolder);			
		}
		else { 
			rowView = convertView;
			viewHolder = (ViewHolder)rowView.getTag();
		}
		
		// create viewHolder(just in case)
		if (null == viewHolder) {
			viewHolder = new ViewHolder(rowView);
			rowView.setTag(viewHolder);
		}
		
		DMTimerHistRec rec = mDMTimerHistList.get(position);
		DMTimerRec dmTimerRec = mDMTimers.getItemById(rec.mTimerId);
		
		viewHolder.mTitle.setText(dmTimerRec.mTitle);		
		viewHolder.mTime.setText(DateFormatterHelper.format(rec.mStartTime));
		viewHolder.mImage.setImageURI(
				null != dmTimerRec.mImageName ? Uri.parse(dmTimerRec.mImageName) : null);
		
		return rowView;
	}
}
