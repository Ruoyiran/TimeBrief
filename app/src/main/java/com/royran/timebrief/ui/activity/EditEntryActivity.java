package com.royran.timebrief.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.haibin.calendarview.CalendarViewDelegate;
import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.jzxiang.pickerview.listener.OnDateSetListener;
import com.orhanobut.logger.Logger;
import com.royran.timebrief.constants.Constants;
import com.royran.timebrief.models.RealmTimeRecord;
import com.royran.timebrief.models.TimeStatus;
import com.royran.timebrief.utils.RealmHelper;
import com.royran.timebrief.utils.StringUtils;

import com.royran.timebrief.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;

import static butterknife.OnTextChanged.Callback.AFTER_TEXT_CHANGED;

public class EditEntryActivity extends BaseActivity {
    @BindView(R.id.text_title)
    TextView mTextTitle;

    @BindView(R.id.text_date)
    TextView mTextDate;

    @BindView(R.id.edit_title)
    EditText mEditTextTitle;

    @BindView(R.id.edit_description)
    EditText mEditTextDescription;

    @BindView(R.id.text_start_time)
    TextView mStartTimeText;

    @BindView(R.id.text_end_time)
    TextView mEndTimeText;

    @BindView(R.id.text_total_time)
    TextView mTotalTimeText;

    @BindView(R.id.layout_of_text_start_time)
    LinearLayout mStartTimeLayout;

    @BindView(R.id.layout_of_text_end_time)
    LinearLayout mEndTimeLayout;

    @BindView(R.id.layout_of_text_total_time)
    LinearLayout mTotalTimeLayout;

    private RealmTimeRecord mTimeRecord;
    private RealmTimeRecord mOldTimeRecord;

    private boolean mDataHasChanged;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTextTitle.setText(getString(R.string.edit));
        mTimeRecord = (RealmTimeRecord) getIntent().getSerializableExtra(Constants.TIME_RECORD_EXTRA_STRING);
        if (mTimeRecord != null) {
            mOldTimeRecord = new RealmTimeRecord();
            mTimeRecord.copyTo(mOldTimeRecord);
            mTextDate.setText(getFormattedDate());
            mEditTextTitle.setText(mTimeRecord.getTitle());
            mEditTextDescription.setText(mTimeRecord.getSummary());
            if (mTimeRecord.getTimeStatus() == TimeStatus.FINISHED_STATUS) {
                mStartTimeLayout.setVisibility(View.VISIBLE);
                mEndTimeLayout.setVisibility(View.VISIBLE);
                mTotalTimeLayout.setVisibility(View.VISIBLE);
                setStartTime(mTimeRecord.getStartTimeMillis());
                setEndTime(mTimeRecord.getEndTimeMillis());
                updateTotalTime();
            } else {
                mStartTimeLayout.setVisibility(View.GONE);
                mEndTimeLayout.setVisibility(View.GONE);
                mTotalTimeLayout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_edit_entry;
    }

    @OnClick(R.id.image_back)
    protected void onBackClicked() {
        finish();
    }

    @OnTextChanged(value = R.id.edit_title, callback = AFTER_TEXT_CHANGED)
    void onEditTitleChanged(Editable s) {
        String title = s.toString();
        if (TextUtils.isEmpty(title)) {
            return;
        }
        mTimeRecord.setTitle(title);
//        boolean ok = RealmHelper.updateTimeRecord(mTimeRecord);
//        if (!ok) {
//            Logger.e("updateTimeRecord failed");
//        } else {
//            Logger.d("updateTimeRecord success");
//        }
    }

    @OnTextChanged(value = R.id.edit_description, callback = AFTER_TEXT_CHANGED)
    void onEditDescriptionChanged(Editable s) {
        mTimeRecord.setSummary(s.toString());
//        boolean ok = RealmHelper.updateTimeRecord(mTimeRecord);
//        if (!ok) {
//            Logger.e("updateTimeRecord failed");
//        } else {
//            Logger.d("updateTimeRecord success");
//            mDataHasChanged = true;
//        }
    }

    @OnClick(R.id.layout_of_text_date)
    void onTextDateClicked() {
        DateEditActivity.open(this, mTimeRecord, Constants.OPEN_DATE_EDIT_ACTIVITY_REQUEST_CODE);
    }

    @OnClick(R.id.layout_of_text_start_time)
    void onStartTimeClicked() {
        showFullDateTimeDialog(mTimeRecord.getStartTimeMillis(), onStartTimeSelectedListener);
    }

    @OnClick(R.id.layout_of_text_end_time)
    void onEndTimeClicked() {
        showFullDateTimeDialog(mTimeRecord.getEndTimeMillis(), onEndTimeSelectedListener);
    }

    @OnClick(R.id.layout_of_text_total_time)
    void onTotalTimeClicked() {
        showHourMinuteTimeDialog(onTimeTimeSelectedListener);
    }

    private long getBeginTimeOfOneDay(long timeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        return calendar.getTimeInMillis();
    }

    private long getTimeInMillis(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        return calendar.getTimeInMillis();
    }

    private void showHourMinuteTimeDialog(OnDateSetListener listener) {
        long beginTimeOfOneDayMs = getBeginTimeOfOneDay(mTimeRecord.getStartTimeMillis());
        long totalTimeMs = mTimeRecord.getEndTimeMillis()-mTimeRecord.getStartTimeMillis();
        if (totalTimeMs < 0) totalTimeMs = 0;
        showCustomDialogTimePicker(beginTimeOfOneDayMs+mTimeRecord.getElapsedTimeMillis(), beginTimeOfOneDayMs, beginTimeOfOneDayMs + totalTimeMs, Type.HOURS_MINS, false, listener);
    }

    public void showFullDateTimeDialog(long currentTimeMs, OnDateSetListener listener) {
        long minYear = getTimeInMillis(2008,0,1);
        long maxYear = getTimeInMillis(2050,11,31);
        showCustomDialogTimePicker(currentTimeMs, minYear, maxYear, Type.ALL, true, listener);
    }

    public void showCustomDialogTimePicker(long currentTimeMs, long minMillis, long maxMills, Type type, boolean cyclic, OnDateSetListener listener) {
        TimePickerDialog mDialogAll = new TimePickerDialog.Builder()
                .setCallBack(listener)
                .setTitleStringId(getString(R.string.time))
                .setCancelStringId(getString(R.string.cancel))
                .setSureStringId(getString(R.string.ok))
                .setCyclic(cyclic)
                .setMinMillseconds(minMillis)
                .setMaxMillseconds(maxMills)
                .setCurrentMillseconds(currentTimeMs)
                .setThemeColor(getResources().getColor(R.color.red))
                .setType(type)
                .setWheelItemTextNormalColor(getResources().getColor(R.color.timetimepicker_default_text_color))
                .setWheelItemTextSelectorColor(getResources().getColor(R.color.timepicker_toolbar_bg))
                .setWheelItemTextSize(12)
                .build();
        mDialogAll.show(getSupportFragmentManager(), "");
    }

    private OnDateSetListener onStartTimeSelectedListener = (timePickerView, milliseconds) -> {
        if (milliseconds > mTimeRecord.getEndTimeMillis()) {
            Toast.makeText(EditEntryActivity.this, getString(R.string.start_time_error), Toast.LENGTH_SHORT).show();
            return;
        }
        long beforeStartTime = mTimeRecord.getStartTimeMillis();
        long offset = beforeStartTime - milliseconds;
        mTimeRecord.setStartTimeMillis(milliseconds);
        setStartTime(milliseconds);
        updateIncreasedTime(offset);
        updateTotalTime();
    };

    private OnDateSetListener onEndTimeSelectedListener = (timePickerView, milliseconds) -> {
        if (milliseconds < mTimeRecord.getStartTimeMillis()) {
            Toast.makeText(EditEntryActivity.this, getString(R.string.end_time_error), Toast.LENGTH_SHORT).show();
            return;
        }
        long beforeEndTime = mTimeRecord.getEndTimeMillis();
        long offset = milliseconds - beforeEndTime;
        mTimeRecord.setEndTimeMillis(milliseconds);
        setEndTime(milliseconds);
        updateIncreasedTime(offset);
        updateTotalTime();
    };

    private void updateIncreasedTime(long offset) {
        long totalTime = mTimeRecord.getIncreasedTimeMillis() + offset;
        if (totalTime < 0) totalTime = 0;
        mTimeRecord.setIncreasedTimeMillis(totalTime);
    }

    private OnDateSetListener onTimeTimeSelectedListener = (timePickerView, milliseconds) -> {
        long beginTimeOfOneDayMs = getBeginTimeOfOneDay(mTimeRecord.getStartTimeMillis());
        long totalTime = milliseconds - beginTimeOfOneDayMs;
        mTimeRecord.setIncreasedTimeMillis(totalTime);
        mTotalTimeText.setText(StringUtils.formatTotalTimeToString(mTimeRecord.getElapsedTimeMillis()));
    };

    private void updateTotalTime() {
        mTotalTimeText.setText(StringUtils.formatTotalTimeToString(mTimeRecord.getElapsedTimeMillis()));
    }

    private String getFormattedDate() {
        if (mTimeRecord == null) {
            return "";
        }
        Date d = new Date(mTimeRecord.getActivityDateMillis());
        com.haibin.calendarview.Calendar calendar = CalendarViewDelegate.createCalendar(d);
        StringBuilder sb = new StringBuilder();
        sb.append(calendar.getYear());
        sb.append("年");
        sb.append(calendar.getMonth());
        sb.append("月");
        sb.append(calendar.getDay());
        sb.append("日 ");
        sb.append(" ");
        sb.append(calendar.getLunar());
        Logger.d("getFormattedDate " + sb.toString());
        return sb.toString();
    }

    @Override
    public void onBackPressed() {
        Intent intent = EditEntryActivity.getBundleIntent(this, mTimeRecord);
        intent.putExtra(Constants.DATA_IS_CHANGED_EXTRA_STRING, mDataHasChanged);
        this.setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    @OnClick(R.id.img_edit_done)
    void onEditDoneClicked() {
        if (!mTimeRecord.equals(mOldTimeRecord)) {
            boolean ok = RealmHelper.updateTimeRecord(mTimeRecord);
            if (ok) {
                mDataHasChanged = true;
            } else {
                Logger.e("update record failed, id: %d", mTimeRecord.getId());
            }
        }
        onBackPressed();
    }

    public String getDateToString(long time) {
        Date d = new Date(time);
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sf.format(d);
    }

    private void setStartTime(long milliseconds) {
        mStartTimeText.setText(BaseActivity.getInstance().getString(R.string.start_time) + "： " + getDateToString(milliseconds));
    }

    private void setEndTime(long milliseconds) {
        mEndTimeText.setText(BaseActivity.getInstance().getString(R.string.end_time) + "： " + getDateToString(milliseconds));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.d("onActivityResult - requestCode: %d, resultCode: %d", requestCode, resultCode);
        if (resultCode == RESULT_OK && requestCode == Constants.OPEN_DATE_EDIT_ACTIVITY_REQUEST_CODE) {
            RealmTimeRecord record = (RealmTimeRecord) data.getSerializableExtra(Constants.TIME_RECORD_EXTRA_STRING);
            if (record != null && mTimeRecord.getActivityDateMillis() != record.getActivityDateMillis()) {
                Logger.d("onActivityResult title: %s", record.getTitle());
                mTimeRecord.setActivityDateMillis(record.getActivityDateMillis());
                mTextDate.setText(getFormattedDate());
            }
        }
    }

    private static Intent getBundleIntent(Context context, RealmTimeRecord record) {
        Intent intent = new Intent(context, EditEntryActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.TIME_RECORD_EXTRA_STRING, record);
        intent.putExtras(bundle);
        return intent;
    }

    public static void open(Activity activity, RealmTimeRecord record, int requestCode) {
        Intent intent = getBundleIntent(activity, record);
        activity.startActivityForResult(intent, requestCode);
    }
}
