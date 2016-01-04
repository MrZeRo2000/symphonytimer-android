package com.romanpulov.symphonytimer.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.romanpulov.library.view.BarChart;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.helper.db.DBHelper;
import com.romanpulov.symphonytimer.model.DMTimerHistTopList;
import com.romanpulov.symphonytimer.model.DMTimerHistTopRec;

public class HistoryTopChartFragment extends HistoryChartFragment {
    @Override
    protected void updateSeries() {
        //calc data
        DMTimerHistTopList timerHistTopList = new DMTimerHistTopList();
        DBHelper.getInstance(this.getActivity()).fillHistTopList(timerHistTopList, mHistoryFilterId);
        timerHistTopList.calcPercent();
        //update data
        BarChart.Series series = getBarChart().addSeries();
        for (int position = 0; position < timerHistTopList.size(); position++) {
            DMTimerHistTopRec rec =  timerHistTopList.get(position);
            series.addXY(position + 1, mDMTimers.getItemById(rec.mTimerId).mTitle, rec.mExecCnt);
        }
    }
}
