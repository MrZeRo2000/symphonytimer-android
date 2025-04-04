package com.romanpulov.symphonytimer.fragment;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.romanpulov.library.view.BarChart;
import com.romanpulov.symphonytimer.model.DMTimerExecutionRec;
import com.romanpulov.symphonytimer.model.DMTimerRec;

import java.util.List;
import java.util.Map;

public class HistoryTopChartFragment extends HistoryChartFragment<List<DMTimerExecutionRec>> {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        historyModel.getFilterId().observe(this, filterId -> historyModel.loadDMTimerExecutionList());

        historyModel.getDMTimerExecutionList().observe(getViewLifecycleOwner(),
                data -> updateBarChart(model.getCurrentDMTimerMap(), data));
    }

    @Override
    protected void updateSeries(Map<Long, DMTimerRec> dmTimerMap, List<DMTimerExecutionRec> data) {
        //update data
        BarChart.Series series = binding.historyTopBarChart.addSeries();
        for (int position = 0; position < data.size(); position++) {
            DMTimerExecutionRec rec =  data.get(position);

            DMTimerRec dmTimer = dmTimerMap.get(rec.mTimerId);
            String title = dmTimer == null ? "" : dmTimer.getTitle();

            series.addXY(position + 1, title, rec.mExecCnt);
        }
    }
}
