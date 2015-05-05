package com.romanpulov.symphonytimer.controls;

import java.util.Locale;

import com.romanpulov.symphonytimer.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class ProgressCircle extends View {
	
	private static final int ARC_MARGIN = 5;
	private static final int ARC_THICKNESS = 5;
	
	private int mMin;
	private int mMax;
	private int mProgress;
	private boolean mAutoHide;
	
	private String mDisplayProgress;
	
	private int mPrevProgress;
	private Paint mTextPaint;
	private Paint mArcPaint;
	private Rect mTextBounds;
	private RectF mArcRect;
	
	private int mMostSize = 0;
	
	// constructor from code
	public ProgressCircle(Context context) {
		super(context);
		initProgressCircle();
	}

	// constructor from xml
	public ProgressCircle(Context context, AttributeSet attrs) {
		super(context, attrs);
		initProgressCircle();
		
		//read resources
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressCircle);
        
        int min =  a.getInteger(R.styleable.ProgressCircle_min, mMin);        
        int max = a.getInteger(R.styleable.ProgressCircle_max, mMax);
        int progress = a.getInteger(R.styleable.ProgressCircle_progress, mProgress);
        boolean autohide = a.getBoolean(R.styleable.ProgressCircle_autohide, mAutoHide);
        
        if ((max >= min) && (progress >= min) && (progress <= max)) {
        	mMin = min;
        	mMax = max;
        	mProgress = progress;
        	mAutoHide = autohide;
        	mPrevProgress = mProgress;
        }

        a.recycle();
	
	}
	
	private String getDisplayProgress() {
		
		if (mMax == mProgress) {
			return "100";
		} else if (mMin == mProgress) {
			return "";
		} else	{
			return String.format("%02d%%", mProgress * 100 / (mMax - mMin));
		} 
		
	}
	
	private void initProgressCircle() {
		
		//defaults
		mMin = 0;
		mMax = 100;
		mProgress = 50;
		mPrevProgress = mProgress;
		mAutoHide = false;
		mDisplayProgress = getDisplayProgress();
		
		//paint
		mTextPaint = new Paint();
		mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(12 * getResources().getDisplayMetrics().density);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setStyle(Paint.Style.STROKE);
        mTextPaint.setTextAlign(Align.LEFT);
        mTextBounds = new Rect();
        mTextPaint.getTextBounds("000", 0, 3, mTextBounds);
        
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStrokeWidth(ARC_THICKNESS);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setColor(Color.RED);
        
        mArcRect = new RectF();
	}
	
	public void setMin(int min) {
		if (min <= mMax) {
			mMin = min;
			if (mProgress < mMin) {
				setProgress(mMin);
			} else { 
				invalidate();
			}
		}
	}
	
	public void setMax(int max) {
		if (max >= mMin) {		
			mMax = max;
			if (mProgress > mMax) {
				setProgress(mMax);
			} else {
				invalidate();
			}
			invalidate();
		}
	}
	
	public void setProgress(int progress) {
		
		// change progress
		if (progress < mMin) {
			mProgress = mMin;
		} else if (progress > mMax) {
			mProgress = mMax;
		} else
			mProgress = progress;
		mDisplayProgress = getDisplayProgress();
		
		//redraw control
		invalidate();
		
		//autohide support
		if (mAutoHide) {
			if (((mPrevProgress == mMin) && (mProgress != mMin)) || ((mPrevProgress != mMin) && (mProgress == mMin))) {
				requestLayout();
			}
			mPrevProgress = mProgress;
		}
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
		int width = getWidth();
		int height = getHeight();
		
		canvas.drawText(mDisplayProgress, (width - mTextBounds.width()) / 2, height - (height - mTextBounds.height()) / 2, mTextPaint);

		//draw border for testing purposes
		//canvas.drawRect(0, 0, width, height, mTextPaint);

		if (width > height) {
			mArcRect.set((width - height) / 2 + ARC_MARGIN , ARC_MARGIN, (width - height) / 2 + height - ARC_MARGIN, height - ARC_MARGIN);
		} else {
			mArcRect.set(ARC_MARGIN, (height - width ) / 2 + ARC_MARGIN , width - ARC_MARGIN, (height - width) / 2 + width - ARC_MARGIN);
		}
		
		if (mMax > mMin) {
			canvas.drawArc(mArcRect, 90, (mProgress - mMin) * 360 / (mMax - mMin), false, mArcPaint);
		}
			 
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);		
		//setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
		
        final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

		int size = 0;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heigthWithoutPadding = height - getPaddingTop() - getPaddingBottom();
        
        /*
        if (widthWithoutPadding > heigthWithoutPadding) {
            size = heigthWithoutPadding;
        } else {
            size = widthWithoutPadding;
        }
        */
        
        if ((MeasureSpec.EXACTLY ==  heightSpecMode) || (MeasureSpec.EXACTLY == widthSpecMode)) {
        	if ((0 == widthWithoutPadding) || (0 == heigthWithoutPadding)) {
        		size = Math.max(widthWithoutPadding, heigthWithoutPadding);
        	} else {
        		size = Math.max(widthWithoutPadding, heigthWithoutPadding);
        	}
        	
        	if (size > 0 ) {
        		mMostSize = size;
        	}
        	
        } else {
        	size = 0;
        }
        
        if (widthSpecMode == MeasureSpec.AT_MOST) {
        	size = mMostSize;
        }
        
        //autohide support
        if (mAutoHide) {
	        if (mProgress == mMin) {
	        	size = 0;
	        }
        }
        
        //Log.d("ProgressCircle", "widthMeasureSpec=" + widthMeasureSpec + ", heightMeasureSpec=" + heightMeasureSpec + ", width=" + widthWithoutPadding + ", height=" + heigthWithoutPadding + ", size=" + size);
        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());		
	}

}
