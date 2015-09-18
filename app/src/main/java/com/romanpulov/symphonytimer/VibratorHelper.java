package com.romanpulov.symphonytimer;

import android.content.Context;
import android.os.Vibrator;

public class VibratorHelper {
	
	private static long[] mPattern = {0, 500, 500, 500, 500, 300, 300, 300, 300};
	
	private static VibratorHelper mVibratorHelperInstance = null;
	private Vibrator mVibrator;
	
	private VibratorHelper(Context context){		
		mVibrator =  (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE); 
	}
	
	public static VibratorHelper getInstance(Context context) {
		if (null == mVibratorHelperInstance) {
			mVibratorHelperInstance = new VibratorHelper(context);
		}		
		return mVibratorHelperInstance;
	}
	
	public void vibrate() {
		mVibrator.vibrate(mPattern, 0);
	}
	
	public void cancel() {
		mVibrator.cancel();
	}
	
}