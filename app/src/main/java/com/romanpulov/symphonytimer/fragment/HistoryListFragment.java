package com.romanpulov.symphonytimer.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.adapter.HistoryArrayAdapter;
import com.romanpulov.symphonytimer.databinding.FragmentHistoryListBinding;
import com.romanpulov.symphonytimer.helper.db.DBHelper;
import com.romanpulov.symphonytimer.model.DMTimerHistRec;
import com.romanpulov.symphonytimer.model.TimerHistoryViewModel;
import com.romanpulov.symphonytimer.model.TimerViewModel;
import com.romanpulov.symphonytimer.utils.SpaceItemDecoration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HistoryListFragment extends Fragment {
	private final List<DMTimerHistRec> mDMTimerHistList = new ArrayList<>();

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
		historyModel.getDMTimerHistList().observe(getViewLifecycleOwner(), histList -> {
			HistoryArrayAdapter adapter = new HistoryArrayAdapter(requireContext(), histList, model.getCurrentDMTimerMap());
			binding.historyListView.setAdapter(adapter);
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
