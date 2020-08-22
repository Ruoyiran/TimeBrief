package com.royran.timebrief.ui.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.orhanobut.logger.Logger;
import com.royran.timebrief.constants.Constants;
import com.royran.timebrief.models.RealmTimeRecord;
import com.royran.timebrief.models.RecordsSortType;
import com.royran.timebrief.ui.activity.BaseActivity;
import com.royran.timebrief.ui.activity.RecordDetailActivity;
import com.royran.timebrief.utils.StringUtils;
import com.royran.timebrief.utils.TimeUtils;

import com.royran.timebrief.R;

import com.royran.timebrief.ui.views.chart.BarChartItem;
import com.royran.timebrief.ui.views.chart.BarChartSortMethod;
import com.royran.timebrief.ui.views.chart.BarChartType;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TimeStatisticsRecyclerAdapter extends BaseRecyclerAdapter<List<RealmTimeRecord>> {
    private static final int ITEM_TYPE_LIST_ITEM = 0;
    private static final int ITEM_TYPE_BAR_CHART = 1;

    private long mTotalTimeMs;
    private BaseActivity mBaseActivity;
    private RecyclerViewHolder mChartItemViewHolder;

    private boolean mListSortAscend = false;
    private RecordsSortType mListSortType = RecordsSortType.Time;

    private List<RealmTimeRecord> mOrinalRecords;
    private List<List<RealmTimeRecord>> mDataRecords;
    private boolean mNeedRefreshChart;

    public TimeStatisticsRecyclerAdapter(BaseActivity activity) {
        super(activity);
        mBaseActivity = activity;
    }

    @Override
    public int getItemCount() {
        return mItems.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ITEM_TYPE_BAR_CHART;
        } else {
            return ITEM_TYPE_LIST_ITEM;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == ITEM_TYPE_BAR_CHART) {
            if  (mChartItemViewHolder == null) {
                view = mInflater.inflate(R.layout.statistics_barchart, parent, false);
                mChartItemViewHolder = new RecyclerViewHolder(view, viewType);
            }
            return mChartItemViewHolder;
        } else {
            view = mInflater.inflate(R.layout.item_time_record, parent, false);
            return new RecyclerViewHolder(view, viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RecyclerViewHolder viewHodler = (RecyclerViewHolder) holder;
        if (position == 0) {
            holder.setIsRecyclable(false);
            if (mNeedRefreshChart) {
                viewHodler.updateChart(mOrinalRecords);
                mNeedRefreshChart = false;
            }
        } else {
            viewHodler.updateList(mItems.get(position-1));
        }
    }

    public void setDataList(List<RealmTimeRecord> records) {
        mNeedRefreshChart = true;
        mOrinalRecords = records;
        mDataRecords = getGroupedDataRecords(records);
        refreshList(mDataRecords, mListSortType, mListSortAscend);
    }

    public BarChartSortMethod getChartSortMethod() {
        if (mChartItemViewHolder != null && mChartItemViewHolder.mChartItemView != null) {
            mChartItemViewHolder.mChartItemView.getChartSortMethod();
        }
        return BarChartSortMethod.Time;
    }

    public boolean isChartSortAscend() {
        if (mChartItemViewHolder != null && mChartItemViewHolder.mChartItemView != null) {
            return mChartItemViewHolder.mChartItemView.isChartSortAscend();
        }
        return true;
    }

    public void setChartType(BarChartType chartType, BarChartSortMethod chartSortMethod, boolean isAscend) {
        if (mChartItemViewHolder != null && mChartItemViewHolder.mChartItemView != null) {
            mChartItemViewHolder.mChartItemView.setChartType(chartType, chartSortMethod, isAscend);
        }
    }

    public BarChartType getChartType() {
        if (mChartItemViewHolder != null && mChartItemViewHolder.mChartItemView != null) {
            return mChartItemViewHolder.mChartItemView.getChartType();
        }
        return BarChartType.Times;
    }

    private long getTotalDays(List<RealmTimeRecord> records) {
        HashSet<Long> dates = new HashSet<>();
        for (RealmTimeRecord record : records) {
            dates.add((record.getEndTimeMillis()/1000+28800)/(60*60*24)); //  8*60*60s = 28800s = 8hours
        }
        return dates.size();
    }

    private long getTotalElapsedTime(List<RealmTimeRecord> records) {
        long total = 0;
        for (RealmTimeRecord record : records) {
            total += record.getElapsedTimeMillis();
        }
        return total;
    }

    private List<List<RealmTimeRecord>> getGroupedDataRecords(List<RealmTimeRecord> records) {
        if (records == null || records.size() == 0) {
            return null;
        }
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
        return new ArrayList<>(allGroupedRecords);
    }

    private void refreshList(List<List<RealmTimeRecord>> records, final RecordsSortType sortType, final boolean ascend) {
        mItems.clear();
        mTotalTimeMs = 0;
        if (records == null || records.isEmpty()) {
            this.notifyDataSetChanged();
            return;
        }
        Map<String, List<RealmTimeRecord>> mappedRecords = new HashMap<>();
        for (List<RealmTimeRecord> sub_records : records) {
            for (RealmTimeRecord record :  sub_records) {
                if (!mappedRecords.containsKey(record.getTitle())) {
                    mappedRecords.put(record.getTitle(), new ArrayList<>());
                }
                mappedRecords.get(record.getTitle()).add(record);
                mTotalTimeMs += record.getElapsedTimeMillis();
            }
        }
        for (Map.Entry<String, List<RealmTimeRecord>> entry: mappedRecords.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty())
                continue;
            Logger.d("key: %s, size: %d", entry.getKey(), entry.getValue().size());
            mItems.add(entry.getValue());
        }
        Collections.sort(mItems, (records1, records2) -> {
            if (sortType == RecordsSortType.Time) {
                return sortByTimes(records1, records2, ascend);
            } else if (sortType == RecordsSortType.Days) {
                return sortByDays(records1, records2, ascend);
            } else if (sortType == RecordsSortType.Count) {
                return sortByCount(records1, records2, ascend);
            } else {
                return 0;
            }

        });
        this.notifyDataSetChanged();
    }

    private int sortByCount(List<RealmTimeRecord> records1, List<RealmTimeRecord> records2, boolean ascend) {
        if (ascend) {
            if (records2.size() > records1.size()) {
                return -1;
            } else if (records2.size() < records1.size()) {
                return 1;
            }
        } else {
            if (records2.size() > records1.size()) {
                return 1;
            } else if (records2.size() < records1.size()) {
                return -1;
            }
        }

        return 0;
    }

    private int sortByDays(List<RealmTimeRecord> records1, List<RealmTimeRecord> records2, boolean ascend) {
        long totalDays1 = getTotalDays(records1);
        long totalDays2 = getTotalDays(records2);
        if (ascend) {
            if (totalDays2 > totalDays1) {
                return -1;
            } else if (totalDays2 < totalDays1){
                return 1;
            } else {
                return 0;
            }
        } else {
            if (totalDays2 > totalDays1) {
                return 1;
            } else if (totalDays2 < totalDays1){
                return -1;
            } else {
                return 0;
            }
        }
    }

    private int sortByTimes(List<RealmTimeRecord> records1, List<RealmTimeRecord> records2, boolean ascend) {
        long totalTime1 = getTotalElapsedTime(records1);
        long totalTime2 = getTotalElapsedTime(records2);
        if (ascend) {
            if (totalTime2 > totalTime1) {
                return -1;
            } else if (totalTime2 < totalTime1){
                return 1;
            } else {
                return 0;
            }
        } else {
            if (totalTime2 > totalTime1) {
                return 1;
            } else if (totalTime2 < totalTime1){
                return -1;
            } else {
                return 0;
            }
        }
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder implements BarChartItem.OnChartItemSelectedListener {
        private ListItem mListItemView;
        private BarChartItem mChartItemView;

        RecyclerViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            if (viewType == ITEM_TYPE_LIST_ITEM) {
                mListItemView = new ListItem(itemView);
            } else {
                mChartItemView = new BarChartItem(mBaseActivity, itemView);
                mChartItemView.setOnChartItemSelectedListener(this);
            }
        }

        private void updateChart(List<RealmTimeRecord> records) {
            if (mChartItemView != null) {
                mChartItemView.updateChart(records);
            }
        }

        private void updateList(List<RealmTimeRecord> records) {
            if (mListItemView != null) {
                mListItemView.refreshUI(records, mTotalTimeMs);
            }
        }

        @Override
        public void onChartItemSelected(List<List<RealmTimeRecord>> records) {
            refreshList(records, mListSortType, mListSortAscend);
        }

        @Override
        public void onChartItemNothingSelected() {
            refreshList(mDataRecords, mListSortType, mListSortAscend);
        }
    }

    class ListItem {
        @BindView(R.id.text_title)
        TextView textTitle;

        @BindView(R.id.text_total_time)
        TextView textTotalTime;

        @BindView(R.id.text_time_percent)
        TextView textTimePercent;

        @BindView(R.id.text_record_days)
        TextView textRecordDays;

        @BindView(R.id.text_record_times)
        TextView textRecordTimes;

        @BindView(R.id.text_avg_per_day)
        TextView textAvgPerDay;

        @BindView(R.id.text_avg_per_times)
        TextView textAvgPerTimes;

        @BindView(R.id.progress_bar)
        ProgressBar progressBar;

        private ArrayList<RealmTimeRecord> mRecords;

        ListItem(View view) {
            ButterKnife.bind(this, view);
            mRecords = new ArrayList<>();
            view.setOnClickListener(v ->
                    openNewView(mRecords)
            );
        }

        private void openNewView(ArrayList<RealmTimeRecord> records) {
            if (records.isEmpty()) {
                return;
            }
            RecordDetailActivity.open(mBaseActivity, records, Constants.OPEN_RECORD_DETAIL_ACTIVITY_REQUEST_CODE);
        }

        void refreshUI(List<RealmTimeRecord> records, double totalTimeMs) {
            if (records == null || records.isEmpty()) {
                return;
            }
            mRecords.clear();
            HashMap<Long, ArrayList<RealmTimeRecord>> mappedRecords = new HashMap<>();
            Set<String> allDates = new HashSet<>();
            long totalTime = 0;
            for (RealmTimeRecord record : records) {
                totalTime += record.getElapsedTimeMillis();
                long date = TimeUtils.getChinaDate(record.getActivityDateMillis()).getTimeInMillis();
                ArrayList<RealmTimeRecord> subRecords = mappedRecords.get(date);
                if (subRecords == null) {
                    subRecords = new ArrayList<>();
                    mappedRecords.put(date, subRecords);
                }
                mRecords.add(record);
                allDates.add(TimeUtils.getChinaDateString(record.getEndTimeMillis()));
                subRecords.add(record);
            }
            double ratio = 0.0f;
            if (totalTimeMs > 0) {
                ratio = (double) totalTime / totalTimeMs;
                ratio *= 100;
            }
            DecimalFormat df = new DecimalFormat("######0.00");

            textTitle.setText(records.get(0).getTitle());
            textTotalTime.setText(StringUtils.formatTotalTimeToString(totalTime));
            textTimePercent.setText(df.format(ratio) + "%");
            textRecordDays.setText(allDates.size() + "天");
            textRecordTimes.setText(records.size() + "次");
            textAvgPerDay.setText(StringUtils.formatTotalTimeToString(totalTime / allDates.size()) + "/天");
            textAvgPerTimes.setText(StringUtils.formatTotalTimeToString(totalTime / records.size()) + "/次");
//            textRecordCount.setText(records.size() + "次 平均: " + StringUtils.formatTotalTimeToString(totalTime/records.size()));
            progressBar.setProgress((int) ratio);
        }
    }

    public void setListSortType(RecordsSortType sortType, boolean ascend) {
        mListSortType = sortType;
        mListSortAscend = ascend;
        refreshList(mDataRecords, mListSortType, mListSortAscend);
    }

    public RecordsSortType getListSortType() {
        return mListSortType;
    }

    public boolean isListSortAscend() {
        return mListSortAscend;
    }


}
