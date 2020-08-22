
package com.royran.timebrief.ui.views.chart;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.View;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.orhanobut.logger.Logger;
import com.royran.timebrief.models.DateType;
import com.royran.timebrief.models.RealmTimeRecord;
import com.royran.timebrief.ui.activity.MainActivity;
import com.royran.timebrief.ui.fragment.StatisticsFragment;
import com.royran.timebrief.ui.views.calendar.CustomWeekBar;
import com.royran.timebrief.utils.RealmHelper;
import com.royran.timebrief.utils.StringUtils;
import com.royran.timebrief.utils.TimeRecordUtils;
import com.royran.timebrief.utils.TimeUtils;

import com.royran.timebrief.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LineChartItem {
    private Context mContext;

    private final Typeface mTf;

    @BindView(R.id.line_chart)
    LineChart mLineChart;

    private List<List<RealmTimeRecord>> mDataRecords;
    private List<RealmTimeRecord> mPriorRecords;
    private Calendar mStartCalendar;
    private Calendar mEndCalendar;
    private long mSelectedCustomStartTimeMs;
    private long mSelectedCustomEndTimeMs;
    List<String> mXLabels;

    public interface OnLineChartValueSelectdListener {
        void onValueSelected(List<RealmTimeRecord> currRecords, List<RealmTimeRecord> prevRecords);
        void onNothingSelected();
    }

    public void setOnLineChartValueSelectedListener(final OnLineChartValueSelectdListener listener) {
        if (mLineChart == null) {
            return;
        }
        mLineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int index = (int) e.getX();
                if (mDataRecords == null || index < 0 || index >= mDataRecords.size()) {
                    if (listener != null) {
                        listener.onValueSelected(null, null);
                    }
                } else {
                    if (listener != null) {
                        if (index > 0) {
                            listener.onValueSelected(mDataRecords.get(index), mDataRecords.get(index-1));
                        } else {
                            listener.onValueSelected(mDataRecords.get(index), mPriorRecords);
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected() {
                if (listener != null) {
                    listener.onNothingSelected();
                }
            }
        });
    }

    public LineChartItem(Context c, View itemView) {
        ButterKnife.bind(this, itemView);
        mTf = Typeface.createFromAsset(c.getAssets(), "OpenSans-Regular.ttf");
        mContext = c;
        mDataRecords = new ArrayList<>();
        mXLabels = new ArrayList<>();
        initSettings();

    }

    private void initSettings() {
        // apply styling
        if (mLineChart == null) {
            Logger.e("mLineChart == null");
            return;
        }
        MyMarkerView mv = new MyMarkerView(mContext, R.layout.custom_marker_view);
        mv.setChartView(mLineChart); // For bounds control
        mLineChart.setMarker(mv); // Set the marker to the chart

        mLineChart.getDescription().setEnabled(false);
        mLineChart.setDrawGridBackground(false);

        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(mTf);
        xAxis.setTextColor(Color.rgb(0, 100, 100));
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setAxisMinimum(0);
        xAxis.setGranularity(1);
        xAxis.setValueFormatter(getXAxisValueFormatter());

        YAxis leftAxis = mLineChart.getAxisLeft();
        leftAxis.setTypeface(mTf);
        leftAxis.setLabelCount(getMaxRange()/2+2, false);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setValueFormatter(getYAxisValueFormatter());

        YAxis rightAxis = mLineChart.getAxisRight();
        rightAxis.setEnabled(false);
        setChartEffect(mLineChart);

        mLineChart.getLegend().setEnabled(false);

    }

    public void setDataList(List<RealmTimeRecord> records) {
        if (mLineChart == null) {
            return;
        }
        if (records == null || records.isEmpty()) {
            mLineChart.clear();
            return;
        }
        mStartCalendar = MainActivity.getInstance().getStatisticsFragment().getStartCalendar();
        mEndCalendar = MainActivity.getInstance().getStatisticsFragment().getEndCalendar();
        mSelectedCustomStartTimeMs = MainActivity.getInstance().getStatisticsFragment().getSelectedCustomStartTimeMs();
        mSelectedCustomEndTimeMs = MainActivity.getInstance().getStatisticsFragment().getSelectedCustomEndTimeMs();
        mLineChart.setData(generateDataLine(records));
        mLineChart.invalidate();
    }

    /*设置图表效果*/
    private void setChartEffect(LineChart chart){
        //不可以手动缩放
        chart.setScaleXEnabled(false);
        chart.setScaleYEnabled(false);
        chart.setScaleEnabled(false);

        //背景颜色
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.setDrawGridBackground(false);
        chart.setDrawBorders(false);

        //设置动画效果
        chart.animateX(700, Easing.Linear);
    }

    private ValueFormatter getXAxisValueFormatter() {
        return new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int index = (int) value;
                if (index < 0 || index >= mXLabels.size()) {
                    return "";
                }
                return mXLabels.get(index);
            }
        };
    }

    private ValueFormatter getYAxisValueFormatter() {
        return new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                value /= 1000;
                float newVal = value/3600;
                if (newVal < 1.0f) {
                    newVal = newVal * 60;
                    return (int)newVal + "m";
                }
                return (int)newVal + "h";
            }
        };
    }

    private void addDays(int n) {
        mStartCalendar.add(Calendar.DAY_OF_MONTH, n);
        mEndCalendar.add(Calendar.DAY_OF_MONTH, n);
    }

    private void addMonths(int n) {
        mStartCalendar.add(Calendar.MONTH, n);
        mEndCalendar.add(Calendar.MONTH, n);
        mEndCalendar.set(Calendar.DAY_OF_MONTH, mEndCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
    }

    private void addYears(int n) {
        mStartCalendar.add(Calendar.YEAR, n);
        mEndCalendar.add(Calendar.YEAR, n);
    }

    void moveToPrevTimeRange() {
        switch (MainActivity.getInstance().getStatisticsFragment().getCurrentSelectedDateType()) {
            case DAY:
                addDays(-1);
                break;
            case WEEK:
                addDays(-7);
                break;
            case MONTH:
                addMonths(-1);
                break;
            case YEAR:
                addYears(-1);
                break;
            case QUARTER:
                addMonths(-3);
                break;
            case CUSTOM:
                int offset = (int)((mSelectedCustomEndTimeMs-mSelectedCustomStartTimeMs)/ TimeUtils.ONE_DAY_MILLISENDS) + 1;
                addDays(-offset);
                break;
            default:
                break;
        }
    }

    private int getMaxRange() {
        switch (MainActivity.getInstance().getStatisticsFragment().getCurrentSelectedDateType()) {
            case DAY:
                return 30;
            case WEEK:
                return 7;
            case MONTH:
                return 12;
            case YEAR:
                return 5;
            case QUARTER:
                return 4;
            case CUSTOM:
                return 10;
            default:
                break;
        }
        return 7;
    }

    private LineData generateDataLine(List<RealmTimeRecord> records) {
        mXLabels.clear();
        if (records.isEmpty()) {
            return null;
        }
        int maxRange = getMaxRange();
        ArrayList<Entry> values = new ArrayList<>();
        for (int i = 0; i < maxRange; ++i) {
            if (i == 0) {
                mDataRecords.add(records);
            } else {
                moveToPrevTimeRange();
                    List<RealmTimeRecord> prevRecords = getRecords(records.get(0).getTitle());
                    mDataRecords.add(prevRecords);
            }
            mXLabels.add(getXLabel(i));
        }
        moveToPrevTimeRange();
        mPriorRecords = getRecords(records.get(0).getTitle());

        Collections.reverse(mDataRecords);
        Collections.reverse(mXLabels);
        for (int i = 0; i <  mDataRecords.size(); ++i) {
            values.add(new Entry(i, TimeRecordUtils.getTotalElapsedTime(mDataRecords.get(i))));
        }
        LineDataSet dataset = new LineDataSet(values, "BarDataSet");
        dataset.setLineWidth(1f);
        dataset.setColor(Color.WHITE);
        dataset.setCircleRadius(3.5f);
        dataset.setDrawCircles(true);
        dataset.setValueTextSize(10);
        dataset.setCircleColor(Color.WHITE);
        dataset.setCircleHoleColor(Color.rgb(100, 100, 100));
        dataset.setCircleHoleRadius(2f);
        dataset.setHighLightColor(Color.LTGRAY);
        dataset.setDrawValues(false);

        ArrayList<ILineDataSet> sets = new ArrayList<>();
        sets.add(dataset);
        return new LineData(sets);
    }


    private String getMonthString(int index) {
        String[] months = mContext.getResources().getStringArray(R.array.chinese_month_string_array);
        if (index < 1 || index > 12) {
            return "";
        }
        return months[index - 1];
    }

    private String getXLabel(int i) {
        DateType dateType = MainActivity.getInstance().getStatisticsFragment().getCurrentSelectedDateType();
        if (i > 0 && i % 2 == 0) {
            return "";
        }
        switch (dateType) {
            case DAY:
                if (i == 0) {
                    return mContext.getString(R.string.today);
                }
            case WEEK:
                if (i == 0) {
                    return mContext.getString(R.string.this_week);
                }
            case QUARTER:
                if (i == 0) {
                    return mContext.getString(R.string.this_quarter);
                }
            case CUSTOM:
                String s1 = (mStartCalendar.get(Calendar.MONTH)+1) + "." + mStartCalendar.get(Calendar.DAY_OF_MONTH);
                String s2 = (mEndCalendar.get(Calendar.MONTH)+1) + "." + mEndCalendar.get(Calendar.DAY_OF_MONTH);
                if (dateType == DateType.DAY) {
                    return s1;
                }
                return s1 + "-" + s2;
            case MONTH:
                if (i == 0) {
                    return mContext.getString(R.string.this_month);
                }
                int month = mStartCalendar.get(Calendar.MONTH) + 1;
                return getMonthString(month);
            case YEAR:
                if (i == 0) {
                    return mContext.getString(R.string.this_year);
                }
                int year = mStartCalendar.get(Calendar.YEAR);
                return year + "";
            default:
                break;
        }
        return "";
    }

    private List<RealmTimeRecord> getRecords(String title) {
        StatisticsFragment fragment = MainActivity.getInstance().getStatisticsFragment();
        if (fragment.getCurrentSelectedDateType() == DateType.ALL) {
            return new ArrayList<>();
        }
        List<RealmTimeRecord> records = RealmHelper.getTimeRecordsByTime(mStartCalendar.getTimeInMillis(),
                mEndCalendar.getTimeInMillis()+TimeUtils.ONE_DAY_MILLISENDS);
        List<RealmTimeRecord> subRecords = new ArrayList<>();
        for (RealmTimeRecord record : records) {
            if (title.equals(record.getTitle())) {
                if (TextUtils.isEmpty(fragment.getCurrentSearchText())) {
                    subRecords.add(record);
                } else {
                    boolean match = StringUtils.find(record.getSummary(), fragment.getCurrentSearchText()) >= 0;
                    if (match) {
                        subRecords.add(record);
                    }
                }

            }
        }
        return subRecords;
    }
}
