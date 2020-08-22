package com.royran.timebrief.ui.adapter;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.haibin.calendarview.Calendar;
import com.orhanobut.logger.Logger;
import com.royran.timebrief.constants.Constants;
import com.royran.timebrief.models.RealmTimeRecord;
import com.royran.timebrief.models.TimeStatus;
import com.royran.timebrief.ui.activity.BaseActivity;
import com.royran.timebrief.ui.activity.EditEntryActivity;
import com.royran.timebrief.utils.CustomClock;
import com.royran.timebrief.utils.RealmHelper;
import com.royran.timebrief.utils.StringUtils;

import com.royran.timebrief.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class MainRecyclerAdapter extends GroupRecyclerAdapter<String, RealmTimeRecord> implements BaseActivity.OnActivityResultListener {
    private BaseActivity mActivity;
    private List<RealmTimeRecord> mTimeRecords;
    private final static int COUNT_TIME_INTERVAL_MILLIS = 1000;
    private HashMap<Integer, TimeRecordViewHodler> mAllViewHolders;
    private Calendar mCurrentSelectedCalendar;

    public MainRecyclerAdapter(BaseActivity activity) {
        super(activity);
        mActivity = activity;
        mAllViewHolders = new HashMap<>();
        mActivity.addOnActivityResultListener(this);
    }

    public MainRecyclerAdapter setDataList(List<RealmTimeRecord> records, Calendar currentSelectedDate) {
        mTimeRecords = records;
        mCurrentSelectedCalendar = currentSelectedDate;
        LinkedHashMap<String, List<RealmTimeRecord>> map = new LinkedHashMap<>();
        List<String> titles = new ArrayList<>();
        titles.add(getFormattedDate(mCurrentSelectedCalendar));
        map.put(titles.get(0), records);
        resetGroups(map,titles);
        return this;
    }

    private String getFormattedDate(Calendar calendar) {
        StringBuilder sb = new StringBuilder();
        sb.append(calendar.getMonth());
        sb.append("月");
        sb.append(calendar.getDay());
        sb.append("日 ");
        sb.append(calendar.getLunar());
        Logger.d("getFormattedDate " + sb.toString());
        return sb.toString();
    }

    public RealmTimeRecord getTimeRecord(int position) {
        if (mTimeRecords == null) {
            return null;
        }
        if (position < 0 || position >= mTimeRecords.size()) {
            return null;
        }
        return mTimeRecords.get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TimeRecordViewHodler(mInflater.inflate(R.layout.item_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Logger.d("MainRecyclerAdapter:onBindViewHolder - position: %d", position);
        TimeRecordViewHodler cardViewHolder = null;
        if (holder instanceof  TimeRecordViewHodler){
            cardViewHolder = (TimeRecordViewHodler)holder;
        }
        if (cardViewHolder == null) {
            return;
        }
        mAllViewHolders.put(position, cardViewHolder);
        cardViewHolder.setItemPosition(position);
        cardViewHolder.setTimeRecordItem(getTimeRecord(position));
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        TimeRecordViewHodler cardViewHolder = (TimeRecordViewHodler)holder;
        cardViewHolder.stopTimer();
        Logger.d("MainRecyclerAdapter:onViewRecycled - position: %d", cardViewHolder.getItemPosition());
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        TimeRecordViewHodler cardViewHolder = (TimeRecordViewHodler)holder;
        cardViewHolder.setTimer();
        Logger.d("MainRecyclerAdapter:onViewAttachedToWindow - position: %d", cardViewHolder.getItemPosition());
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        TimeRecordViewHodler cardViewHolder = (TimeRecordViewHodler)holder;
        cardViewHolder.stopTimer();
        Logger.d("MainRecyclerAdapter:onViewDetachedFromWindow - position: %d", cardViewHolder.getItemPosition());
    }

    public void clearAllTimers() {
        for (TimeRecordViewHodler holder : mAllViewHolders.values()) {
            holder.stopTimer();
        }
    }

    public void setCurrentSelectedCalendar(Calendar calendar) {
        this.mCurrentSelectedCalendar = calendar;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.d("onActivityResult - requestCode: %d, resultCode: %d", requestCode, resultCode);
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.OPEN_EDIT_ENTRY_ACTIVITY_REQUEST_CODE) {
            boolean isChanged = data.getBooleanExtra(Constants.DATA_IS_CHANGED_EXTRA_STRING, false);
            Logger.d("onActivityResult isChanged: %s", isChanged);
            if (isChanged) {
                setDataList(RealmHelper.queryTimeRecordsByDate(new Date(mCurrentSelectedCalendar.getTimeInMillis())), mCurrentSelectedCalendar);
                notifyDataSetChanged();
            }
        }
    }

    class TimeRecordViewHodler extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTitle;
        private TextView mSubTitle;
        private TextView mBeginTime;
        private TextView mEndTime;
        private TextView mSummary;
        private ImageView mImageStart;
        private ImageView mImagePause;
        private ImageView mImageDone;
        private LinearLayout mTimeRoot;
        private LinearLayout mImageRoot;
        private RealmTimeRecord mTimeRecord;
        private CustomClock mTimer;
        private int mPosition;

        public RealmTimeRecord getTimeRecord() {
            return mTimeRecord;
        }

        TimeRecordViewHodler(View parentView) {
            super(parentView);
            initView(parentView);
        }

        private void initView(View parentView) {
            mTitle = parentView.findViewById(R.id.text_title);
            mSubTitle = parentView.findViewById(R.id.text_sub_title);
            mBeginTime = parentView.findViewById(R.id.text_start_time);
            mEndTime = parentView.findViewById(R.id.text_end_time);
            mSummary = parentView.findViewById(R.id.text_summary);
            mImageStart = parentView.findViewById(R.id.image_start);
            mImagePause = parentView.findViewById(R.id.image_pause);
            mImageDone = parentView.findViewById(R.id.image_done);
            mTimeRoot = parentView.findViewById(R.id.layout_time_root);
            mImageRoot = parentView.findViewById(R.id.layout_image_root);
            mImageStart.setOnClickListener(this);
            mImagePause.setOnClickListener(this);
            mImageDone.setOnClickListener(this);
            setTitle("");
            itemView.setOnClickListener(v -> EditEntryActivity.open(mActivity, mTimeRecord, Constants.OPEN_EDIT_ENTRY_ACTIVITY_REQUEST_CODE));
            itemView.setOnLongClickListener(v -> {
                        onItemDelete();
                        return true;
                    }
            );
        }

        void setTitle(String title) {
            if (mTitle == null) {
                return;
            }
            mTitle.setText(title);
        }

        void setSubTitle() {
            if (mSubTitle == null) {
                return;
            }
            int timeStatus = mTimeRecord.getTimeStatus();
            String subTitle = null;
            switch (timeStatus) {
                case TimeStatus.FINISHED_STATUS:
                    subTitle = StringUtils.formatTotalTimeToString(mTimeRecord.getElapsedTimeMillis());
                    break;
                case TimeStatus.RUNNING_STATUS:
                case TimeStatus.PAUSED_STATUS:
                    subTitle = formatTimeString(mTimeRecord.getElapsedTimeMillis());
                    break;
                default:
                    break;
            }
            mSubTitle.setText(subTitle);
            if (TextUtils.isEmpty(subTitle)) {
                mSubTitle.setVisibility(View.GONE);
            } else {
                mSubTitle.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.image_start:
                    onTimeStart();
                    break;
                case R.id.image_pause:
                    onTimePause();
                    break;
                case R.id.image_done:
                    onTimeDone();
                    break;
                default:
                    break;
            }
        }

        void onTimeStart() {
            if (mTimeRecord == null) {
                return;
            }
            mTimeRecord.setTimeStatus(TimeStatus.RUNNING_STATUS);
            refreshUI();
            RealmHelper.updateTimeRecord(mTimeRecord);
        }

        private void onTimePause() {
            if (mTimeRecord == null) {
                return;
            }
            mTimeRecord.setTimeStatus(TimeStatus.PAUSED_STATUS);
            refreshUI();
            RealmHelper.updateTimeRecord(mTimeRecord);
        }

        void onTimeDone() {
            if (mTimeRecord == null) {
                return;
            }
            mTimeRecord.setTimeStatus(TimeStatus.FINISHED_STATUS);
            Collections.sort(mTimeRecords);
            notifyDataSetChanged();
            RealmHelper.updateTimeRecord(mTimeRecord);
        }

        void onItemDelete() {
            new MaterialDialog.Builder(mActivity).
                content(mActivity.getString(R.string.delete_confirm)).
                positiveText(mActivity.getString(R.string.ok)).
                negativeText(mActivity.getString(R.string.cancel)).onPositive(
                (dialog, which) -> {
                    stopTimer();
                    int affectedRows = RealmHelper.deleteTimeRecordById(mTimeRecord.getId());
                    if (affectedRows > 0) {
                        Toast.makeText(mActivity, mActivity.getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
                        setDataList(RealmHelper.queryTimeRecordsByDate(new Date(mCurrentSelectedCalendar.getTimeInMillis())), mCurrentSelectedCalendar);
                        notifyDataSetChanged();
                    } else {
                        Toast.makeText(mActivity, mActivity.getString(R.string.delete_failed), Toast.LENGTH_SHORT).show();
                    }
                }
            ).show();
        }

        private void setTimer() {
            stopTimer();
            if (mTimeRecord.getTimeStatus() != TimeStatus.RUNNING_STATUS) {
                return;
            }
            if (mTimer == null) {
                mTimer = new CustomClock(COUNT_TIME_INTERVAL_MILLIS){
                    @Override
                    public void onTick(long millisIncreased) {
                        setSubTitle();
                    }
                };
            }
            Logger.d("setTimer - position: %d", mPosition);
            mTimer.start();
        }

        private void stopTimer() {
            if (mTimer != null) {
                Logger.d("stopTimer - position: %d", mPosition);
                mTimer.cancel();
            }
        }

        private String formatTimeString(long totalMilliseconds) {
            int hour = (int) (totalMilliseconds / 3600000L);
            int min = (int) (totalMilliseconds / 60000L % 60L);
            int sec = (int) (totalMilliseconds / 1000L % 60L);
            DecimalFormat format = new DecimalFormat("00");
            StringBuilder sb = new StringBuilder();
            sb.append(format.format(hour));
            sb.append(":");
            sb.append(format.format(min));
            sb.append(":");
            sb.append(format.format(sec));
            return sb.toString();
        }

        private void setImagesVisible() {
            int timeStatus = mTimeRecord.getTimeStatus();
            Logger.d("setImagesVisible - timeStatus: %d", timeStatus);
            switch (timeStatus) {
                case TimeStatus.IDLE_STATUS:
                    mImageStart.setVisibility(View.VISIBLE);
                    mImagePause.setVisibility(View.GONE);
                    mImageDone.setVisibility(View.GONE);
                    break;
                case TimeStatus.RUNNING_STATUS:
                    mImageStart.setVisibility(View.GONE);
                    mImagePause.setVisibility(View.VISIBLE);
                    mImageDone.setVisibility(View.VISIBLE);
                    break;
                case TimeStatus.PAUSED_STATUS:
                    mImageStart.setVisibility(View.VISIBLE);
                    mImagePause.setVisibility(View.GONE);
                    mImageDone.setVisibility(View.VISIBLE);
                    break;
                case TimeStatus.FINISHED_STATUS:
                    mImageStart.setVisibility(View.GONE);
                    mImagePause.setVisibility(View.GONE);
                    mImageDone.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }

        void setTimeRecordItem(RealmTimeRecord record) {
            if (record == null) {
                return;
            }
            mTimeRecord = record;
            refreshUI();
        }

        void setItemPosition(int position) {
            mPosition = position;
        }

        int getItemPosition() {
            return mPosition;
        }

        private void refreshUI() {
            setTitle(mTimeRecord.getTitle());
            setSubTitle();
            setImagesVisible();
            setTimer();
            setTimeStatus();
            setSummary();
        }

        private void setSummary() {
            if (TextUtils.isEmpty(mTimeRecord.getSummary())) {
                mSummary.setVisibility(View.GONE);
            } else {
                mSummary.setText(mTimeRecord.getSummary());
                mSummary.setVisibility(View.VISIBLE);
            }
        }

        String getFullTimeString(long timeMs) {
            return DateFormat.format("yyyy.MM.dd HH:mm:ss", timeMs).toString();
        }

        private void setTimeStatus() {
            mBeginTime.setText(BaseActivity.getInstance().getString(R.string.start_time) + ": " + getFullTimeString(mTimeRecord.getStartTimeMillis()));
            mEndTime.setText(BaseActivity.getInstance().getString(R.string.end_time) + ": " + getFullTimeString(mTimeRecord.getEndTimeMillis()));
            if (mTimeRecord.getTimeStatus() == TimeStatus.FINISHED_STATUS) {
                mTimeRoot.setVisibility(View.VISIBLE);
                mImageRoot.setVisibility(View.GONE);
            } else {
                mTimeRoot.setVisibility(View.GONE);
                mImageRoot.setVisibility(View.VISIBLE);
            }
        }
    }
}
