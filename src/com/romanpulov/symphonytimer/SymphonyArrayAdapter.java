package com.romanpulov.symphonytimer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.util.StateSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ListView;
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
		
		//calculate progress
		long timerProgress = tasks.getTaskItemProgress(dmTimerRec.id);
		long displayProgress = dmTimerRec.time_sec - timerProgress; 

		
		if (convertView == null) {
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			rowView = inflater.inflate(R.layout.symphony_row_view, parent, false);
			
			
		}
		else 
			rowView = convertView;
		
		rowView.setTag(position);
		
		//ViewTreeObserver vto = rowView.getViewTreeObserver();
		//vto.addOnGlobalLayoutListener(this);		
		
		TextView titleTextView = (TextView)rowView.findViewById(R.id.title_text_view);		
		titleTextView.setText(dmTimerRec.title);

		//display image
		((ImageView)rowView.findViewById(R.id.image_image_view)).setImageURI(
				null != dmTimerRec.image_name ? UriHelper.fileNameToUri(getContext(), dmTimerRec.image_name) : null);
		
		
		//display text
		TextView progressTextView = (TextView)rowView.findViewById(R.id.progress_text_view);			
		progressTextView.setText(String.format("%02d:%02d:%02d", (long) displayProgress / 3600, (long) displayProgress % 3600 / 60, displayProgress % 60));
		
		//display progress bar
		ProgressBar progressBar = (ProgressBar)rowView.findViewById(R.id.progress_bar);
		progressBar.setMax((int)dmTimerRec.time_sec);
		progressBar.setProgress((int)timerProgress);
		
		/*
		final int rPosition = position;
		rowView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				int action = MotionEventCompat.getActionMasked(event);
				
				switch(action) {
		        case (MotionEvent.ACTION_DOWN) :
		            Log.d("SymphonyArrayAdapter","Action was DOWN");
		            return true;
		        case (MotionEvent.ACTION_MOVE) :
		            Log.d("SymphonyArrayAdapter","Action was MOVE");
		            return true;
		        case (MotionEvent.ACTION_UP) :
		            Log.d("SymphonyArrayAdapter","Action was UP");
		        	ListView lv = (ListView)v.getParent();
		        	lv.performItemClick(v, rPosition, rPosition);
		            return true;
		        case (MotionEvent.ACTION_CANCEL) :
		            Log.d("SymphonyArrayAdapter","Action was CANCEL");
		            return true;
		        case (MotionEvent.ACTION_OUTSIDE) :
		            Log.d("SymphonyArrayAdapter","Movement occurred outside bounds " +
		                    "of current screen element");
		            return true;      
		        default : 
		            return false;
			}
			}
		});
		*/
		
		
		
		/*
		rowView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					Log.d("SymphonyArrayAdapter", "ActionDown");
					setSelectedPosition((Integer)rView.getTag());
					rView.setBackgroundResource(R.drawable.main_list_shape_selected);
					return false;
				}
				if (event.getAction() == MotionEvent.ACTION_MOVE) {
					Log.d("SymphonyArrayAdapter", "ActionMove");
					setSelectedPosition(-1);
					//rView.setBackgroundResource(R.drawable.main_list_shape_selected);
				}					
				return true;
			}
		});
		*/
	
		
		//update background
		/*
		rowView.setBackgroundResource(
				0 == displayProgress ? R.drawable.main_list_bg_final_selector : R.drawable.main_list_bg_selector
		);
		*/
		
		final View rView = rowView;
		final long rDisplayProgress = displayProgress;
		ViewTreeObserver vto = rowView.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				// TODO Auto-generated method stub
				
				//Log.d("SymphonyArrayAdapter", "onGlobalLayout");
				
				int rowWidth = rView.getWidth();
				int rowHeight = rView.getHeight();
				if ((rowWidth > 0) && (rowHeight > 0)) {
					
					if (0 == rDisplayProgress ) {
						rView.setBackgroundResource(R.drawable.main_list_bg_final_selector);
					} else {
						
						if (null == bgBitmapDrawable) {
							bgBitmapDrawable = new BgBitmapDrawable(rowWidth, rowHeight);
						}	
						
						StateListDrawable stateDrawable = new StateListDrawable(); 
						stateDrawable.addState(new int[] { android.R.attr.state_pressed }, context.getResources().getDrawable(R.drawable.main_list_shape));
						stateDrawable.addState(StateSet.WILD_CARD, bgBitmapDrawable.getDrawable());
					
						
						rView.setBackground(stateDrawable);
					}												

				};				
				
			}
		});		
		
		return rowView;
	}
}