package com.romanpulov.symphonytimer.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.adapter.HistoryArrayAdapter;
import com.romanpulov.symphonytimer.databinding.FragmentHistoryListBinding;
import com.romanpulov.symphonytimer.model.TimerHistoryViewModel;
import com.romanpulov.symphonytimer.model.TimerViewModel;
import com.romanpulov.symphonytimer.utils.SpaceItemDecoration;

public class HistoryListFragment extends Fragment {

	private FragmentHistoryListBinding binding;

    @Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		binding = FragmentHistoryListBinding.inflate(getLayoutInflater());
		/*
		View rootView = inflater.inflate(R.layout.fragment_history_list, container, false);
		mAdapter = new HistoryArrayAdapter(this.getActivity(), mDMTimerHistList, mDMTimers);
		((ListView)rootView.findViewById(R.id.history_list_view)).setAdapter(mAdapter);

		 */
        return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.historyListView.setLayoutManager(new LinearLayoutManager(requireContext()));
		binding.historyListView.addItemDecoration(new SpaceItemDecoration((int)requireContext().getResources().getDimension(R.dimen.list_divider_height)));

		TimerViewModel model = TimerViewModel.getInstance(requireActivity().getApplication());
		TimerHistoryViewModel historyModel = new ViewModelProvider(requireParentFragment()).get(TimerHistoryViewModel.class);

		historyModel.getFilterId().observe(this, filterId -> historyModel.loadDMTimerHistList());

		historyModel.getDMTimerHistList().observe(getViewLifecycleOwner(), histList -> {
			if (binding.historyListView.getAdapter() == null) {
				HistoryArrayAdapter adapter = new HistoryArrayAdapter(requireContext(), histList, model.getCurrentDMTimerMap());
				binding.historyListView.setAdapter(adapter);
			} else {
				HistoryArrayAdapter adapter = (HistoryArrayAdapter)binding.historyListView.getAdapter();
				adapter.updateDMTimerHistList(histList);
			}
		});
	}

	/*
	@Override
	public void setHistoryFilterId(int historyFilterId) {
		super.setHistoryFilterId(historyFilterId);
		DBHelper.getInstance(this.getActivity()).fillHistList(mDMTimerHistList, historyFilterId);
		if (null != mAdapter) {
			mAdapter.notifyDataSetChanged();
		}
	}

	 */
}
