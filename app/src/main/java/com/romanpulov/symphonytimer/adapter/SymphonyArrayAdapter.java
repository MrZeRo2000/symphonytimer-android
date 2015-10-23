package com.romanpulov.symphonytimer.adapter;

import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.utils.RoundedBitmapBackgroundBuilder;
import com.romanpulov.library.view.ProgressCircle;
import com.romanpulov.symphonytimer.helper.UriHelper;
import com.romanpulov.symphonytimer.model.DMTasks;
import com.romanpulov.symphonytimer.model.DMTimerRec;
import com.romanpulov.symphonytimer.model.DMTimers;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

public class SymphonyArrayAdapter extends android.widget.ArrayAdapter<DMTimerRec>{
	
	private final Context mContext;
	private final DMTimers mValues;
	private DMTasks mTasks;
	private RoundedBitmapBackgroundBuilder mBackgroundBuilder;
    private int mItemHeight = 0;
    private int mItemWidth = 0;
	
	static class ViewHolder {
		TextView mTitleTextView;
		ImageView mImageView;
		TextView mProgressTextView;
		ProgressCircle mProgressCircle;
        Drawable mNormalDrawable;
        Drawable mFinalDrawable;
		
		public ViewHolder(View view) {
			this.mTitleTextView = (TextView)view.findViewById(R.id.title_text_view);
			this.mImageView = (ImageView)view.findViewById(R.id.image_image_view);
			this.mProgressTextView = (TextView)view.findViewById(R.id.progress_text_view);
			this.mProgressCircle = (ProgressCircle)view.findViewById(R.id.progress_circle);
		}
	}
	
	public SymphonyArrayAdapter(Context context, DMTimers values, DMTasks tasks) {
		super(context, R.layout.symphony_row_2_view);
		this.mContext = context;
		this.mValues = values;
		this.mTasks = tasks;
	}
	
	public void setTasks(DMTasks tasks) {
		this.mTasks = tasks;
	}
	
	@Override
    public int getCount() {
        return mValues.size();
    }
	
	@Override
    public DMTimerRec getItem(int position) {
        return mValues.get(position);
    }		
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

        long startTime = System.nanoTime();
		
		final DMTimerRec dmTimerRec = mValues.get(position);
		
		//calculate progress
		final long timerProgress = mTasks.getTaskItemProgress(dmTimerRec.mId);
		final long displayProgress = dmTimerRec.mTimeSec - timerProgress;
		
		//background drawer
		final boolean isBitmapBackground = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("pref_bitmap_background", false);
		
		View rowView;
		ViewHolder viewHolder;
		
		if (convertView == null) {
            Log.d("SymphonyArrayAdapter", "convertView = null");
			
			//inflate from layout
			LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
			rowView = inflater.inflate(R.layout.symphony_row_2_view, parent, false);
			
			//setup holder
			viewHolder = new ViewHolder(rowView);
            //store holder
            rowView.setTag(viewHolder);
            //create and store backgrounds for better performance
            if (isBitmapBackground && (null != mBackgroundBuilder)) {
                viewHolder.mNormalDrawable = mBackgroundBuilder.buildDrawable(RoundedBitmapBackgroundBuilder.BG_NORMAL);
                viewHolder.mFinalDrawable = mBackgroundBuilder.buildDrawable(RoundedBitmapBackgroundBuilder.BG_FINAL);
            }

            //setup listener to query layout only once
            if ((mItemHeight == 0) || (mItemWidth == 0)) {
                rowView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        if ((mItemHeight == 0) || (mItemWidth == 0)) {
                            int measuredWidth = right - left;
                            int measuredHeight = bottom - top;
                            if ((measuredWidth > 0) && (measuredHeight > 0)) {
                                mItemHeight = measuredHeight;
                                mItemWidth = measuredWidth;
                                if (isBitmapBackground && createBackgroundBuilder()) {
                                    ViewHolder viewHolder = (ViewHolder) v.getTag();
                                    viewHolder.mNormalDrawable = mBackgroundBuilder.buildDrawable(RoundedBitmapBackgroundBuilder.BG_NORMAL);
                                    viewHolder.mFinalDrawable = mBackgroundBuilder.buildDrawable(RoundedBitmapBackgroundBuilder.BG_FINAL);
                                    v.setBackground(viewHolder.mNormalDrawable);
                                }
                            }
                        }
                    }
                });
            }

        }
		else { 
			rowView = convertView;
			viewHolder = (ViewHolder)rowView.getTag();
		}
		
		// create viewHolder(just in case)
		if (null == viewHolder) {
			viewHolder = new ViewHolder(rowView);
			rowView.setTag(viewHolder);
            Log.d("SymphonyArrayAdapter", "viewHolder unexpectedly created !!!");
		}

		viewHolder.mTitleTextView.setText(dmTimerRec.mTitle);

		//display image
		viewHolder.mImageView.setImageURI(
				null != dmTimerRec.mImageName ? UriHelper.fileNameToUri(getContext(), dmTimerRec.mImageName) : null);

		//display text
		viewHolder.mProgressTextView.setText(String.format("%02d:%02d:%02d", (long) displayProgress / 3600, (long) displayProgress % 3600 / 60, displayProgress % 60));
		
		//display circle bar
		viewHolder.mProgressCircle.setMax((int)dmTimerRec.mTimeSec);
		viewHolder.mProgressCircle.setProgress((int)timerProgress);

        //background change
        if (isBitmapBackground ) {
            if ((viewHolder.mNormalDrawable == null) || (viewHolder.mFinalDrawable == null)) {
                if (createBackgroundBuilder()) {
                    viewHolder.mNormalDrawable = mBackgroundBuilder.buildDrawable(RoundedBitmapBackgroundBuilder.BG_NORMAL);
                    viewHolder.mFinalDrawable = mBackgroundBuilder.buildDrawable(RoundedBitmapBackgroundBuilder.BG_FINAL);
                }
            }
            //update bitmap background
            rowView.setBackground((0 == displayProgress ) ? viewHolder.mFinalDrawable : viewHolder.mNormalDrawable);
        } else {
            //update solid background
            rowView.setBackgroundResource(
                    0 == displayProgress ? R.drawable.main_list_bg_final_selector : R.drawable.main_list_bg_selector
            );
        }

        long endTime = System.nanoTime();
        Log.d("SymphonyArrayAdapter", "Execution time: " + (endTime - startTime) + " ns");

		return rowView;
	}

    public boolean createBackgroundBuilder() {
        if ((mItemWidth > 0) && (mItemHeight > 0)) {
            Log.d("SymphonyArrayAdapter", "creating Background Builder");
            if (mBackgroundBuilder == null)
                mBackgroundBuilder = new RoundedBitmapBackgroundBuilder(mContext, mItemWidth, mItemHeight, 6);
        };
        return (mBackgroundBuilder != null);
    }

}