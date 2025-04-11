package com.romanpulov.symphonytimer.adapter;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.databinding.SymphonyRowViewBinding;
import com.romanpulov.symphonytimer.model.DMTaskItem;
import com.romanpulov.symphonytimer.model.TimerViewModel;
import com.romanpulov.symphonytimer.utils.RoundedBitmapBackgroundBuilder;
import com.romanpulov.library.view.ProgressCircle;
import com.romanpulov.symphonytimer.helper.UriHelper;
import com.romanpulov.symphonytimer.model.DMTimerRec;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.appcompat.view.ActionMode;

import java.util.*;
import java.util.function.BiConsumer;

public class SymphonyArrayAdapter extends RecyclerView.Adapter<SymphonyArrayAdapter.ViewHolder> {
    private static final String TAG = SymphonyArrayAdapter.class.getSimpleName();
    private static final long LIST_CLICK_DELAY = 1000;

    private final Context mContext;
	private List<DMTimerRec> mValues;
    private Map<Long, DMTaskItem> mTaskMap;
    private final BiConsumer<DMTimerRec, Integer> mTimerInteractionListener;
    private final ListViewSelector mListViewSelector;
    private long mLastClickTime;

    public ListViewSelector getListViewSelector() {
        return mListViewSelector;
    }

    private boolean mIsBitmapBackground;
	private RoundedBitmapBackgroundBuilder mBackgroundBuilder;
    private int mItemHeight = 0;
    private int mItemWidth = 0;

    private final View.OnLayoutChangeListener mOnLayoutChangeListener = (
            v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
        if ((mItemHeight == 0) || (mItemWidth == 0)) {
            Log.d(TAG, "onLayoutChange: measuring height");
            int measuredWidth = right - left;
            int measuredHeight = bottom - top;
            if ((measuredWidth > 0) && (measuredHeight > 0)) {
                mItemHeight = measuredHeight;
                mItemWidth = measuredWidth;

                if (mIsBitmapBackground && createBackgroundBuilder()) {
                    ViewHolder viewHolder = (ViewHolder) v.getTag();
                    viewHolder.mNormalDrawable = mBackgroundBuilder.buildDrawable(RoundedBitmapBackgroundBuilder.BG_NORMAL);
                    viewHolder.mFinalDrawable = mBackgroundBuilder.buildDrawable(RoundedBitmapBackgroundBuilder.BG_FINAL);

                    v.setBackground(getBackgroungDrawable(viewHolder, viewHolder.getBindingAdapterPosition()));
                }
            }
        } else {
            Log.d(TAG, "onLayoutChange: removing listener");
            v.removeOnLayoutChangeListener(SymphonyArrayAdapter.this.mOnLayoutChangeListener);
        }
    };

    private Drawable getBackgroungDrawable(@NonNull SymphonyArrayAdapter.ViewHolder viewHolder, int position) {
        if (viewHolder.mSelectedItemPos == -1) {
            return  0 == viewHolder.mDisplayProgress ? viewHolder.mFinalDrawable : viewHolder.mNormalDrawable;
        } else if (position == viewHolder.mSelectedItemPos) {
            return mBackgroundBuilder.buildDrawable(RoundedBitmapBackgroundBuilder.BG_PRESSED_ONLY);
        } else {
            return mBackgroundBuilder.buildDrawable(RoundedBitmapBackgroundBuilder.BG_NORMAL_ONLY);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SymphonyRowViewBinding binding = SymphonyRowViewBinding.inflate(LayoutInflater.from(parent.getContext()));
        View view = binding.getRoot();
        return new ViewHolder(view, binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SymphonyArrayAdapter.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            Log.d(TAG, "onBindViewHolder: payloads is empty");
        } else {
            Log.d(TAG, "onBindViewHolder: payloads " + payloads);
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onBindViewHolder(@NonNull SymphonyArrayAdapter.ViewHolder viewHolder, int position) {
        //background drawer
        mIsBitmapBackground = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("pref_bitmap_background", false);

        DMTimerRec item = mValues.get(position);

        //calculate progress
        DMTaskItem taskItem = mTaskMap != null ? mTaskMap.get(item.getId()) : null;
        int timerProgress = taskItem == null ? 0 : (int)taskItem.getProgressInSec();

        //calculate date used for display
        viewHolder.mDisplayProgress = item.getTimeSec() - timerProgress;
        viewHolder.mSelectedItemPos = mListViewSelector.getSelectedItemPos();

        //create and store backgrounds for better performance
        if (mIsBitmapBackground && (null != mBackgroundBuilder)) {
            viewHolder.mNormalDrawable = mBackgroundBuilder.buildDrawable(RoundedBitmapBackgroundBuilder.BG_NORMAL);
            viewHolder.mFinalDrawable = mBackgroundBuilder.buildDrawable(RoundedBitmapBackgroundBuilder.BG_FINAL);
        }

        if ((mItemHeight == 0) || (mItemWidth == 0)) {
            Log.d(TAG, "Adding LayoutChangeListener for position " + position);
            viewHolder.itemView.addOnLayoutChangeListener(mOnLayoutChangeListener);
            viewHolder.itemView.setTag(viewHolder);
        } else {
            Log.d(TAG, "Position " + position + ": height and width known");
        }

        viewHolder.mTitleTextView.setText(item.getTitle());

        //display image
        viewHolder.mImageView.setImageURI(
                null != item.getImageName() ? UriHelper.fileNameToUri(mContext, item.getImageName()) : null);

        //display text
        viewHolder.mProgressTextView.setText(String.format(Locale.getDefault(),
                "%02d:%02d:%02d",
                viewHolder.mDisplayProgress / 3600,
                viewHolder.mDisplayProgress % 3600 / 60,
                viewHolder.mDisplayProgress % 60));

        //display circle bar
        viewHolder.mProgressCircle.setMax((int) item.getTimeSec());
        viewHolder.mProgressCircle.setProgress(timerProgress);
        //ensure minimum progress for active item
        viewHolder.mProgressCircle.setAlwaysVisible(((taskItem != null) && (timerProgress == 0)));

        if (mIsBitmapBackground ) {
            if ((viewHolder.mNormalDrawable == null) || (viewHolder.mFinalDrawable == null)) {
                if (createBackgroundBuilder()) {
                    viewHolder.mNormalDrawable = mBackgroundBuilder.buildDrawable(RoundedBitmapBackgroundBuilder.BG_NORMAL);
                    viewHolder.mFinalDrawable = mBackgroundBuilder.buildDrawable(RoundedBitmapBackgroundBuilder.BG_FINAL);
                }
            }

            viewHolder.itemView.setBackground(getBackgroungDrawable(viewHolder, position));
        } else {
            //update solid background
            int bgResId;
            if (viewHolder.mSelectedItemPos == -1)
                bgResId =  0 == viewHolder.mDisplayProgress ? R.drawable.main_list_bg_final_selector : R.drawable.main_list_bg_selector;
            else if (position == viewHolder.mSelectedItemPos) {
                bgResId = R.drawable.main_list_shape_selected;
            } else
                bgResId = R.drawable.main_list_shape;

            viewHolder.itemView.setBackgroundResource(bgResId);
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener{
        final TextView mTitleTextView;
        final ImageView mImageView;
        final TextView mProgressTextView;
        final ProgressCircle mProgressCircle;

        Drawable mNormalDrawable;
        Drawable mFinalDrawable;

        long mDisplayProgress;
        int mSelectedItemPos;

        public ViewHolder(View view, SymphonyRowViewBinding binding) {
            super(view);
            mTitleTextView = binding.titleTextView;
            mImageView = binding.imageImageView;
            mProgressTextView = binding.progressTextView;
            mProgressCircle = binding.progressCircle;

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
		}

        private void updateSelectedTitle() {
            ActionMode actionMode;
            DMTimerRec selectedItem;

            if ((mListViewSelector != null) &&
                    ((actionMode = mListViewSelector.getActionMode()) != null) &&
                    ((selectedItem = mValues.get(mListViewSelector.getSelectedItemPos())) != null))
                actionMode.setTitle(selectedItem.getTitle());
        }

        @Override
        public boolean onLongClick(View v) {
            if (validateClickDelay()) {
                mListViewSelector.startActionMode(v, getBindingAdapterPosition());
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void onClick(View v) {
            if (validateClickDelay()) {
                if (mListViewSelector.getSelectedItemPos() == -1) {
                    if (mTimerInteractionListener != null) {
                        mTimerInteractionListener.accept(mValues.get(getBindingAdapterPosition()), getBindingAdapterPosition());
                    }
                } else
                    mListViewSelector.setSelectedView(getBindingAdapterPosition());
                updateSelectedTitle();
            }
        }
    }
	
	public SymphonyArrayAdapter(
            Context context,
            ActionMode.Callback actionModeCallback,
            List<DMTimerRec> values,
            Map<Long, DMTaskItem> taskItemMap,
            BiConsumer<DMTimerRec, Integer> timerInteractionListener) {
        mContext = context;
        mValues = values;
        mTaskMap = taskItemMap;
        mTimerInteractionListener = timerInteractionListener;
        mListViewSelector = new ListViewSelector(this, actionModeCallback);
	}

    private boolean validateClickDelay() {
        long clickTime = System.currentTimeMillis();
        if (clickTime - mLastClickTime > LIST_CLICK_DELAY) {
            mLastClickTime = clickTime;
            return true;
        } else {
            return false;
        }
    }

    private boolean createBackgroundBuilder() {
        if ((mItemWidth > 0) && (mItemHeight > 0) && (mBackgroundBuilder == null))
            mBackgroundBuilder = new RoundedBitmapBackgroundBuilder(
                    mContext,
                    mItemWidth,
                    mItemHeight,
                    mContext.getResources().getDimension(R.dimen.corner_radius));
        return (mBackgroundBuilder != null);
    }

    public void updateValues(List<DMTimerRec> values, RecyclerView recyclerView) {
        Log.d(TAG, "Adapter updating values");
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(getValuesDiffCallback(values));

        int selectedPos = -1;
        if (mValues.size() != values.size()) {
            // add or delete actions
            mListViewSelector.destroyActionMode();

            if (mValues.size() < values.size()) {
                selectedPos = values.size() - 1;
            } else if (!mValues.isEmpty()) {
                selectedPos = 0;
            }
        } else if (mListViewSelector.getSelectedItemPos() != -1) {
            // move actions
            long selectedId = mValues.get(mListViewSelector.getSelectedItemPos()).getId();
            selectedPos = values
                    .stream()
                    .filter(v -> v.getId() == selectedId)
                    .map(values::indexOf)
                    .findFirst()
                    .orElse(-1);
            if (selectedPos != -1) {
                mListViewSelector.setSelectedView(selectedPos);
            }
        }

        this.mValues = values;
        diffResult.dispatchUpdatesTo(this);

        if (selectedPos != -1) {
            Log.d(TAG, "Adapter scrolling to selected position " + selectedPos);
            recyclerView.scrollToPosition(selectedPos);
        }
    }

    public void updateTasks(Map<Long, DMTaskItem> taskMap, RecyclerView recyclerView) {
        Log.d(TAG, "Adapter updating tasks");
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(getTasksDiffCallback(taskMap));

        DMTaskItem beforeLastTaskItemCompleted = TimerViewModel.getLastTaskItemCompleted(mTaskMap);
        DMTaskItem afterLastTaskItemCompleted = TimerViewModel.getLastTaskItemCompleted(taskMap);

        mTaskMap = taskMap == null ? null : new HashMap<>(taskMap);
        Log.d(TAG, "Adapter dispatching updates");
        diffResult.dispatchUpdatesTo(this);

        if ((!Objects.equals(beforeLastTaskItemCompleted, afterLastTaskItemCompleted)) &&
                (afterLastTaskItemCompleted != null)) {
                for (int i = 0; i < mValues.size(); i++) {
                    if (mValues.get(i).getId() == afterLastTaskItemCompleted.getId()) {
                        Log.d(TAG, "Adapter scrolling to position " + i);
                        recyclerView.scrollToPosition(i);
                    }
                }
        }
    }

    private DiffUtil.Callback getValuesDiffCallback(List<DMTimerRec> newValues) {
        return new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return mValues.size();
            }

            @Override
            public int getNewListSize() {
                return newValues.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return mValues.get(oldItemPosition).getId() == newValues.get(newItemPosition).getId();
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return mValues.get(oldItemPosition).getId() == newValues.get(newItemPosition).getId() &&
                        mValues.get(oldItemPosition).getTimeSec() == newValues.get(newItemPosition).getTimeSec() &&
                        Objects.equals(mValues.get(oldItemPosition).getTitle(), newValues.get(newItemPosition).getTitle()) &&
                        Objects.equals(mValues.get(oldItemPosition).getImageName(), newValues.get(newItemPosition).getImageName());
            }
        };
    }

    private DiffUtil.Callback getTasksDiffCallback(Map<Long, DMTaskItem> newTasks) {
        return new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return mValues.size();
            }

            @Override
            public int getNewListSize() {
                return mValues.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return true;
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                DMTaskItem oldTaskItem = mTaskMap == null ? null : mTaskMap.get(mValues.get(oldItemPosition).getId());
                DMTaskItem newTaskItem = newTasks == null ? null : newTasks.get(mValues.get(newItemPosition).getId());

                if (oldTaskItem != null && newTaskItem != null) {
                    Log.d(TAG, "oldTaskItem progress: " + oldTaskItem.getProgressInSec() + ", newTaskItem: " + newTaskItem);
                }

                return ((oldTaskItem == null) && (newTaskItem == null)) ||
                        ((oldTaskItem != null) && (newTaskItem != null) &&
                                (oldTaskItem.getProgressInSec() == newTaskItem.getProgressInSec()) &&
                                (oldTaskItem.getCompleted() == newTaskItem.getCompleted()));
            }
        };
    }
}