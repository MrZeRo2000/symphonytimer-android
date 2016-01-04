package com.romanpulov.symphonytimer.fragment;

import com.romanpulov.library.view.BarChart;
import com.romanpulov.symphonytimer.helper.db.DBHelper;
import com.romanpulov.symphonytimer.model.DMTimerExecutionList;
import com.romanpulov.symphonytimer.model.DMTimerExecutionRec;

public class HistoryTopChartFragment extends HistoryChartFragment {
    @Override
    protected void updateSeries() {
        //calc data
        DMTimerExecutionList timerExecutionList = new DMTimerExecutionList();
        DBHelper.getInstance(this.getActivity()).fillHistTopList(timerExecutionList, mHistoryFilterId);
        timerExecutionList.calcPercent();
        //update data
        BarChart.Series series = getBarChart().addSeries();
        for (int position = 0; position < timerExecutionList.size(); position++) {
            DMTimerExecutionRec rec =  timerExecutionList.get(position);
            series.addXY(position + 1, mDMTimers.getItemById(rec.mTimerId).mTitle, rec.mExecCnt);
        }
    }
}
