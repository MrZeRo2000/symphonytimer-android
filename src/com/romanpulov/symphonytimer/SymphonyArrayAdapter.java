package com.romanpulov.symphonytimer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageView;

class SymphonyArrayAdapter extends android.widget.ArrayAdapter<DMTimerRec>{
	
	private final Context context;
	private final DMTimers values;
	private DMTasks tasks;
	private BgBitmapDrawable bgBitmapDrawable;
		
	class BgBitmapDrawable {
		
		private int width;
		private int height;
		
		private Drawable drawable;
		
		public BgBitmapDrawable(int width, int height) {
			this.width = width;
			this.height = height;
		}
		
		private void createDrawable() {
			Bitmap bg = BitmapFactory.decodeResource(context.getResources(), R.drawable.sky_home);
			Bitmap sbg = Bitmap.createScaledBitmap(bg, width, height, false);
			drawable = new StreamDrawable(sbg, 6, 0);	

		}
		
		public Drawable getDrawable() {
			if (null == drawable) {
				createDrawable();
			}
			return drawable;
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
		
		View rowView;
		DMTimerRec dmTimerRec = values.get(position);
		
		if (convertView == null) {
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			rowView = inflater.inflate(R.layout.symphony_row_view, parent, false);
		}
		else 
			rowView = convertView;
		
		//ViewTreeObserver vto = rowView.getViewTreeObserver();
		//vto.addOnGlobalLayoutListener(this);		
		
		TextView titleTextView = (TextView)rowView.findViewById(R.id.title_text_view);		
		titleTextView.setText(dmTimerRec.title);

		//display image
		((ImageView)rowView.findViewById(R.id.image_image_view)).setImageURI(
				null != dmTimerRec.image_name ? UriHelper.fileNameToUri(getContext(), dmTimerRec.image_name) : null);
		
		//calculate progress
		long timerProgress = tasks.getTaskItemProgress(dmTimerRec.id);
		long displayProgress = dmTimerRec.time_sec - timerProgress; 
		
		//display text
		TextView progressTextView = (TextView)rowView.findViewById(R.id.progress_text_view);			
		progressTextView.setText(String.format("%02d:%02d:%02d", (long) displayProgress / 3600, (long) displayProgress % 3600 / 60, displayProgress % 60));
		
		//display progress bar
		ProgressBar progressBar = (ProgressBar)rowView.findViewById(R.id.progress_bar);
		progressBar.setMax((int)dmTimerRec.time_sec);
		progressBar.setProgress((int)timerProgress);
		
		
		final View rView = rowView;
		ViewTreeObserver vto = rowView.getViewTreeObserver();		
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				// TODO Auto-generated method stub
				
				int rowWidth = rView.getWidth();
				int rowHeight = rView.getHeight();
				if ((rowWidth > 0) && (rowHeight > 0)) {
					if (null == bgBitmapDrawable) {
						bgBitmapDrawable = new BgBitmapDrawable(rowWidth, rowHeight);
					}	
					rView.setBackground(bgBitmapDrawable.getDrawable());								
				};				
				
			}
		});
		
		//update background
		/*
		rowView.setBackgroundResource(
				0 == displayProgress ? R.drawable.main_list_bg_final_selector : R.drawable.main_list_bg_selector
		);
		*/
		
		return rowView;
	}
}