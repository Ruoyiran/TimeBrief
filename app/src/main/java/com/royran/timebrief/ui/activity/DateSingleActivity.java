package com.royran.timebrief.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.CalendarLayout;
import com.haibin.calendarview.CalendarView;
import com.royran.timebrief.utils.DateHelper;

import com.royran.timebrief.R;

import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;

public class DateSingleActivity extends BaseActivity implements
        CalendarView.OnCalendarSelectListener, CalendarView.OnYearChangeListener {

    @BindView(R.id.tv_month_day)
    TextView mTextMonthDay;

    @BindView(R.id.tv_year)
    TextView mTextYear;

    @BindView(R.id.tv_lunar)
    TextView mTextLunar;

    @BindView(R.id.tv_current_day)
    TextView mTextCurrentDay;

    @BindView(R.id.calendarView)
    CalendarView mCalendarView;

    @BindView(R.id.rl_tool)
    RelativeLayout mRelativeTool;

    @BindView(R.id.text_selected_date)
    TextView mTextSelectedDate;

    @BindView(R.id.calendarLayout)
    CalendarLayout mCalendarLayout;

    Calendar mSelectedCalendar;

    private int mYear;
    private Date mTodayDate;
    private static OnDateSingleSelectFinishedListener mOnDateSingleSelectFinishedListener;
    private static long mSelectTimeInMillis;

    @Override
    public void onYearChange(int year) {
        mTextMonthDay.setText(String.valueOf(year));
    }

    public interface OnDateSingleSelectFinishedListener {
        void onSelected(Calendar selectedCalendar);
    }

    public static void show(Context context, long selectTimeInMillis, OnDateSingleSelectFinishedListener listener) {
        mOnDateSingleSelectFinishedListener = listener;
        mSelectTimeInMillis = selectTimeInMillis;
        context.startActivity(new Intent(context, DateSingleActivity.class));
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_date_single;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    @SuppressLint("SetTextI18n")
    protected void initView() {
        mTodayDate = new Date(mCalendarView.getCurCalendar().getTimeInMillis());
        mTextCurrentDay.setText(String.valueOf(mCalendarView.getCurDay()));
        if (mSelectTimeInMillis > 0) {
            mCalendarView.updateDate(new Date(mSelectTimeInMillis));
        }
        mYear = mCalendarView.getCurYear();
        mCalendarView.setOnYearChangeListener(this);
        mCalendarView.setOnCalendarSelectListener(this);
        mCalendarView.scrollToCurrent();
    }

    @OnClick(R.id.fl_current)
    void onCurrentDateClicked() {
        mCalendarView.updateDate(mTodayDate);
        mCalendarView.scrollToCurrent();
    }

    @OnClick(R.id.tv_month_day)
    void onTextMonthDayClicked() {
        if (mYear == 0) {
            mYear = mCalendarView.getCurYear();
        }
        if (!mCalendarLayout.isExpand()) {
            mCalendarLayout.expand();
            return;
        }
        mCalendarView.showYearSelectLayout(mYear);
        mTextLunar.setVisibility(View.GONE);
        mTextYear.setVisibility(View.GONE);
        mTextMonthDay.setText(String.valueOf(mYear));
    }

    @OnClick(R.id.iv_clear_date)
    void onClearDateClicked() {
        mCalendarView.clearSingleSelect();
        mTextSelectedDate.setText("未选择日期");
        mSelectedCalendar = null;
    }

    @OnClick(R.id.image_home)
    void onBackClicked() {
        finish();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onCalendarSelect(Calendar calendar, boolean isClick) {
        mTextMonthDay.setText(calendar.getMonth() + "月" + calendar.getDay() + "日");
        mTextYear.setText(String.valueOf(calendar.getYear()));
        mTextLunar.setText(calendar.getLunar());
        mYear = calendar.getYear();
        mSelectedCalendar = calendar;
        setSelectedDateText();
    }

    @Override
    public void onCalendarOutOfRange(Calendar calendar) {
    }

    void setSelectedDateText() {
        if (mSelectedCalendar == null) {
            mTextSelectedDate.setText("未选择日期");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(mSelectedCalendar.getYear()).append("年");
            sb.append(mSelectedCalendar.getMonth()).append("月");
            sb.append(mSelectedCalendar.getDay()).append("日 ");
            sb.append(DateHelper.getWeekString(this, mSelectedCalendar.getWeek()));
            mTextSelectedDate.setText(sb.toString());
        }

    }

    @OnClick(R.id.text_ok)
    void onOkClicked() {
        if (mOnDateSingleSelectFinishedListener != null) {
            mOnDateSingleSelectFinishedListener.onSelected(mSelectedCalendar);
            finish();
        }
    }
}
