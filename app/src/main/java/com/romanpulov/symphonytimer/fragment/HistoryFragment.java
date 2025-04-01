package com.romanpulov.symphonytimer.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.romanpulov.symphonytimer.databinding.FragmentHistoryBinding;
import com.romanpulov.symphonytimer.model.DMTimers;

public class HistoryFragment extends Fragment {
    private static final String ARG_TIMERS = "timers";
    private static final String ARG_HISTORY_FILTER_ID = "history_filter_id";

    private FragmentHistoryBinding binding;

    protected DMTimers mDMTimers;
    protected int mHistoryFilterId = -1;
	protected ArrayAdapter<?> mAdapter;

	@NonNull
    public static <T extends HistoryFragment> T newInstance(Class<T> historyFragmentClass, DMTimers dmTimers, int historyFilterId) {
        T fragment;
        try {
            fragment = historyFragmentClass.newInstance();
        } catch (java.lang.InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        Bundle args = new Bundle();
        args.putParcelable(ARG_TIMERS, dmTimers);
        args.putInt(ARG_HISTORY_FILTER_ID, historyFilterId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDMTimers = getArguments().getParcelable(ARG_TIMERS);
            mHistoryFilterId = getArguments().getInt(ARG_HISTORY_FILTER_ID);
        }
    }
	
	public void setHistoryFilterId(int historyFilterId) {
		mHistoryFilterId = historyFilterId;
	}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
