package com.romanpulov.symphonytimer.fragment;

import android.content.Context;

import androidx.annotation.NonNull;

import com.romanpulov.library.view.BarChart;
import com.romanpulov.symphonytimer.R;
import com.romanpulov.symphonytimer.helper.db.DBHelper;
import com.romanpulov.symphonytimer.model.DMTimerRec;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 04.01.2016.
 */
public class HistoryDynamicsChartFragment extends HistoryChartFragment {
    private int mHistColor0;
    private int mHistColor1;
    private int mColor0;
    private int mColor1;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mHistColor0 = getResources().getColor(R.color.chartGradientHist0Color);
        mHistColor1 = getResources().getColor(R.color.chartGradientHistColor);
        mColor0 = getResources().getColor(R.color.chartGradient0Color);
        mColor1 = getResources().getColor(R.color.chartGradientColor);
    }

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
                DMTimerRec timer = mDMTimers.getItemById(argumentItem.getKey());
                if (timer != null)
                    series.addXY(position++, timer.getTitle(), histItemValue == null ? 0 : histItemValue);
            }
            isHist = false;
        }
    }
}
