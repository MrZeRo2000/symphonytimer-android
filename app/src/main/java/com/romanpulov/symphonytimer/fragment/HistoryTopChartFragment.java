package com.romanpulov.symphonytimer.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.romanpulov.library.view.BarChart;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.helper.db.DBHelper;
import com.romanpulov.symphonytimer.model.DMTimerHistTopList;
import com.romanpulov.symphonytimer.model.DMTimerHistTopRec;

public class HistoryTopChartFragment extends HistoryFragment {
    private BarChart mBarChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_history_top_chart, container, false);
        mBarChart = (BarChart)rootView.findViewById(R.id.history_top_bar_chart);
        updateBarChart();
        return rootView;
    }

    private void updateBarChart() {
        DMTimerHistTopList timerHistTopList = new DMTimerHistTopList();
        DBHelper.getInstance(this.getActivity()).fillHistTopList(timerHistTopList, mHistoryFilterId);
        timerHistTopList.calcPercent();

        //update data
        mBarChart.clearSeries();
        BarChart.Series series = mBarChart.addSeries();
        for (int position = 0; position < timerHistTopList.size(); position++) {
            DMTimerHistTopRec rec =  timerHistTopList.get(position);
            series.addXY(position + 1, mDMTimers.getItemById(rec.mTimerId).mTitle, rec.mExecCnt);
        }
        //update control
        mBarChart.updateSeriesListValueBounds();
        mBarChart.updateChartLayout();
        mBarChart.requestLayout();
        mBarChart.invalidate();
    }

    @Override
    public void setHistoryFilterId(int historyFilterId) {
        super.setHistoryFilterId(historyFilterId);
        if (mBarChart != null) {
            updateBarChart();
        }
    }
}
