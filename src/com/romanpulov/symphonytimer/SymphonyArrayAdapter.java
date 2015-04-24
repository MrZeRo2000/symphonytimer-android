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
import android.view.View.MeasureSpec;
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
		
		final static int BG_NORMAL = 0; 
		final static int BG_FINAL = 1;
		
		private int width;
		private int height;
		
		private Drawable bgDrawable;
		private Drawable bgBrightDrawable;
		private Drawable bgFinalDrawable;
		private Drawable bgFinalBrightDrawable;
		
		private Bitmap finalBg;
		private Bitmap finalBrightBg;
		private Bitmap finalScaledBg;
		private Bitmap finalScaledBrightBg;
		
		
		//private StateListDrawable drawable;
		
		public BgBitmapDrawable(int width, int height) {
			Log.d("SymphonyArrayAdapter", "creating BgBitmapDrawable");
			this.width = width;
			this.height = height;
		}
		
		private void prepareDrawable() {
			
			Bitmap bg = BitmapFactory.decodeResource(context.getResources(), R.drawable.sky_home_sm);
			Bitmap brightBg = createBrightBitmap(bg, 100);
			Bitmap scaledBg = Bitmap.createScaledBitmap(bg, width, height, false);
			Bitmap scaledBrightBg = Bitmap.createScaledBitmap(brightBg, width, height, false);
			
			finalBg = createBlueToRedBitmap(bg);
			finalBrightBg = createBrightBitmap(finalBg, 100);
			finalScaledBg = Bitmap.createScaledBitmap(finalBg, width, height, false);
			finalScaledBrightBg = Bitmap.createScaledBitmap(finalBrightBg, width, height, false);			
			
			bgDrawable = new StreamDrawable(scaledBg, 6, 0);
			bgBrightDrawable = new StreamDrawable(scaledBrightBg, 6, 0);
			bgFinalDrawable = new StreamDrawable(finalScaledBg, 6, 0);
			bgFinalBrightDrawable = new StreamDrawable(finalScaledBrightBg, 6, 0);
	
		}
		
		
		public Drawable getDrawable(int type) {
			
			if ((null == bgDrawable) || (null == bgBrightDrawable)) {
				prepareDrawable();
			}
			
			StateListDrawable drawable = new StateListDrawable(); 
			drawable.addState(new int[] { android.R.attr.state_pressed }, (type == 0) ? bgBrightDrawable : bgFinalBrightDrawable);
			drawable.addState(StateSet.WILD_CARD, (type == 0) ? bgDrawable : bgFinalDrawable);
			
			return drawable;
		}
		
		private Bitmap createBrightBitmap(Bitmap src, int value) {
			
			int width = src.getWidth();
			int height = src.getHeight();
			
			int[] pixels = new int[height * width];
			src.getPixels(pixels, 0, width, 0, 0, width, height);
			
			for (int i = 0; i < height * width; i++) {
				pixels[i] = Color.rgb(
						(Color.red(pixels[i]) + value > 255 ? 255 : Color.red(pixels[i]) + value), 
						(Color.green(pixels[i]) + value > 255 ? 255 : Color.green(pixels[i]) + value),
						(Color.blue(pixels[i]) + value > 255 ? 255 : Color.blue(pixels[i]) + value)
				);
			}
			
			Bitmap res = Bitmap.createBitmap(width, height, src.getConfig());
			res.setPixels(pixels, 0, width, 0, 0, width, height);
			
			return res;
		}
		
		private Bitmap createBlueToRedBitmap(Bitmap src) {
			
			int width = src.getWidth();
			int height = src.getHeight();
			
			int[] pixels = new int[height * width];
			src.getPixels(pixels, 0, width, 0, 0, width, height);
			
			for (int i = 0; i < height * width; i++) {
				pixels[i] = Color.rgb(
						Color.blue(pixels[i]), 
						Color.green(pixels[i]),
						Color.red(pixels[i])
				);
			}
			
			Bitmap res = Bitmap.createBitmap(width, height, src.getConfig());
			res.setPixels(pixels, 0, width, 0, 0, width, height);
			
			return res;
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
		final DMTimerRec dmTimerRec = values.get(position);
		
		//calculate progress
		final long timerProgress = tasks.getTaskItemProgress(dmTimerRec.id);
		final long displayProgress = dmTimerRec.time_sec - timerProgress; 
		
		
		if (null == bgBitmapDrawable) {
			bgBitmapDrawable = new BgBitmapDrawable(1000, 500);
		}					
		
		
		if (convertView == null) {
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			rowView = inflater.inflate(R.layout.symphony_row_view, parent, false);
/*
			if (null == bgBitmapDrawable) {
				rowView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
				bgBitmapDrawable = new BgBitmapDrawable(parent.getMeasuredWidth(), rowView.getMeasuredHeight());
			}
						
			
			if (null == bgBitmapDrawable) {
				
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
							
							
							if (0 == displayProgress ) {
								rView.setBackground(bgBitmapDrawable.getDrawable(BgBitmapDrawable.BG_FINAL));
								//rView.setBackgroundResource(R.drawable.main_list_bg_final_selector);
							} else {
								
								rView.setBackground(bgBitmapDrawable.getDrawable(BgBitmapDrawable.BG_NORMAL));
							}
							
	
						};				
						
					}
					
				});
					
			
			}
			*/
		}
		else { 
			rowView = convertView;
		}
		
		
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
		
		Drawable backGround = (0 == displayProgress ) ? bgBitmapDrawable.getDrawable(BgBitmapDrawable.BG_FINAL) : bgBitmapDrawable.getDrawable(BgBitmapDrawable.BG_NORMAL);
		//Drawable backGround = context.getResources().getDrawable(R.drawable.sky_home_sm);
		rowView.setBackground(backGround);
		
		/*
		if (null != bgBitmapDrawable) {
			if (0 == displayProgress ) {
				rowView.setBackground(bgBitmapDrawable.getDrawable(BgBitmapDrawable.BG_FINAL));
			} else {				
				rowView.setBackground(bgBitmapDrawable.getDrawable(BgBitmapDrawable.BG_NORMAL));
			}
		}
		*/
		

		return rowView;
	}
}