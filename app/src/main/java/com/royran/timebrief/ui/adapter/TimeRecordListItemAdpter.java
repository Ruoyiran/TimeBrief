package com.royran.timebrief.ui.adapter;

import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.royran.timebrief.constants.Constants;
import com.royran.timebrief.models.RealmTimeRecord;
import com.royran.timebrief.ui.activity.BaseActivity;
import com.royran.timebrief.ui.activity.EditEntryActivity;
import com.royran.timebrief.utils.RealmHelper;
import com.royran.timebrief.utils.StringUtils;

import com.royran.timebrief.R;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TimeRecordListItemAdpter extends BaseRecyclerAdapter<RealmTimeRecord> {

    private RecyclerViewHolder mHolder;
    private BaseActivity mBaseActivity;

    public TimeRecordListItemAdpter(BaseActivity activity) {
        super(activity);
        mBaseActivity = activity;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_record_card, parent, false);
        mHolder = new RecyclerViewHolder(view);
        return mHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RecyclerViewHolder viewHodler = (RecyclerViewHolder) holder;
        viewHodler.updateUI(mItems.get(position));
    }

    public void setDataList(List<RealmTimeRecord> records) {
        mItems.clear();
        if (records == null || records.isEmpty()) {
            this.notifyDataSetChanged();
            return;
        }
        mItems.addAll(records);
        Collections.sort(mItems, (records1, records2) -> sortByTime(records1, records2));
        this.notifyDataSetChanged();
    }

    private int sortByTime(RealmTimeRecord records1, RealmTimeRecord records2) {
        long time1 = records1.getEndTimeMillis();
        long time2 = records2.getEndTimeMillis();
        if (time2 > time1) {
            return 1;
        } else if (time2 < time1){
            return -1;
        } else {
            return 0;
        }
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.text_title)
        TextView mTextTitle;

        @BindView(R.id.text_start_time)
        TextView mTextStartTime;

        @BindView(R.id.text_end_time)
        TextView mTextEndTime;

        @BindView(R.id.text_total_time)
        TextView mTextTotalTime;

        @BindView(R.id.layout_summary)
        LinearLayout mLayoutSummary;

        @BindView(R.id.text_summary)
        TextView mTextSummary;

        private RealmTimeRecord mTimeRecord;

        RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v -> EditEntryActivity.open(mBaseActivity, mTimeRecord, Constants.OPEN_EDIT_ENTRY_ACTIVITY_REQUEST_CODE));
            itemView.setOnLongClickListener(v -> {
                        onItemDelete();
                        return true;
                    }
            );
        }

        void onItemDelete() {
            new MaterialDialog.Builder(mBaseActivity).
                    content(mBaseActivity.getString(R.string.delete_confirm)).
                    positiveText(mBaseActivity.getString(R.string.ok)).
                    negativeText(mBaseActivity.getString(R.string.cancel)).onPositive(
                    (dialog, which) -> {
                        int affectedRows = RealmHelper.deleteTimeRecordById(mTimeRecord.getId());
                        if (affectedRows > 0) {
                            Toast.makeText(mBaseActivity, mBaseActivity.getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
                            removeItem(mTimeRecord);
                            notifyDataSetChanged();
                        } else {
                            Toast.makeText(mBaseActivity, mBaseActivity.getString(R.string.delete_failed), Toast.LENGTH_SHORT).show();
                        }
                    }
            ).show();
        }

        String getTimeString(long timeMs) {
            return DateFormat.format("HH:mm", timeMs).toString();
        }

        private void updateUI(RealmTimeRecord record) {
            if (record == null) {
                return;
            }
            mTimeRecord = record;
            mTextTitle.setText(record.getTitle());
            mTextStartTime.setText(getTimeString(record.getStartTimeMillis()));
            mTextEndTime.setText(getTimeString(record.getEndTimeMillis()));
            mTextTotalTime.setText(StringUtils.formatTotalTimeToString(record.getElapsedTimeMillis()));
            mTextSummary.setText(record.getSummary());
            if (TextUtils.isEmpty(record.getSummary())) {
                mLayoutSummary.setVisibility(View.GONE);
            } else {
                mLayoutSummary.setVisibility(View.VISIBLE);
            }
        }
    }

}
