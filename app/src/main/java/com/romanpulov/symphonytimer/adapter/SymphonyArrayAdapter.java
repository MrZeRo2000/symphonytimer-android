package com.romanpulov.symphonytimer.adapter;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.databinding.SymphonyRowViewBinding;
import com.romanpulov.symphonytimer.model.DMTaskItem;
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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class SymphonyArrayAdapter extends RecyclerView.Adapter<SymphonyArrayAdapter.ViewHolder> {
    private static final String TAG = SymphonyArrayAdapter.class.getSimpleName();

    private final Context mContext;
	private List<DMTimerRec> mValues;
    private Map<Long, DMTaskItem> mTaskItemMap;
    private final BiConsumer<DMTaskItem, Integer> mTimerInteractionListener;
    private final ListViewSelector mListViewSelector;

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
                    v.setBackground(viewHolder.mNormalDrawable);
                }
            }
        } else {
            Log.d(TAG, "onLayoutChange: removing listener");
            v.removeOnLayoutChangeListener(SymphonyArrayAdapter.this.mOnLayoutChangeListener);
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SymphonyRowViewBinding binding = SymphonyRowViewBinding.inflate(LayoutInflater.from(parent.getContext()));
        View view = binding.getRoot();
        return new ViewHolder(view, binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SymphonyArrayAdapter.ViewHolder viewHolder, int position) {
        //background drawer
        mIsBitmapBackground = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("pref_bitmap_background", false);

        if ((mItemHeight == 0) || (mItemWidth == 0)) {
            Log.d(TAG, "Adding LayoutChangeListener for position " + position);
            viewHolder.itemView.addOnLayoutChangeListener(mOnLayoutChangeListener);
            viewHolder.itemView.setTag(viewHolder);
        } else {
            Log.d(TAG, "Position " + position + ": height and width known");
        }

        DMTimerRec item = mValues.get(position);

        //calculate progress
        DMTaskItem taskItem = mTaskItemMap != null ? mTaskItemMap.get(item.getId()) : null;

        int timerProgress = taskItem == null ? 0 : (int)taskItem.getProgressInSec();
        final long displayProgress = item.getTimeSec() - timerProgress;

        //create and store backgrounds for better performance
        if (mIsBitmapBackground && (null != mBackgroundBuilder)) {
            viewHolder.mNormalDrawable = mBackgroundBuilder.buildDrawable(RoundedBitmapBackgroundBuilder.BG_NORMAL);
            viewHolder.mFinalDrawable = mBackgroundBuilder.buildDrawable(RoundedBitmapBackgroundBuilder.BG_FINAL);
        }

        viewHolder.mTitleTextView.setText(item.getTitle());

        //display image
        viewHolder.mImageView.setImageURI(
                null != item.getImageName() ? UriHelper.fileNameToUri(mContext, item.getImageName()) : null);

        //display text
        viewHolder.mProgressTextView.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", displayProgress / 3600, displayProgress % 3600 / 60, displayProgress % 60));

        //display circle bar
        viewHolder.mProgressCircle.setMax((int) item.getTimeSec());
        viewHolder.mProgressCircle.setProgress(timerProgress);
        //ensure minimum progress for active item
        viewHolder.mProgressCircle.setAlwaysVisible(((taskItem != null) && (timerProgress == 0)));

        //background change depending on selection
        int selectedItemPos = mListViewSelector.getSelectedItemPos();

        if (mIsBitmapBackground ) {
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
            else if (position == selectedItemPos) {
                bgDrawable = mBackgroundBuilder.buildDrawable(RoundedBitmapBackgroundBuilder.BG_PRESSED_ONLY);
            } else
                bgDrawable = mBackgroundBuilder.buildDrawable(RoundedBitmapBackgroundBuilder.BG_NORMAL_ONLY);

            viewHolder.itemView.setBackground(bgDrawable);
        } else {
            //update solid background
            int bgResId;
            if (selectedItemPos == -1)
                bgResId =  0 == displayProgress ? R.drawable.main_list_bg_final_selector : R.drawable.main_list_bg_selector;
            else if (position == selectedItemPos) {
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
            mListViewSelector.startActionMode(v, getBindingAdapterPosition());
            return true;
        }

        @Override
        public void onClick(View v) {
            if (mListViewSelector.getSelectedItemPos() == -1) {
                if (mTimerInteractionListener != null) {
                    mTimerInteractionListener.accept(null, getBindingAdapterPosition());
                }
            } else
                mListViewSelector.setSelectedView(getBindingAdapterPosition());
            updateSelectedTitle();
        }
    }
	
	public SymphonyArrayAdapter(
            Context context,
            ActionMode.Callback actionModeCallback,
            List<DMTimerRec> values,
            Map<Long, DMTaskItem> taskItemMap,
            BiConsumer<DMTaskItem, Integer> timerInteractionListener) {
        mContext = context;
        mValues = values;
        mTaskItemMap = taskItemMap;
        mTimerInteractionListener = timerInteractionListener;
        mListViewSelector = new ListViewSelector(this, actionModeCallback);
	}

    public void setTaskItemMap(Map<Long, DMTaskItem> mTaskItemMap) {
        this.mTaskItemMap = mTaskItemMap;
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
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(getDiffCallback(values));

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
            recyclerView.scrollToPosition(selectedPos);
        }
    }

    private DiffUtil.Callback getDiffCallback(List<DMTimerRec> newValues) {
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
}