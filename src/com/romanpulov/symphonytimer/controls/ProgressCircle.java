package com.romanpulov.symphonytimer.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ProgressCircle extends View {
	
	private Paint mTextPaint;
	
	// constructor from code
	public ProgressCircle(Context context) {
		super(context);
		initProgressCircle();
	}

	// constructor from xml
	public ProgressCircle(Context context, AttributeSet attrs) {
		super(context, attrs);
		initProgressCircle();
		// TODO Auto-generated constructor stub
	}
	
	private void initProgressCircle() {
		mTextPaint = new Paint();
		mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(16 * getResources().getDisplayMetrics().density);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setStyle(Paint.Style.STROKE);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		canvas.drawText("00", (getWidth() - (int) mTextPaint.measureText("00")) / 2 , getHeight() / 2, mTextPaint);		
		canvas.drawRect(0, 0, getWidth(), getHeight(), mTextPaint);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
	}

}
