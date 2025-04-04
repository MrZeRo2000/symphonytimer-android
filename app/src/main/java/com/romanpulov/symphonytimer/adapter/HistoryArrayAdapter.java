package com.romanpulov.symphonytimer.adapter;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.databinding.HistoryRowViewBinding;
import com.romanpulov.symphonytimer.helper.DateFormatterHelper;
import com.romanpulov.symphonytimer.model.DMTimerHistRec;
import com.romanpulov.symphonytimer.model.DMTimerRec;


public class HistoryArrayAdapter extends RecyclerView.Adapter<HistoryArrayAdapter.ViewHolder> {

	private final Context mContext;
	private final Map<Long, DMTimerRec> mDMTimerMap;
	private final List<DMTimerHistRec> mDMTimerHistList;

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		HistoryRowViewBinding binding = HistoryRowViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
		View view = binding.getRoot();
		return new ViewHolder(view, binding);
	}

	@Override
	public void onBindViewHolder(@NonNull HistoryArrayAdapter.ViewHolder viewHolder, int position) {
		DMTimerHistRec rec = mDMTimerHistList.get(position);
		DMTimerRec dmTimerRec = mDMTimerMap.get(rec.timerId);

		viewHolder.mTime.setText(DateFormatterHelper.format(rec.startTime));
		if (dmTimerRec != null) {
			viewHolder.mTitle.setText(dmTimerRec.getTitle());
			viewHolder.mImage.setImageURI(
					null != dmTimerRec.getImageName() ? Uri.parse(dmTimerRec.getImageName()) : null);
		}

		boolean hideTimeDetails = !PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("pref_full_history_info", false);

		if ((rec.realTime == 0) || hideTimeDetails) {
			viewHolder.mTimeDetails.setVisibility(View.GONE);
		}
		else {
			viewHolder.mTimeDetails.setVisibility(View.VISIBLE);
			String detailsText = mContext.getString(R.string.caption_due_real_time,
					DateFormatterHelper.formatTime(rec.endTime), DateFormatterHelper.formatTime(rec.realTime));
			//viewHolder.mTimeDetails.setText("Due time : " + DateFormatterHelper.formatTime(rec.mEndTime) + ", real time : " + DateFormatterHelper.formatTime(rec.mRealTime));
			viewHolder.mTimeDetails.setText(detailsText);
		}
	}

	@Override
	public int getItemCount() {
		return mDMTimerHistList.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public final TextView mTitle;
		public final ImageView mImage;
		public final TextView mTime;
		public final TextView mTimeDetails;
		
		public ViewHolder(View view, HistoryRowViewBinding binding) {
			super(view);

			mTitle = binding.historyTextView;
			mImage = binding.historyImageView;
			mTime = binding.historyTimeView;
			mTimeDetails = binding.historyTimeDetailsView;
		}
	}

	public HistoryArrayAdapter(Context context, List<DMTimerHistRec> dmTimerHistList, Map<Long, DMTimerRec> dmTimerMap) {
		mContext = context;
		mDMTimerHistList = dmTimerHistList;
		mDMTimerMap = dmTimerMap;
	}

	public void updateDMTimerHistList(List<DMTimerHistRec> dmTimerHistList) {
		DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(getDMTimerHistListDiffCallback(dmTimerHistList));
		diffResult.dispatchUpdatesTo(this);
	}

	private DiffUtil.Callback getDMTimerHistListDiffCallback(List<DMTimerHistRec> newDMTimerHistList) {
		return new DiffUtil.Callback() {
			@Override
			public int getOldListSize() {
				return mDMTimerHistList.size();
			}

			@Override
			public int getNewListSize() {
				return newDMTimerHistList.size();
			}

			@Override
			public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
				return mDMTimerHistList.get(oldItemPosition).id == newDMTimerHistList.get(newItemPosition).id;
			}

			@Override
			public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
				return areItemsTheSame(oldItemPosition, newItemPosition);
			}
		};
	}
}
