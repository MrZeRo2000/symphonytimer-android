package com.romanpulov.symphonytimer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.StateSet;

import com.romanpulov.symphonytimer.R;

public class RoundedBitmapBackgroundBuilder {

	final public static int BG_NORMAL = 0; 
	final public static int BG_FINAL = 1;
	final private static int BRIGHTENING_FACTOR = 100;
	
	private Context mContext;
	private int mWidth;
	private int mHeight;
	private float mCornerRadius;
	
	private Boolean mIsBitmapPrepared = false;
	
	private Bitmap mScaledBg;
	private Bitmap mScaledBrightBg;
	
	private Bitmap mFinalScaledBg;
	private Bitmap mFinalScaledBrightBg;
	
	public RoundedBitmapBackgroundBuilder(Context context, int width, int height, float cornerRadius) {
		this.mContext = context;
		this.mWidth = width;
		this.mHeight = height;
		this.mCornerRadius = cornerRadius;
	}
	
	private void prepareBitmaps() {
		
		final Bitmap bg = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.sky_home_sm);
		final Bitmap brightBg = createBrightBitmap(bg, BRIGHTENING_FACTOR);
		mScaledBg = Bitmap.createScaledBitmap(bg, mWidth, mHeight, false);
		mScaledBrightBg = Bitmap.createScaledBitmap(brightBg, mWidth, mHeight, false);
		
		final Bitmap finalBg = createBlueToRedBitmap(bg);
		final Bitmap finalBrightBg = createBrightBitmap(finalBg, BRIGHTENING_FACTOR);
		mFinalScaledBg = Bitmap.createScaledBitmap(finalBg, mWidth, mHeight, false);
		mFinalScaledBrightBg = Bitmap.createScaledBitmap(finalBrightBg, mWidth, mHeight, false);			
		
		mIsBitmapPrepared = true;

	}
	
	public Drawable buildDrawable(int type) {
		
		if (!mIsBitmapPrepared) {
			prepareBitmaps();
		}
		
		final Drawable bgDrawable = new StreamDrawable(mScaledBg, mCornerRadius, 0);
		final Drawable bgBrightDrawable = new StreamDrawable(mScaledBrightBg, mCornerRadius, 0);
		final Drawable bgFinalDrawable = new StreamDrawable(mFinalScaledBg, mCornerRadius, 0);
		final Drawable bgFinalBrightDrawable = new StreamDrawable(mFinalScaledBrightBg, mCornerRadius, 0);
		
		final StateListDrawable drawable = new StateListDrawable(); 
		drawable.addState(new int[] { android.R.attr.state_pressed }, (type == 0) ? bgBrightDrawable : bgFinalBrightDrawable);
		drawable.addState(StateSet.WILD_CARD, (type == 0) ? bgDrawable : bgFinalDrawable);
		
		return drawable;
	}
	
	private Bitmap createBrightBitmap(Bitmap src, int value) {
		
		final int width = src.getWidth();
		final int height = src.getHeight();
		
		final int[] pixels = new int[height * width];
		src.getPixels(pixels, 0, width, 0, 0, width, height);
		
		for (int i = 0; i < height * width; i++) {
			pixels[i] = Color.rgb(
					(Color.red(pixels[i]) + value > 255 ? 255 : Color.red(pixels[i]) + value), 
					(Color.green(pixels[i]) + value > 255 ? 255 : Color.green(pixels[i]) + value),
					(Color.blue(pixels[i]) + value > 255 ? 255 : Color.blue(pixels[i]) + value)
			);
		}
		
		final Bitmap res = Bitmap.createBitmap(width, height, src.getConfig());
		res.setPixels(pixels, 0, width, 0, 0, width, height);
		
		return res;
	}
	
	private Bitmap createBlueToRedBitmap(Bitmap src) {
		
		final int width = src.getWidth();
		final int height = src.getHeight();
		
		final int[] pixels = new int[height * width];
		src.getPixels(pixels, 0, width, 0, 0, width, height);
		
		for (int i = 0; i < height * width; i++) {
			pixels[i] = Color.rgb(
					Color.blue(pixels[i]), 
					Color.green(pixels[i]),
					Color.red(pixels[i])
			);
		}
		
		final Bitmap res = Bitmap.createBitmap(width, height, src.getConfig());
		res.setPixels(pixels, 0, width, 0, 0, width, height);
		
		return res;
	}
	
}
