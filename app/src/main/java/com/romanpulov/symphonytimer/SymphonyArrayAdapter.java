package com.romanpulov.symphonytimer;

import com.romanpulov.symphonytimer.controls.ProgressCircle;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

class SymphonyArrayAdapter extends android.widget.ArrayAdapter<DMTimerRec>{
	
	private final Context mContext;
	private final DMTimers mValues;
	private DMTasks mTasks;
	private RoundedBitmapBackgroundBuilder mBackgroundBuilder;
	
	static class ViewHolder {
		TextView mTitleTextView;
		ImageView mImageView;
		TextView mProgressTextView;
		ProgressCircle mProgressCircle;
		
		public ViewHolder(View view) {
			this.mTitleTextView = (TextView)view.findViewById(R.id.title_text_view);
			this.mImageView = (ImageView)view.findViewById(R.id.image_image_view);
			this.mProgressTextView = (TextView)view.findViewById(R.id.progress_text_view);
			this.mProgressCircle = (ProgressCircle)view.findViewById(R.id.progress_circle);
		}
				
	}
	
	public SymphonyArrayAdapter(Context context, DMTimers values, DMTasks tasks) {
		super(context, R.layout.symphony_row_view);
		this.mContext = context;
		this.mValues = values;
		this.mTasks = tasks;
	}
	
	public void setTasks(DMTasks tasks) {
		this.mTasks = tasks;
	}
	
	@Override
    public int getCount() {
        return mValues.size();
    }
	
	@Override
    public DMTimerRec getItem(int position) {
        return mValues.get(position);
    }		
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		final DMTimerRec dmTimerRec = mValues.get(position);
		
		//calculate progress
		final long timerProgress = mTasks.getTaskItemProgress(dmTimerRec.mId);
		final long displayProgress = dmTimerRec.mTimeSec - timerProgress;
		
		//background drawer
		final boolean isBitmapBackground = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("pref_bitmap_background", false);
		
		View rowView;
		ViewHolder viewHolder;
		
		if (convertView == null) {
			
			//inflate from layout
			LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
			rowView = inflater.inflate(R.layout.symphony_row_view, parent, false);
			
			//setup holder
			viewHolder = new ViewHolder(rowView);
			
			//store holder
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
		
		//handle background settings
		if (isBitmapBackground) {
			
			// create drawable source for background
			if (null == mBackgroundBuilder) {
				rowView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
				int measuredWidth = parent.getMeasuredWidth();
				int measuredHeight =  rowView.getMeasuredHeight();
				if ((measuredWidth > 0) && (measuredHeight > 0)) {
					mBackgroundBuilder = new RoundedBitmapBackgroundBuilder(mContext, measuredWidth, measuredHeight, 6);
				}
			}
		}
				
		viewHolder.mTitleTextView.setText(dmTimerRec.mTitle);

		//display image
		viewHolder.mImageView.setImageURI(
				null != dmTimerRec.mImageName ? UriHelper.fileNameToUri(getContext(), dmTimerRec.mImageName) : null);
		
		//display text
		viewHolder.mProgressTextView.setText(String.format("%02d:%02d:%02d", (long) displayProgress / 3600, (long) displayProgress % 3600 / 60, displayProgress % 60));
		
		//display circle bar
		viewHolder.mProgressCircle.setMax((int)dmTimerRec.mTimeSec);
		viewHolder.mProgressCircle.setProgress((int)timerProgress);
		
		if (isBitmapBackground) {
			//update bitmap background
			if (null != mBackgroundBuilder) {
				Drawable backGround = (0 == displayProgress ) ? mBackgroundBuilder.buildDrawable(RoundedBitmapBackgroundBuilder.BG_FINAL) : mBackgroundBuilder.buildDrawable(RoundedBitmapBackgroundBuilder.BG_NORMAL);
				rowView.setBackground(backGround);
			}
		} else {
			//update solid background
			rowView.setBackgroundResource(
					0 == displayProgress ? R.drawable.main_list_bg_final_selector : R.drawable.main_list_bg_selector
							);
		}

		return rowView;
	}
}