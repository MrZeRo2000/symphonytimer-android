package com.romanpulov.symphonytimer;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageView;

class SymphonyArrayAdapter extends android.widget.ArrayAdapter<DMTimerRec>{
	
	private final Context context;
	private final DMTimers values;
	private DMTasks tasks;
	private RoundedBitmapBackgroundBuilder backgroundBuilder;
	
	static class ViewHolder {
		TextView titleTextView;
		ImageView imageView;
		TextView progressTextView;
		ProgressBar progressBar;
		
		public ViewHolder(View view) {
			this.titleTextView = (TextView)view.findViewById(R.id.title_text_view);
			this.imageView = (ImageView)view.findViewById(R.id.image_image_view);
			this.progressTextView = (TextView)view.findViewById(R.id.progress_text_view);
			this.progressBar = (ProgressBar)view.findViewById(R.id.progress_bar);			
		}
				
	}
	
	public SymphonyArrayAdapter(Context context, DMTimers values, DMTasks tasks) {
		super(context, R.layout.symphony_row_view);
		this.context = context;
		this.values = values;
		this.tasks = tasks;
	}
	
	public void setTasks(DMTasks tasks) {
		this.tasks = tasks;
	}
	
	@Override
    public int getCount() {
        return values.size();
    }
	
	@Override
    public DMTimerRec getItem(int position) {
        return values.get(position);
    }		
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		final DMTimerRec dmTimerRec = values.get(position);
		
		//calculate progress
		final long timerProgress = tasks.getTaskItemProgress(dmTimerRec.id);
		final long displayProgress = dmTimerRec.time_sec - timerProgress;
		
		//background drawer
		final boolean isBitmapBackground = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_bitmap_background", false);
		
		View rowView;
		ViewHolder viewHolder;
		
		if (convertView == null) {
			
			//inflate from layout
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
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
			if (null == backgroundBuilder) {
				rowView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
				int measuredWidth = parent.getMeasuredWidth();
				int measuredHeight =  rowView.getMeasuredHeight();
				if ((measuredWidth > 0) && (measuredHeight > 0)) {
					backgroundBuilder = new RoundedBitmapBackgroundBuilder(context, measuredWidth, measuredHeight, 6);
				}
			}
		}
				
		viewHolder.titleTextView.setText(dmTimerRec.title);

		//display image
		viewHolder.imageView.setImageURI(
				null != dmTimerRec.image_name ? UriHelper.fileNameToUri(getContext(), dmTimerRec.image_name) : null);
		
		//display text
		viewHolder.progressTextView.setText(String.format("%02d:%02d:%02d", (long) displayProgress / 3600, (long) displayProgress % 3600 / 60, displayProgress % 60));
		
		//display progress bar
		viewHolder.progressBar.setMax((int)dmTimerRec.time_sec);
		viewHolder.progressBar.setProgress((int)timerProgress);
		
		if (isBitmapBackground) {
			//update bitmap background
			if (null != backgroundBuilder) {
				Drawable backGround = (0 == displayProgress ) ? backgroundBuilder.buildDrawable(RoundedBitmapBackgroundBuilder.BG_FINAL) : backgroundBuilder.buildDrawable(RoundedBitmapBackgroundBuilder.BG_NORMAL);
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