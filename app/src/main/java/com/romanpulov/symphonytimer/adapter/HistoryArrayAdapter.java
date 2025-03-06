package com.romanpulov.symphonytimer.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.preference.PreferenceManager;
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
		public final TextView mTimeDetails;
		
		public ViewHolder(View view) {
			mTitle = view.findViewById(R.id.history_text_view);
			mImage = view.findViewById(R.id.history_image_view);
			mTime = view.findViewById(R.id.history_time_view);
			mTimeDetails = view.findViewById(R.id.history_time_details_view);
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
		
		viewHolder.mTitle.setText(dmTimerRec.getTitle());
		viewHolder.mTime.setText(DateFormatterHelper.format(rec.mStartTime));
		viewHolder.mImage.setImageURI(
				null != dmTimerRec.getImageName() ? Uri.parse(dmTimerRec.getImageName()) : null);

        boolean hideTimeDetails = !PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("pref_full_history_info", false);

        if ((rec.mRealTime == 0) || hideTimeDetails) {
            viewHolder.mTimeDetails.setVisibility(View.GONE);
        }
        else {
            viewHolder.mTimeDetails.setVisibility(View.VISIBLE);
            String detailsText = getContext().getString(R.string.caption_due_real_time, DateFormatterHelper.formatTime(rec.mEndTime), DateFormatterHelper.formatTime(rec.mRealTime));
            //viewHolder.mTimeDetails.setText("Due time : " + DateFormatterHelper.formatTime(rec.mEndTime) + ", real time : " + DateFormatterHelper.formatTime(rec.mRealTime));
            viewHolder.mTimeDetails.setText(detailsText);
        }
		
		return rowView;
	}
}
