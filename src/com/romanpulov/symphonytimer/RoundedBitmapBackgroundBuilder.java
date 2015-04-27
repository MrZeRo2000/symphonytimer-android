package com.romanpulov.symphonytimer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.StateSet;

public class RoundedBitmapBackgroundBuilder {

	final public static int BG_NORMAL = 0; 
	final public static int BG_FINAL = 1;
	final private static int BRIGHTENING_FACTOR = 100;
	
	private Context context;
	private int width;
	private int height;
	private float cornerRadius;
	
	private Boolean isBitmapPrepared = false;
	
	private Bitmap scaledBg;
	private Bitmap scaledBrightBg;
	
	private Bitmap finalScaledBg;
	private Bitmap finalScaledBrightBg;
	
	public RoundedBitmapBackgroundBuilder(Context context, int width, int height, float cornerRadius) {
		this.context = context;
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
