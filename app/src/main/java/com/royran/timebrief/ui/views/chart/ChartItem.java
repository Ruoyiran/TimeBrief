package com.royran.timebrief.ui.views.chart;

import android.content.Context;
import android.view.View;

import com.github.mikephil.charting.data.ChartData;

/**
 * Base class of the Chart ListView items
 * @author philipp
 *
 */
@SuppressWarnings("unused")
public abstract class ChartItem {

    static final int TYPE_BARCHART = 0;
    static final int TYPE_LINECHART = 1;
    static final int TYPE_PIECHART = 2;

    ChartData<?> mChartData;

    public abstract int getItemType();

    public abstract View getView(int position, View convertView, Context c);
}
