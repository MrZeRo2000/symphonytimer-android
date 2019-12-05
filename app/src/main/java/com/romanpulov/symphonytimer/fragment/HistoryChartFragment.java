package com.romanpulov.symphonytimer.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.romanpulov.library.view.BarChart;
import com.romanpulov.symphonytimer.R;

/**
 * Created on 04.01.2016.
 */
public abstract class HistoryChartFragment extends HistoryFragment {
    private BarChart mBarChart;

    protected BarChart getBarChart() {
        return mBarChart;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_history_top_chart, container, false);
        mBarChart = rootView.findViewById(R.id.history_top_bar_chart);
        updateBarChart();

        final Button scaleUpButton = rootView.findViewById(R.id.scaleUpButton);
        final Button scaleDownButton = rootView.findViewById(R.id.scaleDownButton);
        Button.OnClickListener buttonClickListener = new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                int barChartWidth = mBarChart.getBarItemWidth();
                int barChartDelta = v == scaleUpButton ? barChartWidth / 10 : -barChartWidth / 10;
                mBarChart.setBarItemWidth(barChartWidth + barChartDelta);
                mBarChart.updateChartLayout();
                mBarChart.requestLayout();
                mBarChart.invalidate();
            }
        };
        scaleUpButton.setOnClickListener(buttonClickListener);
        scaleDownButton.setOnClickListener(buttonClickListener);

        return rootView;
    }

    private void updateBarChart() {
        //clear old data
        mBarChart.clearSeries();

        updateSeries();

        //update control
        mBarChart.updateSeriesListValueBounds();
        mBarChart.updateChartLayout();
        mBarChart.requestLayout();
        mBarChart.invalidate();
    }

    // to override for data retrieval
    abstract protected void updateSeries();

    @Override
    public void setHistoryFilterId(int historyFilterId) {
        super.setHistoryFilterId(historyFilterId);
        if (mBarChart != null) {
            updateBarChart();
        }
    }
}
