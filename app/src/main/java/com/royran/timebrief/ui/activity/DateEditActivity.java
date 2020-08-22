package com.royran.timebrief.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.CalendarLayout;
import com.haibin.calendarview.CalendarView;
import com.haibin.calendarview.CalendarViewDelegate;
import com.orhanobut.logger.Logger;
import com.royran.timebrief.constants.Constants;
import com.royran.timebrief.models.RealmTimeRecord;

import com.royran.timebrief.R;

import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;

public class DateEditActivity extends BaseActivity {
    @BindView(R.id.calendar_view)
    CalendarView mCalendarView;

    @BindView(R.id.calendar_layout)
    CalendarLayout mCalendarLayout;

    @BindView(R.id.text_month_day)
    TextView mTextMonthDay;

    @BindView(R.id.text_year)
    TextView mTextYear;

    @BindView(R.id.text_lunar)
    TextView mTextLunar;

    private RealmTimeRecord mTimeRecord;
    private int mSelectedYear;

    @Override
    public int getLayoutId() {
        return R.layout.activity_date_edit;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTimeRecord = (RealmTimeRecord) getIntent().getSerializableExtra(Constants.TIME_RECORD_EXTRA_STRING);
        mCalendarLayout.setModeOnlyMonthView();
        setNowDate();
        initCalendarView();
        setMonthDayClickListener();
    }

    private void initCalendarView() {
        mCalendarView.setOnCalendarSelectListener(new CalendarSelectListenerImpl());
        mCalendarView.setOnYearChangeListener(new CalendarYearChangeListenerImpl());
        mSelectedYear = mCalendarView.getCurYear();
    }

    private void setMonthDayClickListener() {
        mTextMonthDay.setOnClickListener(v -> {
            if (!mCalendarLayout.isExpand()) {
                mCalendarLayout.expand();
                return;
            }
            mCalendarView.showYearSelectLayout(mSelectedYear);
            mTextLunar.setVisibility(View.GONE);
            mTextYear.setVisibility(View.GONE);
            mTextMonthDay.setText(String.valueOf(mSelectedYear));
        });
    }

    private void setNowDate() {
        if (mTimeRecord == null) {
            return;
        }
        Date d = new Date(mTimeRecord.getActivityDateMillis());
        mCalendarView.updateDate(d);
        com.haibin.calendarview.Calendar calendar = CalendarViewDelegate.createCalendar(d);
        mTextLunar.setText(calendar.getLunar());
        mTextYear.setText(String.valueOf(mCalendarView.getCurYear()));
        mTextMonthDay.setText(mCalendarView.getCurMonth() + "月" + mCalendarView.getCurDay() + "日");
    }

    @OnClick(R.id.image_ok)
    void onOkClicked() {
        Logger.d("onOk: %d", mTimeRecord.getActivityDateMillis());
        Intent intent = DateEditActivity.getBundleIntent(this, mTimeRecord);
        this.setResult(RESULT_OK, intent);
        finish();
    }

    private class CalendarSelectListenerImpl implements CalendarView.OnCalendarSelectListener {

        @SuppressLint("SetTextI18n")
        @Override
        public void onCalendarSelect(Calendar calendar, boolean isClick) {
            mTextLunar.setVisibility(View.VISIBLE);
            mTextYear.setVisibility(View.VISIBLE);
            mTimeRecord.setActivityDateMillis(calendar.getTimeInMillis());
            setNowDate();
        }

        @Override
        public void onCalendarOutOfRange(Calendar calendar) {
        }
    }

    private class CalendarYearChangeListenerImpl implements CalendarView.OnYearChangeListener {

        @Override
        public void onYearChange(int year) {
            mTextMonthDay.setText(String.valueOf(year));
        }
    }

    private static Intent getBundleIntent(Activity activity, RealmTimeRecord timeRecord) {
        Intent intent = new Intent(activity, DateEditActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.TIME_RECORD_EXTRA_STRING, timeRecord);
        intent.putExtras(bundle);
        return intent;
    }

    public static void open(Activity activity, RealmTimeRecord timeRecord, int requestCode) {
        Intent intent = getBundleIntent(activity, timeRecord);
        activity.startActivityForResult(intent, requestCode);
    }
}
