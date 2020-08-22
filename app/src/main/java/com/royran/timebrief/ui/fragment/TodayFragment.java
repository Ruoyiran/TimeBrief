package com.royran.timebrief.ui.fragment;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.CalendarLayout;
import com.haibin.calendarview.CalendarView;
import com.orhanobut.logger.Logger;
import com.royran.timebrief.events.DataLoadedEvent;
import com.royran.timebrief.models.RealmTimeRecord;
import com.royran.timebrief.utils.RealmHelper;
import com.royran.timebrief.utils.TimeUtils;

import com.royran.timebrief.R;

import com.royran.timebrief.ui.activity.BaseActivity;
import com.royran.timebrief.ui.adapter.GroupItemDecoration;
import com.royran.timebrief.ui.adapter.MainRecyclerAdapter;
import com.royran.timebrief.ui.views.EditTextBottomDialog;
import com.royran.timebrief.ui.views.GroupRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class TodayFragment extends BaseFragment {

    @BindView(R.id.recycler_main)
    GroupRecyclerView mRecyclerView;

    @BindView(R.id.calendar_layout)
    CalendarLayout mCalendarLayout;

    @BindView(R.id.calendar_view)
    CalendarView mCalendarView;

    @BindView(R.id.fl_current)
    FrameLayout mCurrentCalendar;

    @BindView(R.id.tv_current_day)
    TextView mTextCurrentDay;

    @BindView(R.id.text_month_day)
    TextView mTextMonthDay;

    @BindView(R.id.text_year)
    TextView mTextYear;

    @BindView(R.id.text_lunar)
    TextView mTextLunar;

    private MainRecyclerAdapter mRecyclerAdapter;

    private int mSelectedYear;

    public static TodayFragment newInstance() {
        return new TodayFragment();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.fragment_today;
    }

    @Override
    protected void onCreateView() {
        Logger.d("TodayFragment::onCreateView");
        setNowDate();
        initRecycleView();
        initCalendarView();
        setMonthDayClickListener();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDataLoaded(DataLoadedEvent event) {
        updateDataAndRefreshUI();
    }

    @Override
    public void onEnable() {
        Logger.d("TodayFragment::onEnable");
        updateDataAndRefreshUI();
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

    private void initRecycleView() {
        mRecyclerAdapter = new MainRecyclerAdapter((BaseActivity) getActivity());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.addItemDecoration(new GroupItemDecoration<String, RealmTimeRecord>());
        updateDataAndRefreshUI();
    }

    private void initCalendarView() {
        mSelectedYear = mCalendarView.getCurYear();
        mCalendarView.setOnCalendarSelectListener(new CalendarSelectListenerImpl());
        mCalendarView.setOnYearChangeListener(new CalendarYearChangeListenerImpl());
        mCurrentCalendar.setOnClickListener(v -> mCalendarView.scrollToCurrent());
        mTextCurrentDay.setText(String.valueOf(mCalendarView.getCurDay()));
        selectCalendar(System.currentTimeMillis());
    }

    public void selectCalendar(long timeMs) {
        java.util.Calendar calendar = TimeUtils.getChinaDate(timeMs);
        int year = calendar.get(java.util.Calendar.YEAR);
        int month = calendar.get(java.util.Calendar.MONTH) + 1;
        int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);
        mCalendarView.scrollToCalendar(year, month,day);
    }

    @Override
    public void onDestroy() {
        Logger.d("TodayFragment:onDestroy");
        mRecyclerAdapter.clearAllTimers();
        super.onDestroy();
    }

    private void setNowDate() {
        mTextLunar.setText("今天");
        mTextYear.setText(String.valueOf(mCalendarView.getCurYear()));
        mTextMonthDay.setText(mCalendarView.getCurMonth() + "月" + mCalendarView.getCurDay() + "日");
    }

    public void updateDataAndRefreshUI() {
        List<RealmTimeRecord> records = RealmHelper.queryTimeRecordsByDate(new Date(mCalendarView.getSelectedCalendar().getTimeInMillis()));
        mRecyclerAdapter.setDataList(records, mCalendarView.getSelectedCalendar());
        mRecyclerView.notifyDataSetChanged();
    }

    @OnClick(R.id.image_time_add)
    public void onTimeAddButtonClicked() {
        EditTextBottomDialog.show(getActivity(), text -> {
            if (text.isEmpty()) {
                return;
            }
            RealmTimeRecord record = new RealmTimeRecord();
            record.setTitle(text);
            record.setActivityDateMillis(mCalendarView.getSelectedCalendar().getTimeInMillis());
            RealmHelper.addTimeRecord(record);
            updateDataAndRefreshUI();
        });
    }

    private class CalendarYearChangeListenerImpl implements CalendarView.OnYearChangeListener {
        @Override
        public void onYearChange(int year) {
            mTextMonthDay.setText(String.valueOf(year));
        }
    }

    private class CalendarSelectListenerImpl implements CalendarView.OnCalendarSelectListener {

        @SuppressLint("SetTextI18n")
        @Override
        public void onCalendarSelect(Calendar calendar, boolean isClick) {
            mTextLunar.setVisibility(View.VISIBLE);
            mTextYear.setVisibility(View.VISIBLE);
            mTextMonthDay.setText(calendar.getMonth() + "月" + calendar.getDay() + "日");
            mTextYear.setText(String.valueOf(calendar.getYear()));
            mTextLunar.setText(calendar.getLunar());
            mSelectedYear = calendar.getYear();
            updateDataAndRefreshUI();
            mRecyclerAdapter.setCurrentSelectedCalendar(calendar);
        }

        @Override
        public void onCalendarOutOfRange(Calendar calendar) {
        }
    }

}
