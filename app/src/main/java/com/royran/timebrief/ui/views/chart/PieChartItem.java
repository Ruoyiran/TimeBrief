
package com.royran.timebrief.ui.views.chart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.royran.timebrief.models.RealmTimeRecord;

import com.royran.timebrief.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PieChartItem extends ChartItem {

    private final Typeface mTf;
    private final SpannableString mCenterText;
    private PieChart mPieChart;
    private PieData mChartData;

    public PieChartItem(Context c) {
        mTf = Typeface.createFromAsset(c.getAssets(), "OpenSans-Bold.ttf");
        mCenterText = generateCenterText();
    }

    public void setDataList(List<RealmTimeRecord> records) {
        mChartData = generateDataPie(records);
        onDataChanged();
    }

    private void onDataChanged() {
        if (mPieChart == null || mChartData == null) {
            return;
        }

        mChartData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.CHINA, "%.2f%%", value);
            }
        });
        mChartData.setValueTypeface(mTf);
        mChartData.setValueTextSize(11f);
        mChartData.setValueTextColor(Color.WHITE);
        // set data
        mPieChart.setData(mChartData);

        mPieChart.invalidate();
        mPieChart.animateY(700);
    }

    private PieData generateDataPie(List<RealmTimeRecord> records) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        Map<String, Long> recordMap = new HashMap<>();
        long totalTimeMs = 0;
        for (RealmTimeRecord record : records) {
            String key = record.getTitle();
            if (!recordMap.containsKey(key)) {
                recordMap.put(key, 0L);
            }
            long elapsedTimeMs = record.getElapsedTimeMillis();
            recordMap.put(key, recordMap.get(key) + elapsedTimeMs);
            totalTimeMs += elapsedTimeMs;
        }
        for (Map.Entry<String, Long> entry: recordMap.entrySet()) {
            if (totalTimeMs == 0) {
                entries.add(new PieEntry(0, entry.getKey()));
            } else {
                entries.add(new PieEntry(entry.getValue()/(float) totalTimeMs,  entry.getKey()));
            }
        }

        PieDataSet d = new PieDataSet(entries, "");
        // space between slices
        d.setSliceSpace(2f);
        d.setColors(ColorTemplate.VORDIPLOM_COLORS);

        return new PieData(d);
    }


    @Override
    public int getItemType() {
        return TYPE_PIECHART;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, Context c) {

        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(c).inflate(
                    R.layout.list_item_piechart, null);
            holder.mPieChart = convertView.findViewById(R.id.chart);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        mPieChart = holder.mPieChart;

        mPieChart.setUsePercentValues(true);
        mPieChart.getDescription().setEnabled(false);
        mPieChart.setExtraOffsets(5, 10, 5, 5);

        mPieChart.setDragDecelerationFrictionCoef(0.95f);

        mPieChart.setCenterTextTypeface(mTf);
        mPieChart.setCenterText(mCenterText);

        mPieChart.setExtraOffsets(20.f, 0.f, 20.f, 0.f);

        mPieChart.setDrawHoleEnabled(true);
        mPieChart.setHoleColor(Color.WHITE);

        mPieChart.setTransparentCircleColor(Color.WHITE);
        mPieChart.setTransparentCircleAlpha(110);

        mPieChart.setHoleRadius(58f);
        mPieChart.setTransparentCircleRadius(61f);

        mPieChart.setDrawCenterText(true);

        mPieChart.setRotationAngle(0);
        // enable rotation of the mPieChart by touch
        mPieChart.setRotationEnabled(true);
        mPieChart.setHighlightPerTapEnabled(true);

        // add a selection listener
//        mPieChart.setOnChartValueSelectedListener(this);


        mPieChart.animateY(1400, Easing.EaseInOutQuad);
        // mPieChart.spin(2000, 0, 360);

        Legend l = mPieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setEnabled(false);
        
        // apply styling
        return convertView;
    }

    private SpannableString generateCenterText() {
        SpannableString s = new SpannableString("时间投资");
        s.setSpan(new RelativeSizeSpan(1.8f), 0, 4, 0);
        s.setSpan(new ForegroundColorSpan(ColorTemplate.VORDIPLOM_COLORS[0]), 0, 4, 0);
        return s;
    }

    private static class ViewHolder {
        PieChart mPieChart;
    }
}
