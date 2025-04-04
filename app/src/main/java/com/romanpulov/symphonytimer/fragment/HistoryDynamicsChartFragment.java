package com.romanpulov.symphonytimer.fragment;

import android.content.Context;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import com.romanpulov.library.view.BarChart;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.model.DMTimerRec;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 04.01.2016.
 */
public class HistoryDynamicsChartFragment extends HistoryChartFragment<List<LinkedHashMap<Long, Long>>> {
    private int mHistColor0;
    private int mHistColor1;
    private int mColor0;
    private int mColor1;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mHistColor0 = context.getColor(R.color.chartGradientHist0Color);
        mHistColor1 = context.getColor(R.color.chartGradientHistColor);
        mColor0 = context.getColor(R.color.chartGradient0Color);
        mColor1 = context.getColor(R.color.chartGradientColor);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        historyModel.getFilterId().observe(this, filterId -> historyModel.loadHistList());

        historyModel.getHistList().observe(getViewLifecycleOwner(),
                data -> updateBarChart(model.getCurrentDMTimerMap(), data));
    }

    @Override
    protected void updateSeries(Map<Long, DMTimerRec> dmTimerMap, List<LinkedHashMap<Long, Long>> data) {
        //find unique entries in reverse order
        LinkedHashMap<Long, Long> uList = new LinkedHashMap<>();
        for (int i = data.size() - 1; i >= 0; i--) {
            uList.putAll(data.get(i));
        }

        boolean isHist = true;
        for (HashMap<Long, Long> histItem : data) {
            BarChart.Series series = binding.historyTopBarChart.addSeries();
            if (isHist)
                series.setGradientColors(
                        mHistColor0,
                        mHistColor1
                );
            else
                series.setGradientColors(
                        mColor0,
                        mColor1
                );
            int position = 1;
            for (Map.Entry<Long, Long> argumentItem : uList.entrySet()) {
                Long histItemValue = histItem.get(argumentItem.getKey());
                DMTimerRec timer = dmTimerMap.get(argumentItem.getKey());
                if (timer != null)
                    series.addXY(position++, timer.getTitle(), histItemValue == null ? 0 : histItemValue);
            }
            isHist = false;
        }
    }
}
