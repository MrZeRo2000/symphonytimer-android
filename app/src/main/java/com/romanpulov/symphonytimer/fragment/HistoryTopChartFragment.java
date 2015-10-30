package com.romanpulov.symphonytimer.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.romanpulov.library.view.BarChart;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.helper.db.DBHelper;
import com.romanpulov.symphonytimer.model.DMTimerHistTopList;
import com.romanpulov.symphonytimer.model.DMTimerHistTopRec;
import com.romanpulov.symphonytimer.model.DMTimers;

public class HistoryTopChartFragment extends HistoryFragment {
    private static final String ARG_TIMERS = "timers";
    private static final String ARG_HISTORY_FILTER_ID = "history_filter_id";

    private DMTimers mDMTimers;
    private int mHistoryFilterId;
    private BarChart mBarChart;

    public static HistoryTopChartFragment newInstance(DMTimers dmTimers, int historyFilterId) {
        HistoryTopChartFragment fragment = new HistoryTopChartFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TIMERS, dmTimers);
        args.putInt(ARG_HISTORY_FILTER_ID, historyFilterId);
        fragment.setArguments(args);
        return fragment;
    }

    public HistoryTopChartFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDMTimers = getArguments().getParcelable(ARG_TIMERS);
            mHistoryFilterId = getArguments().getInt(ARG_HISTORY_FILTER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_history_top_chart, container, false);
        mBarChart = (BarChart)rootView.findViewById(R.id.history_top_bar_chart);
        updateBarChart();
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void updateBarChart() {
        DMTimerHistTopList timerHistTopList = new DMTimerHistTopList();
        DBHelper.getInstance(this.getActivity()).fillHistTopList(timerHistTopList, mHistoryFilterId);
        timerHistTopList.calcPercent();

        //update data
        mBarChart.clearSeries();
        BarChart.Series series = mBarChart.addSeries();
        series.setGradientColors(0xff4b6cb7, 0xff182848);
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
        mHistoryFilterId = historyFilterId;
        if (mBarChart != null) {
            updateBarChart();
        }
    }
}
