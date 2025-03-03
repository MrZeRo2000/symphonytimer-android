package com.romanpulov.symphonytimer.adapter;

import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.model.DMTaskItem;
import com.romanpulov.symphonytimer.utils.RoundedBitmapBackgroundBuilder;
import com.romanpulov.library.view.ProgressCircle;
import com.romanpulov.symphonytimer.helper.UriHelper;
import com.romanpulov.symphonytimer.model.DMTasks;
import com.romanpulov.symphonytimer.model.DMTimerRec;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.appcompat.view.ActionMode;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

public class SymphonyArrayAdapter extends ArrayAdapter<DMTimerRec> {
	private final List<DMTimerRec> mValues;
    private Map<Long, DMTaskItem> mTaskItemMap;
	private DMTasks mTasks;
    private final BiConsumer<DMTaskItem, Integer> mTimerInteractionListener;
    private final ListViewSelector mListViewSelector;

    public ListViewSelector getListViewSelector() {
        return mListViewSelector;
    }

	private RoundedBitmapBackgroundBuilder mBackgroundBuilder;
    private int mItemHeight = 0;
    private int mItemWidth = 0;

	private class ViewHolder implements View.OnLongClickListener, View.OnClickListener{
        final View mView;
        int mPosition;
        final TextView mTitleTextView;
        final ImageView mImageView;
        final TextView mProgressTextView;
        final ProgressCircle mProgressCircle;

        Drawable mNormalDrawable;
        Drawable mFinalDrawable;
		
		ViewHolder(View view, DMTimerRec item, int position) {
            mView = view;
            mPosition = position;
            mTitleTextView = view.findViewById(R.id.title_text_view);
			mImageView = view.findViewById(R.id.image_image_view);
			mProgressTextView = view.findViewById(R.id.progress_text_view);
			mProgressCircle = view.findViewById(R.id.progress_circle);

            mView.setOnClickListener(this);
            mView.setOnLongClickListener(this);
		}

        private void updateSelectedTitle() {
            ActionMode actionMode;
            DMTimerRec selectedItem;

            if ((mListViewSelector != null) && ((actionMode = mListViewSelector.getActionMode()) != null) && ((selectedItem = getItem(mListViewSelector.getSelectedItemPos())) != null))
                actionMode.setTitle(selectedItem.mTitle);
        }


        @Override
        public boolean onLongClick(View v) {
            mListViewSelector.startActionMode(v, mPosition);
            return true;
        }

        @Override
        public void onClick(View v) {
            if (mListViewSelector.getSelectedItemPos() == -1) {
                if (mTimerInteractionListener != null) {
                    mTimerInteractionListener.accept(null, mPosition);
                }
            } else
                mListViewSelector.setSelectedView(mPosition);
            updateSelectedTitle();
        }
    }
	
	public SymphonyArrayAdapter(
            Context context,
            ActionMode.Callback actionModeCallback,
            List<DMTimerRec> values,
            DMTasks tasks,
            BiConsumer<DMTaskItem, Integer> timerInteractionListener) {
		super(context, R.layout.symphony_row_view);
		mValues = values;
		mTasks = tasks;
        mTimerInteractionListener = timerInteractionListener;
        mListViewSelector = new ListViewSelector(this, actionModeCallback);
	}
	
	public void setTasks(DMTasks tasks) {
		this.mTasks = tasks;
	}

    public void setTaskItemMap(Map<Long, DMTaskItem> mTaskItemMap) {
        this.mTaskItemMap = mTaskItemMap;
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
		final DMTimerRec item = mValues.get(position);
		
		//calculate progress
        DMTaskItem taskItem = mTasks != null ? mTasks.getTaskItemById(item.mId) :
                mTaskItemMap != null ? mTaskItemMap.get(item.mId) : null;

		int timerProgress = taskItem == null ? 0 : (int)taskItem.getProgressInSec();
		final long displayProgress = item.mTimeSec - timerProgress;

		//background drawer
		final boolean isBitmapBackground = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("pref_bitmap_background", false);
		
		View rowView;
		ViewHolder viewHolder;
		
		if (convertView == null) {
			//inflate from layout

			LayoutInflater inflater = LayoutInflater.from(getContext());
			rowView = inflater.inflate(R.layout.symphony_row_view, parent, false);
			
			//setup holder
			viewHolder = new ViewHolder(rowView, item, position);
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
		
		// create viewHolder(just in case) and ensure update position (important!!!)
		if (null == viewHolder) {
			viewHolder = new ViewHolder(rowView, item, position);
			rowView.setTag(viewHolder);
		} else
            //update position
            viewHolder.mPosition = position;

		viewHolder.mTitleTextView.setText(item.mTitle);

		//display image
		viewHolder.mImageView.setImageURI(
                null != item.mImageName ? UriHelper.fileNameToUri(getContext(), item.mImageName) : null);

		//display text
		viewHolder.mProgressTextView.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", displayProgress / 3600, displayProgress % 3600 / 60, displayProgress % 60));
		
		//display circle bar
		viewHolder.mProgressCircle.setMax((int) item.mTimeSec);
		viewHolder.mProgressCircle.setProgress(timerProgress);
        //ensure minimum progress for active item
        viewHolder.mProgressCircle.setAlwaysVisible(((taskItem != null) && (timerProgress == 0)));

        //background change depending on selection
        int selectedItemPos = mListViewSelector.getSelectedItemPos();

        if (isBitmapBackground ) {
            if ((viewHolder.mNormalDrawable == null) || (viewHolder.mFinalDrawable == null)) {
                if (createBackgroundBuilder()) {
                    viewHolder.mNormalDrawable = mBackgroundBuilder.buildDrawable(RoundedBitmapBackgroundBuilder.BG_NORMAL);
                    viewHolder.mFinalDrawable = mBackgroundBuilder.buildDrawable(RoundedBitmapBackgroundBuilder.BG_FINAL);
                }
            }
            //update bitmap background
            Drawable bgDrawable;
            if (selectedItemPos == -1)
                bgDrawable = 0 == displayProgress  ? viewHolder.mFinalDrawable : viewHolder.mNormalDrawable;
            else if (viewHolder.mPosition == selectedItemPos) {
                bgDrawable = mBackgroundBuilder.buildDrawable(RoundedBitmapBackgroundBuilder.BG_PRESSED_ONLY);
            } else
                bgDrawable = mBackgroundBuilder.buildDrawable(RoundedBitmapBackgroundBuilder.BG_NORMAL_ONLY);

            rowView.setBackground(bgDrawable);
        } else {
            //update solid background
            int bgResId;
            if (selectedItemPos == -1)
                bgResId =  0 == displayProgress ? R.drawable.main_list_bg_final_selector : R.drawable.main_list_bg_selector;
            else if (viewHolder.mPosition == selectedItemPos) {
                bgResId = R.drawable.main_list_shape_selected;
            } else
                bgResId = R.drawable.main_list_shape;

            rowView.setBackgroundResource(bgResId);
        }

		return rowView;
	}

    private boolean createBackgroundBuilder() {
        if ((mItemWidth > 0) && (mItemHeight > 0) && (mBackgroundBuilder == null))
            mBackgroundBuilder = new RoundedBitmapBackgroundBuilder(getContext(), mItemWidth, mItemHeight, getContext().getResources().getDimension(R.dimen.corner_radius));
        return (mBackgroundBuilder != null);
    }
}