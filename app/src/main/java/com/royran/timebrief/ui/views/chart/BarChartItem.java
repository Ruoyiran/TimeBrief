package com.royran.timebrief.ui.views.chart;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.renderer.XAxisRenderer;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.orhanobut.logger.Logger;
import com.royran.timebrief.models.RealmTimeRecord;
import com.royran.timebrief.ui.activity.BaseActivity;
import com.royran.timebrief.ui.activity.MainActivity;
import com.royran.timebrief.utils.StringUtils;
import com.royran.timebrief.utils.TimeRecordUtils;
import com.royran.timebrief.utils.TimeUtils;

import com.royran.timebrief.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BarChartItem implements OnChartGestureListener {
    @BindView(R.id.bar_chart)
    BarChart chart;

    @BindView(R.id.text_total_records)
    TextView textTotalRecords;

    @BindView(R.id.text_total_events)
    TextView textTotalEvents;

    @BindView(R.id.text_total_time)
    TextView textTotalTime;

    @BindView(R.id.text_time_usage)
    TextView textTimeUsage;

    ChartData mChartData;

    private OnChartItemSelectedListener mListener;

    class TimeRecordSummary {
        int totalRecords;
        int totalEvents;
        long totalTimeMs;
        float timeUsage;
    }

    private final Typeface mTf;
    private final int mLabelsCount = 7;
    private List<List<RealmTimeRecord>> mDataRecords;
    private List<Integer> mAllColors;
    private BaseActivity mBaseActivity;

    private boolean mBarChartSelected;
    private boolean mChartSortAscend = true;
    private List<RealmTimeRecord> mAllRecords;

    private BarChartType mChartType = BarChartType.TotalTime;
    private BarChartSortMethod mChartSortMethod = BarChartSortMethod.Time;

    public BarChartItem(BaseActivity activity, View view) {
        ButterKnife.bind(this, view);
        mTf = Typeface.createFromAsset(activity.getAssets(), "OpenSans-Regular.ttf");
        mBaseActivity = activity;
        mDataRecords = new ArrayList<>();
        collectColors();
        // apply styling
        initSetting();
        addChartValueSelectedListener();
    }

    @OnClick(R.id.layout_summary)
    void onSummaryLayoutClicked() {
        if (mBarChartSelected) {
            mBarChartSelected = false;
            clearHighlightState();
            refreshContents(mDataRecords);
            if (mListener != null) {
                mListener.onChartItemNothingSelected();
            }
        }
    }

    public void updateChart(List<RealmTimeRecord> records) {
        mAllRecords = records;
        mChartData = generateChartData(records);
        onDataChanged();
    }

    public BarChartSortMethod getChartSortMethod() {
        return mChartSortMethod;
    }

    public boolean isChartSortAscend() {
        return mChartSortAscend;
    }

    private ValueFormatter getXAxisValueFormatter() {
        return new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (mDataRecords == null || mDataRecords.size() == 0) {
                    return "";
                }
                if (index < 0 || index >= mDataRecords.size()) {
                    return "";
                }
                long timeMs = mDataRecords.get(index).get(0).getEndTimeMillis();
                String week = TimeUtils.getWeekString(timeMs);
                String date = TimeUtils.getChinaTimeString(timeMs, "MM.dd");
                return week + "\n" + date;
            }
        };
    }

    private ValueFormatter getYAxisValueFormatter() {
        return new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (mChartType == BarChartType.Times) {
                    return (int)value + "";
                } else if (mChartType == BarChartType.TotalTime) {
                    value /= 1000;
                    float newVal = value/3600;
                    if (newVal < 1.0f) {
                        newVal = newVal * 60;
                        return (int)newVal + "m";
                    }
                    return (int)newVal + "h";
                }
                return (int)value + "";
            }
        };
    }

    private ValueFormatter getChartValueFormatter() {
        return new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if ((int) value == 0) {
                    return "";
                }
                if (mChartType == BarChartType.Times) {
                    return (int)value + "";
                } else if (mChartType == BarChartType.TotalTime) {
                    return StringUtils.formatTotalTimeToStringEn((long)value);
                }
                return (int)value + "";
            }
        };
    }

    private void initSetting() {
        /*xy轴设置*/
        //x轴设置显示位置在底部
        XAxis xAxis = chart.getXAxis();
        xAxis.setTypeface(mTf);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        //xAxis.setCenterAxisLabels(true);
        xAxis.setTextSize(10f);
        //不显示X轴 Y轴线条
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setGridColor(Color.TRANSPARENT);
        chart.setXAxisRenderer(
                new CustomXAxisRenderer(chart.getViewPortHandler(),
                xAxis, chart.getTransformer(YAxis.AxisDependency.LEFT)));

        YAxis leftAxis = chart.getAxisLeft();
        YAxis rightAxis = chart.getAxisRight();
        //保证y轴从0开始 不然会上移一点
        leftAxis.setTypeface(mTf);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setSpaceTop(20f);
        leftAxis.setLabelCount(5, false);

        setAxisesFormatter();

        leftAxis.setDrawGridLines(true);
        rightAxis.setEnabled(false);

        /*折现图例 标签 设置*/
        Legend legend = chart.getLegend();
        legend.setEnabled(false);
        //不显示右下角描述内容
        Description description = chart.getDescription();
        description.setEnabled(false);
    }

    private void setAxisesFormatter() {
        XAxis xAxis = chart.getXAxis();
        YAxis leftAxis = chart.getAxisLeft();
        xAxis.setValueFormatter(getXAxisValueFormatter());
        leftAxis.setValueFormatter(getYAxisValueFormatter());
    }

    private TimeRecordSummary getTimeRecordSummary(List<List<RealmTimeRecord>> records) {
        TimeRecordSummary summary = new TimeRecordSummary();
        int totalRecords = 0;
        long totalTimeMs = 0;
        HashSet<String> eventSet = new HashSet<>();
        HashSet<Long> daySet = new HashSet<>();
        for (List<RealmTimeRecord> sub_records : records) {
            totalRecords += sub_records.size();
            for (RealmTimeRecord record : sub_records) {
                eventSet.add(record.getTitle());
                totalTimeMs += record.getElapsedTimeMillis();
                daySet.add(TimeUtils.getChinaDate(record.getEndTimeMillis()).getTimeInMillis());
            }
        }
        summary.totalRecords = totalRecords;
        summary.totalEvents = eventSet.size();
        summary.totalTimeMs = totalTimeMs;
        if (daySet.size() == 0) {
            summary.timeUsage = 0;
        } else {
            summary.timeUsage = totalTimeMs * 100 / (float) (daySet.size() * TimeUtils.ONE_DAY_MILLISENDS);
        }
        return summary;
    }

    public void setOnChartItemSelectedListener(OnChartItemSelectedListener listener) {
        mListener = listener;
    }

    private void addChartValueSelectedListener() {
        chart.setOnChartGestureListener(this);
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                List<RealmTimeRecord> records = (List<RealmTimeRecord>) e.getData();
                if (records == null) {
                    onSummaryLayoutClicked();
                    return;
                }
                List<List<RealmTimeRecord>> dataList = new ArrayList<>();
                dataList.add(records);
                refreshContents(dataList);
                mBarChartSelected = true;
                if (mListener != null) {
                    mListener.onChartItemSelected(dataList);
                }
            }

            @Override
            public void onNothingSelected() {
                mBarChartSelected = false;
                refreshContents(mDataRecords);
                if (mListener != null) {
                    mListener.onChartItemNothingSelected();
                }
            }
        });
    }

    private void refreshContents(List<List<RealmTimeRecord>> records) {
        TimeRecordSummary summary = getTimeRecordSummary(records);
        textTotalRecords.setText(summary.totalRecords + "");
        textTotalEvents.setText(summary.totalEvents + "");
        textTotalTime.setText(StringUtils.formatTotalTimeToString(summary.totalTimeMs));
        DecimalFormat df = new DecimalFormat("######0.00");
        textTimeUsage.setText(df.format(summary.timeUsage) + "%");
    }


    private ChartData generateChartData(List<RealmTimeRecord> records) {
        ArrayList<BarEntry> entries = getBarEntryList(records);
        if (entries == null) {
            return null;
        }
        BarDataSet d = new BarDataSet(entries, getChartSetLabel());
        d.setColors(mAllColors);
        d.setHighLightAlpha(100);

        BarData cd = new BarData(d);
        cd.setValueTextSize(10);
        cd.setBarWidth(0.5f);
        cd.setValueFormatter(getChartValueFormatter());
        return cd;
    }

    private String getChartSetLabel() {
        switch (mChartType) {
            case TotalTime:
                return "时长";
            case Times:
                return "次数";
        }
        return "次数";
    }

    private void addColors(List<Integer> colorList, int[] colors) {
        if (colorList == null) {
            return;
        }
        for (int c : colors) {
            colorList.add(c);
        }
    }

    private void collectColors() {
        mAllColors = new ArrayList<>();
        addColors(mAllColors, ColorTemplate.JOYFUL_COLORS);
        addColors(mAllColors, ColorTemplate.MATERIAL_COLORS);
        addColors(mAllColors, ColorTemplate.COLORFUL_COLORS);
        addColors(mAllColors, ColorTemplate.VORDIPLOM_COLORS);
        addColors(mAllColors, ColorTemplate.PASTEL_COLORS);
        addColors(mAllColors, ColorTemplate.LIBERTY_COLORS);
    }

    private ArrayList<BarEntry> getBarEntryList(List<RealmTimeRecord> records) {
        mDataRecords.clear();
        if (records == null || records.size() == 0) {
            return null;
        }
        ArrayList<BarEntry> entries = new ArrayList<>();
        Map<Long, List<RealmTimeRecord>> dataMap = new HashMap<>();
        for (RealmTimeRecord record : records) {
            long timeMs = TimeUtils.getChinaDate(record.getEndTimeMillis()).getTimeInMillis();
            if (!dataMap.containsKey(timeMs)) {
                dataMap.put(timeMs, new ArrayList<>());
            }
            dataMap.get(timeMs).add(record);
        }
        List<List<RealmTimeRecord>> allGroupedRecords = new ArrayList<>();
        for (Map.Entry<Long, List<RealmTimeRecord>> entry: dataMap.entrySet()) {
            allGroupedRecords.add(entry.getValue());
        }
        if (allGroupedRecords.size() == 0) {
            return null;
        }
        sortGroupedRecords(allGroupedRecords);
        for (int i = 0; i < allGroupedRecords.size(); ++i) {
            List<RealmTimeRecord> r = allGroupedRecords.get(i);
            long value = getBarEntryYValue(r);
            entries.add(new BarEntry(i, value, r));
            mDataRecords.add(r);
        }
        while (entries.size() < mLabelsCount) {
            entries.add(new BarEntry(entries.size(), 0));
        }
        return entries;
    }

    private void sortGroupedRecords(List<List<RealmTimeRecord>> allGroupedRecords) {
        if (mChartType == BarChartType.TotalTime) {
            if (mChartSortMethod == BarChartSortMethod.Count) { // calc total
                Collections.sort(allGroupedRecords, (records1, records2) -> {
                    long t1 = TimeRecordUtils.getTotalElapsedTime(records1);
                    long t2 = TimeRecordUtils.getTotalElapsedTime(records2);
                    return compFunc(t1, t2, mChartSortAscend);
                });
            } else { // time based
                Collections.sort(allGroupedRecords, (records1, records2) -> {
                    if (records1.size() == 0 || records2.size() == 0) {
                        return 0;
                    }
                    long t1 = TimeUtils.getChinaDate(records1.get(0).getEndTimeMillis()).getTimeInMillis();
                    long t2 = TimeUtils.getChinaDate(records2.get(0).getEndTimeMillis()).getTimeInMillis();
                    return compFunc(t1, t2, mChartSortAscend);
                });
            }
        } else { // stat by Times
            if (mChartSortMethod == BarChartSortMethod.Count) { // calc total
                Collections.sort(allGroupedRecords, (records1, records2) -> {
                    long t1 = records1.size();
                    long t2 = records2.size();
                    return compFunc(t1, t2, mChartSortAscend);
                });
            } else { // time based
                Collections.sort(allGroupedRecords, (records1, records2) -> {
                    if (records1.size() == 0 || records2.size() == 0) {
                        return 0;
                    }
                    long t1 = TimeUtils.getChinaDate(records1.get(0).getEndTimeMillis()).getTimeInMillis();
                    long t2 = TimeUtils.getChinaDate(records2.get(0).getEndTimeMillis()).getTimeInMillis();
                    return compFunc(t1, t2, mChartSortAscend);
                });
            }
        }
    }

    private int compFunc(long t1, long t2, boolean ascend) {
        if (ascend) {
            if (t1 > t2) {
                return 1;
            } else if (t1 < t2) {
                return -1;
            }
            return 0;
        } else {
            if (t1 > t2) {
                return -1;
            } else if (t1 < t2) {
                return 1;
            }
            return 0;
        }
    }

    private long getBarEntryYValue(List<RealmTimeRecord> realmTimeRecords) {
        switch (mChartType) {
            case Times:
                return realmTimeRecords.size();
            case TotalTime:
                return TimeRecordUtils.getTotalElapsedTime(realmTimeRecords);
        }
        return realmTimeRecords.size();
    }

    public class CustomXAxisRenderer extends XAxisRenderer {
        public CustomXAxisRenderer(ViewPortHandler viewPortHandler, XAxis xAxis, Transformer trans) {
            super(viewPortHandler, xAxis, trans);
        }

        @Override
        protected void drawLabel(Canvas c, String formattedLabel, float x, float y, MPPointF anchor, float angleDegrees) {
            String values[] = formattedLabel.split("\n");
            int spacing = 10;
            Utils.drawXAxisValue(c, values[0], x, y, mAxisLabelPaint, anchor, angleDegrees);
            if (values.length > 1) {
                Utils.drawXAxisValue(c, values[1], x, y + mAxisLabelPaint.getTextSize()+spacing, mAxisLabelPaint, anchor, angleDegrees);
            }
        }
    }

    private void clearHighlightState() {
        if (chart == null) {
            return;
        }
        chart.highlightValue(null);
    }

    private void onDataChanged() {
        refreshChart();
        refreshContents(mDataRecords);
    }

    private void refreshChart() {
        mBarChartSelected = false;
        if (chart == null) {
            return;
        }
        if (mChartData == null) {
            chart.clear();
            return;
        }
        refreshContents(mDataRecords);
        XAxis xAxis = chart.getXAxis();
        chart.setXAxisRenderer(new CustomXAxisRenderer(chart.getViewPortHandler(), xAxis, chart.getTransformer(YAxis.AxisDependency.LEFT)));
        chart.setExtraBottomOffset(20);

        xAxis.setLabelCount(mChartData.getEntryCount());
        clearHighlightState();
        mChartData.setValueTypeface(mTf);
        mChartData.setValueTextSize(10);
        Matrix m = new Matrix();
        int count = mChartData.getEntryCount();
        if (count <= mLabelsCount) {
            m.postScale(count / (float) mLabelsCount, 1f);//两个参数分别是x,y轴的缩放比例。例如：将x轴的数据放大为之前的1.5倍
        } else {
            m.postScale(scaleNum(count), 1f);//两个参数分别是x,y轴的缩放比例。例如：将x轴的数据放大为之前的1.5倍
        }
        chart.setData((BarData) mChartData);
        chart.getViewPortHandler().refresh(m, chart, false);//将图表动画显示之前进行缩放
        setChartEffect(chart);
        chart.invalidate();
    }

    public void setChartType(BarChartType chartType, BarChartSortMethod chartSortMethod, boolean isAscend) {
        mChartType = chartType;
        mChartSortMethod = chartSortMethod;
        mChartSortAscend = isAscend;
        setAxisesFormatter();
        mChartData = generateChartData(mAllRecords);
        refreshChart();
    }

    public BarChartType getChartType() {
        return mChartType;
    }
    

    /*设置图表效果*/
    private void setChartEffect(BarChart barChart){
        //不可以手动缩放
        barChart.setScaleXEnabled(false);
        barChart.setScaleYEnabled(false);
        barChart.setScaleEnabled(false);

        //背景颜色
        barChart.setBackgroundColor(Color.WHITE);
        //不显示图表网格
        barChart.setDrawGridBackground(false);
        barChart.setDrawBorders(false);
        //背景阴影
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false);
        //设置动画效果
        barChart.animateY(700, Easing.Linear);
        barChart.animateX(700, Easing.Linear);
    }

    private float scalePercent = 1f/(float) mLabelsCount;

    private float scaleNum(int xCount){
        return xCount * scalePercent;
    }


    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Highlight highlight = chart.getHighlightByTouchPoint(me.getX(), me.getY());
        if (highlight == null) {
            return;
        }
        int index = (int)highlight.getX();
        if (mDataRecords == null || index < 0 || index >= mDataRecords.size()) {
            if (mDataRecords == null) {
                Logger.e("mDataRecords == null");
            } else {
                Logger.e("index: %d, mDataRecords size: %d", index, mDataRecords.size());
            }
            return;
        }
        List<RealmTimeRecord> records = mDataRecords.get(index);
        if (records.size() == 0) {
            Logger.e("records is empty, index: %d", index);
            Toast.makeText(mBaseActivity, mBaseActivity.getString(R.string.no_records), Toast.LENGTH_SHORT).show();
            return;
        }
        MainActivity.getInstance().jumpToTodayPage(records.get(0).getEndTimeMillis());
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

    }

    public interface OnChartItemSelectedListener {
        void onChartItemSelected(List<List<RealmTimeRecord>> records);

        void onChartItemNothingSelected();
    }

}
