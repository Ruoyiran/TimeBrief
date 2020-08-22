package com.royran.timebrief.ui.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.royran.timebrief.models.DateType;
import com.royran.timebrief.models.RealmTimeRecord;
import com.royran.timebrief.ui.activity.MainActivity;
import com.royran.timebrief.ui.activity.RecordDetailActivity;
import com.royran.timebrief.utils.StringUtils;
import com.royran.timebrief.utils.TimeRecordUtils;
import com.royran.timebrief.utils.TimeUtils;

import com.royran.timebrief.R;

import com.royran.timebrief.ui.views.chart.LineChartItem;

import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TimeRecordDetailRecyclerAdapter extends BaseRecyclerAdapter<List<RealmTimeRecord>> {
    private static final int ITEM_TYPE_LIST_ITEM = 0;
    private static final int ITEM_TYPE_LIST_CHART = 1;
    private RecyclerViewHolder mListItemViewHolder;
    private boolean mNeedRefreshChart = true;
    private List<RealmTimeRecord> mOrinalRecords;
    private RecordDetailActivity mActivity;


    public TimeRecordDetailRecyclerAdapter(RecordDetailActivity activity) {
        super(activity);
        mActivity = activity;
    }

    @Override
    public int getItemViewType(int position) {
        if (MainActivity.getInstance().getStatisticsFragment().getCurrentSelectedDateType() == DateType.ALL) {
            return ITEM_TYPE_LIST_ITEM;
        }
        if (position == 0) {
            return ITEM_TYPE_LIST_CHART;
        }
        return ITEM_TYPE_LIST_ITEM;
    }

    @Override
    public int getItemCount() {
        if (MainActivity.getInstance().getStatisticsFragment().getCurrentSelectedDateType() == DateType.ALL) {
            return mItems.size();
        } else {
            return mItems.size() + 1;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == ITEM_TYPE_LIST_CHART) {
            if  (mListItemViewHolder == null) {
                view = mInflater.inflate(R.layout.record_detail_line_chart, parent, false);
                mListItemViewHolder = new TimeRecordDetailRecyclerAdapter.RecyclerViewHolder(view, viewType);
            }
            return mListItemViewHolder;
        } else {
            view = mInflater.inflate(R.layout.item_record_detail, parent, false);
            return new TimeRecordDetailRecyclerAdapter.RecyclerViewHolder(view, viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RecyclerViewHolder viewHodler = (RecyclerViewHolder) holder;
        if (viewHodler.getItemViewType() == ITEM_TYPE_LIST_CHART) {
            holder.setIsRecyclable(false);
            setOnLineChartValueSelectedListener(viewHodler);
            if (mNeedRefreshChart) {
                viewHodler.updateChart(mOrinalRecords);
                mNeedRefreshChart = false;
            }
        } else {
            if (MainActivity.getInstance().getStatisticsFragment().getCurrentSelectedDateType() == DateType.ALL) {
                viewHodler.updateUI(mItems.get(position));
            } else {
                viewHodler.updateUI(mItems.get(position-1));
            }
        }
    }

    private void setOnLineChartValueSelectedListener(RecyclerViewHolder viewHodler) {
        viewHodler.mListChartItem.setOnLineChartValueSelectedListener(new LineChartItem.OnLineChartValueSelectdListener() {
            @Override
            public void onValueSelected(List<RealmTimeRecord> currRecords, List<RealmTimeRecord> prevRecords) {
                setDataList(currRecords, prevRecords);
            }

            @Override
            public void onNothingSelected() {
            }
        });
    }

    private String mTitle;
    private void setTextTitle(List<RealmTimeRecord> records, List<RealmTimeRecord> prevRecords) {
        if (records == null || records.size() == 0) {
            mActivity.setTitle(mTitle);
        } else {
            mTitle = records.get(0).getTitle();
            long prevTotalTimeMs = TimeRecordUtils.getTotalElapsedTime(prevRecords);
            long totalTimeMs = TimeRecordUtils.getTotalElapsedTime(records);
            String totalTimeText = StringUtils.formatTotalTimeToString(totalTimeMs);
            String title = mTitle + " (" + totalTimeText + ")";
            String comp = "";
            if (prevTotalTimeMs > 0) {
                if (totalTimeMs == 0) {
                    comp = "-100%";
                } else {
                    long diff = totalTimeMs - prevTotalTimeMs;
                    if (diff > 0) {
                        comp = String.format("+%.2f%%", diff * 100 / (float) prevTotalTimeMs);
                    } else {
                        comp = String.format("%.2f%%", diff * 100 / (float) prevTotalTimeMs);
                    }
                }
            }
            if (!comp.equals("")) {
                title += "  比上期: " + comp;
            }
            mActivity.setTitle(title);
        }
    }
    
    public void setDataList(List<RealmTimeRecord> records, List<RealmTimeRecord> prevRecords) {
        setTextTitle(records, prevRecords);
        mOrinalRecords = records;
        mItems.clear();
        if (records == null) {
            this.notifyDataSetChanged();
            return;
        }
        mItems = TimeRecordUtils.groupAndSortByEndTime(records, false);
        this.notifyDataSetChanged();
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.text_day_of_month)
        TextView mTextDayOfMonth;

        @BindView(R.id.text_year_month)
        TextView mTextYearMonth;

        @BindView(R.id.text_day_of_week)
        TextView mTextDayOfWeek;

        @BindView(R.id.recycler_view)
        RecyclerView mRecyclerView;

        TimeRecordListItemAdpter mRecyclerAdapter;

        LineChartItem mListChartItem;
        int mViewType;

        public RecyclerViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            mViewType = viewType;
            if (viewType == ITEM_TYPE_LIST_CHART) {
                mListChartItem = new LineChartItem(mActivity, itemView);
            } else {
                ButterKnife.bind(this, itemView);
                mRecyclerAdapter = new TimeRecordListItemAdpter(mActivity);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
                mRecyclerView.setAdapter(mRecyclerAdapter);
            }

        }

        public void updateUI(List<RealmTimeRecord> records) {
            if (records.isEmpty()) {
                mRecyclerAdapter.setDataList(null);
                return;
            }
            setDateText(records.get(0));
            mRecyclerAdapter.setDataList(records);
        }

        private void setDateText(RealmTimeRecord record) {
            Calendar calendar = TimeUtils.getChinaDate(record.getEndTimeMillis());
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            mTextDayOfMonth.setText(month + "月" + dayOfMonth + "日");
            mTextYearMonth.setText(year + "年");
            mTextDayOfWeek.setText(TimeUtils.getWeekString(calendar.getTimeInMillis()));
        }

        public void updateChart(List<RealmTimeRecord> records) {
            if (mListChartItem != null) {
                mListChartItem.setDataList(records);
            }
        }
    }

}
