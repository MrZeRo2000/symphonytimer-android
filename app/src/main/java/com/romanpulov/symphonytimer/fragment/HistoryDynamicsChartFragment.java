package com.romanpulov.symphonytimer.fragment;

import com.romanpulov.library.view.BarChart;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.helper.db.DBHelper;
import com.romanpulov.symphonytimer.model.DMTimerExecutionList;
import com.romanpulov.symphonytimer.model.DMTimerExecutionRec;
import com.romanpulov.symphonytimer.model.DMTimerRec;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 04.01.2016.
 */
public class HistoryDynamicsChartFragment extends HistoryChartFragment {
    @Override
    protected void updateSeries() {
        //calc data
        //get from database
        List<LinkedHashMap<Long, Long>> histList = DBHelper.getInstance(this.getActivity()).getHistList(mHistoryFilterId, 2);
        //find unique entries in reverse order
        LinkedHashMap<Long, Long> uList = new LinkedHashMap<>();
        for (int i = histList.size() - 1; i >= 0; i--) {
            for (Map.Entry<Long, Long> item : histList.get(i).entrySet()) {
                uList.put(item.getKey(), item.getValue());
            }
        }

        boolean isHist = true;
        for (HashMap<Long, Long> histItem : histList) {
            BarChart.Series series = getBarChart().addSeries();
            if (isHist)
                series.setGradientColors(
                        getResources().getColor(R.color.chart_gradient_hist_0),
                        getResources().getColor(R.color.chart_gradient_hist)
                );
            else
                series.setGradientColors(
                        getResources().getColor(R.color.chart_gradient_0),
                        getResources().getColor(R.color.chart_gradient)
                );
            int position = 1;
            for (Map.Entry<Long, Long> argumentItem : uList.entrySet()) {
                Long histItemValue = histItem.get(argumentItem.getKey());
                DMTimerRec timer = mDMTimers.getItemById(argumentItem.getKey());
                if (timer != null)
                    series.addXY(position++, timer.mTitle, histItemValue == null ? 0 : histItemValue);
            }
            isHist = false;
        }
    }
}
