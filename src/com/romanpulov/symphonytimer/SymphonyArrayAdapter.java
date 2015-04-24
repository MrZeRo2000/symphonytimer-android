package com.romanpulov.symphonytimer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.Log;
import android.util.StateSet;
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
		
	class RoundedBitmapBackgroundBuilder {
		
		final public static int BG_NORMAL = 0; 
		final public static int BG_FINAL = 1;
		final private static int BRIGHTENING_FACTOR = 100;
		
		private int width;
		private int height;
		private float cornerRadius;
		
		private Boolean isBitmapPrepared = false;
		
		private Bitmap scaledBg;
		private Bitmap scaledBrightBg;
		
		private Bitmap finalScaledBg;
		private Bitmap finalScaledBrightBg;
		
		
		//private StateListDrawable drawable;
		
		public RoundedBitmapBackgroundBuilder(int width, int height, float cornerRadius) {
			Log.d("SymphonyArrayAdapter", "creating BgBitmapDrawable");
			this.width = width;
			this.height = height;
			this.cornerRadius = cornerRadius;
		}
		
		private void prepareBitmaps() {
			
			Bitmap bg = BitmapFactory.decodeResource(context.getResources(), R.drawable.sky_home_sm);
			Bitmap brightBg = createBrightBitmap(bg, BRIGHTENING_FACTOR);
			scaledBg = Bitmap.createScaledBitmap(bg, width, height, false);
			scaledBrightBg = Bitmap.createScaledBitmap(brightBg, width, height, false);
			
			Bitmap finalBg = createBlueToRedBitmap(bg);
			Bitmap finalBrightBg = createBrightBitmap(finalBg, BRIGHTENING_FACTOR);
			finalScaledBg = Bitmap.createScaledBitmap(finalBg, width, height, false);
			finalScaledBrightBg = Bitmap.createScaledBitmap(finalBrightBg, width, height, false);			
			
			isBitmapPrepared = true;
	
		}
		
		
		public Drawable buildDrawable(int type) {
			
			if (!isBitmapPrepared) {
				prepareBitmaps();
			}
			
			Drawable bgDrawable = new StreamDrawable(scaledBg, cornerRadius, 0);
			Drawable bgBrightDrawable = new StreamDrawable(scaledBrightBg, cornerRadius, 0);
			Drawable bgFinalDrawable = new StreamDrawable(finalScaledBg, cornerRadius, 0);
			Drawable bgFinalBrightDrawable = new StreamDrawable(finalScaledBrightBg, cornerRadius, 0);
			
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
		
		final DMTimerRec dmTimerRec = values.get(position);
		
		//calculate progress
		final long timerProgress = tasks.getTaskItemProgress(dmTimerRec.id);
		final long displayProgress = dmTimerRec.time_sec - timerProgress; 
		
		View rowView;
		
		if (convertView == null) {
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			rowView = inflater.inflate(R.layout.symphony_row_view, parent, false);

			// create drawable source for background
			if (null == backgroundBuilder) {
				rowView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
				int measuredWidth = parent.getMeasuredWidth();
				int measuredHeight =  rowView.getMeasuredHeight();
				if ((measuredWidth > 0) && (measuredHeight > 0)) {
					backgroundBuilder = new RoundedBitmapBackgroundBuilder(measuredWidth, measuredHeight, 6);
				}
			}
						
		}
		else { 
			rowView = convertView;
		}
		
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
		
		//update background old style
		/*
		rowView.setBackgroundResource(
				0 == displayProgress ? R.drawable.main_list_bg_final_selector : R.drawable.main_list_bg_selector
		);
		*/
		
		//update background
		if (null != backgroundBuilder) {
			Drawable backGround = (0 == displayProgress ) ? backgroundBuilder.buildDrawable(RoundedBitmapBackgroundBuilder.BG_FINAL) : backgroundBuilder.buildDrawable(RoundedBitmapBackgroundBuilder.BG_NORMAL);
			rowView.setBackground(backGround);
		}

		return rowView;
	}
}