package com.romanpulov.symphonytimer;

import android.content.Context;
import android.os.Vibrator;

public class VibratorHelper {
	
	private static long[] pattern = {0, 500, 500, 500, 500, 300, 300, 300, 300};
	
	private static VibratorHelper vibratorHelperInstance = null;
	private Vibrator vibrator;
	
	private VibratorHelper(Context context){		
		vibrator =  (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE); 
	}
	
	public static VibratorHelper getInstance(Context context) {
		if (null == vibratorHelperInstance) {
			vibratorHelperInstance = new VibratorHelper(context);
		}		
		return vibratorHelperInstance;
	}
	
	public void vibrate() {
		vibrator.vibrate(pattern, 0);
	}
	
	public void cancel() {
		vibrator.cancel();
	}
	
}