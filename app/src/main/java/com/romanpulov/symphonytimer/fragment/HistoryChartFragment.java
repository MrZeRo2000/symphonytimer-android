package com.romanpulov.symphonytimer.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.romanpulov.symphonytimer.databinding.FragmentHistoryTopChartBinding;
import com.romanpulov.symphonytimer.model.DMTimerRec;
import com.romanpulov.symphonytimer.model.TimerHistoryViewModel;
import com.romanpulov.symphonytimer.model.TimerViewModel;

import java.util.Map;

/**
 * Created on 04.01.2016.
 */
public abstract class HistoryChartFragment<T> extends Fragment {

    protected FragmentHistoryTopChartBinding binding;

    protected TimerViewModel model;
    protected TimerHistoryViewModel historyModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHistoryTopChartBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button.OnClickListener buttonClickListener = v -> {
            int barChartWidth = binding.historyTopBarChart.getBarItemWidth();
            int barChartDelta = v == binding.scaleUpButton ? barChartWidth / 10 : -barChartWidth / 10;
            binding.historyTopBarChart.setBarItemWidth(barChartWidth + barChartDelta);
            binding.historyTopBarChart.updateChartLayout();
            binding.historyTopBarChart.requestLayout();
            binding.historyTopBarChart.invalidate();
        };
        binding.scaleUpButton.setOnClickListener(buttonClickListener);
        binding.scaleDownButton.setOnClickListener(buttonClickListener);

        model = TimerViewModel.getInstance(requireActivity().getApplication());
        historyModel = new ViewModelProvider(requireParentFragment()).get(TimerHistoryViewModel.class);
    }

    protected void updateBarChart(Map<Long, DMTimerRec> dmTimerMap, T data) {
        //clear old data
        binding.historyTopBarChart.clearSeries();

        updateSeries(dmTimerMap, data);

        //update control
        binding.historyTopBarChart.updateSeriesListValueBounds();
        binding.historyTopBarChart.updateChartLayout();
        binding.historyTopBarChart.requestLayout();
        binding.historyTopBarChart.invalidate();
    }

    // to override for data retrieval
    abstract protected void updateSeries(Map<Long, DMTimerRec> dmTimerMap, T data);
}
