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
import android.view.View;

public class ProgressCircle extends View {
	
	private static final int ARC_MARGIN = 5;
	private static final int ARC_THICKNESS = 5;
	
	private int mMin;
	private int mMax;
	private int mProgress;
	private boolean mAutoHide;
	private int mProgressColor;
	private int mProgressRestColor;
	
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
        
        setTextColor(a.getColor(R.styleable.ProgressCircle_textColor, Color.WHITE));
        
        mProgressColor = a.getColor(R.styleable.ProgressCircle_progressColor, mProgressColor);
        mProgressRestColor = a.getColor(R.styleable.ProgressCircle_progressRestColor, mProgressRestColor);

        int textSize = a.getDimensionPixelOffset(R.styleable.ProgressCircle_textSize, 0);
        if (textSize > 0) {
            setTextSize(textSize);
        }
        
        a.recycle();
	
	}
	
	private String getDisplayProgress() {
		
		if (mMax == mProgress) {
			return "100";
		} else if (mMin == mProgress) {
			return "";
		} else	{
			return String.format(Locale.getDefault(), "%02d%%", mProgress * 100 / (mMax - mMin));
		} 
		
	}
	
	private void initProgressCircle() {
		
		//defaults
		mMin = 0;
		mMax = 100;
		mProgress = 50;
		mPrevProgress = mProgress;
		mAutoHide = false;
		mProgressColor = Color.RED;
		mProgressRestColor = Color.WHITE;
		mDisplayProgress = getDisplayProgress();
		
		//text paint
		mTextPaint = new Paint();
		mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(12 * getResources().getDisplayMetrics().density);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setStyle(Paint.Style.STROKE);
        mTextPaint.setTextAlign(Align.LEFT);
        mTextBounds = new Rect();
        mTextPaint.getTextBounds("000", 0, 3, mTextBounds);
        
        //arc paint
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStrokeWidth(ARC_THICKNESS);
        mArcPaint.setStyle(Paint.Style.STROKE);
        
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
	
    public void setTextSize(int size) {
        // This text size has been pre-scaled by the getDimensionPixelOffset method
        mTextPaint.setTextSize(size);
        //would be required if control size gets dependent from test font 
        //requestLayout();
        invalidate();
    }	
    
    public void setTextColor(int color) {
        mTextPaint.setColor(color);
        invalidate();
    }    
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
		final int width = getWidth();
		final int height = getHeight();
		
		canvas.drawText(mDisplayProgress, (width - mTextBounds.width()) / 2, height - (height - mTextBounds.height()) / 2, mTextPaint);

		//draw border for testing purposes
		//canvas.drawRect(0, 0, width, height, mTextPaint);

		if (width > height) {
			mArcRect.set((width - height) / 2 + ARC_MARGIN , ARC_MARGIN, (width - height) / 2 + height - ARC_MARGIN, height - ARC_MARGIN);
		} else {
			mArcRect.set(ARC_MARGIN, (height - width ) / 2 + ARC_MARGIN , width - ARC_MARGIN, (height - width) / 2 + width - ARC_MARGIN);
		}
		
		if (mMax > mMin) {
			// calculate progress angle
			final float progressAngle = (mProgress - mMin) * 360 / (mMax - mMin);
			
			//draw progress
			mArcPaint.setColor(mProgressColor);
			canvas.drawArc(mArcRect, 90, progressAngle, false, mArcPaint);
			
			//draw rest
			if (mProgress > mMin) {
				mArcPaint.setColor(mProgressRestColor);
				canvas.drawArc(mArcRect, 90 + progressAngle, 360 - progressAngle, false, mArcPaint);
			}
		}
			 
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);		

		//vars used for calculation
        final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        final int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        final int heigthWithoutPadding = height - getPaddingTop() - getPaddingBottom();
        
        //this is the main metric to be calculated
		int size = 0;

		/*
		  as there is no internal restriction to the control size, 
		  trying to get this information from the container
		*/ 
        if ((MeasureSpec.EXACTLY ==  heightSpecMode) || (MeasureSpec.EXACTLY == widthSpecMode)) {
        	
        	if ((0 == widthWithoutPadding) || (0 == heigthWithoutPadding)) {
        		size = Math.max(widthWithoutPadding, heigthWithoutPadding);
        	} else {
        		size = Math.min(widthWithoutPadding, heigthWithoutPadding);
        	}
        	
        	//get size if specified exactly
        	//size = Math.max(widthWithoutPadding, heigthWithoutPadding);
        	
        	if (size > 0 ) {
        		mMostSize = size;
        	}
        	
        } else {
        	//unable to get to know the size, return 0
        	size = 0;
        }
        
        // return most size if it was calculated during previous EXACTLY measure request
        if ((widthSpecMode == MeasureSpec.AT_MOST) || (heightSpecMode == MeasureSpec.AT_MOST)) {
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
