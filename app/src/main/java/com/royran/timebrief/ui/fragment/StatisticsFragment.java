package com.royran.timebrief.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.utils.Utils;
import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.jzxiang.pickerview.listener.OnDateSetListener;
import com.orhanobut.logger.Logger;
import com.royran.timebrief.popwindow.PopWindow;
import com.royran.timebrief.constants.Constants;
import com.royran.timebrief.models.DateType;
import com.royran.timebrief.models.RealmTimeRecord;
import com.royran.timebrief.models.RecordsSortType;
import com.royran.timebrief.utils.RealmHelper;
import com.royran.timebrief.utils.StringUtils;
import com.royran.timebrief.utils.TimeUtils;

import com.royran.timebrief.R;

import com.royran.timebrief.ui.activity.BaseActivity;
import com.royran.timebrief.ui.activity.SearchActivity;
import com.royran.timebrief.ui.adapter.TimeStatisticsRecyclerAdapter;
import com.royran.timebrief.ui.views.chart.BarChartSortMethod;
import com.royran.timebrief.ui.views.chart.BarChartType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class StatisticsFragment extends BaseFragment implements BaseActivity.OnActivityResultListener {

    @BindView(R.id.text_selected_date)
    TextView mTextSelectedDate;

    @BindView(R.id.img_left_arrow)
    ImageView mImgLeftArrow;

    @BindView(R.id.img_right_arrow)
    ImageView mImgRightArrow;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    TimeStatisticsRecyclerAdapter mStatisticsRecyclerAdapter;

    private Calendar mStartCalendar;
    private Calendar mEndCalendar;

    private DateType mSelectedDateType = DateType.WEEK;

    private DateType mLastSelectedDataType = mSelectedDateType;

    private String mCurrentSearchText;

    private long mSelectedCustomStartTimeMs;
    private long mSelectedCustomEndTimeMs;

    @BindView(R.id.my_toolbar)
    Toolbar toolbar;
    public static BaseFragment newInstance() {
        return new StatisticsFragment();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.fragment_statistics;
    }

    @Override
    protected void onCreateView() {
        Logger.d("Statistics::onCreateView");
        Utils.init(getContext());
        initStartEndCalendar();
        initChart();
        ((BaseActivity) getActivity()).setSupportActionBar(toolbar);
        ((BaseActivity) getActivity()).addOnActivityResultListener(this);
        setHasOptionsMenu(true);
    }

    @OnClick(R.id.img_up_arrow)
    void onUpArrowClicked() {
        mRecyclerView.scrollToPosition(0);
    }

    @OnClick(R.id.img_down_arrow)
    void onDownArrowClicked() {
        mRecyclerView.scrollToPosition(mStatisticsRecyclerAdapter.getItemCount()-1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.d("onActivityResult - requestCode: %d, resultCode: %d", requestCode, resultCode);
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.OPEN_RECORD_DETAIL_ACTIVITY_REQUEST_CODE) {
            setContents();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.statisitcs_settings, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_chart_settings:
                openChartSettingsDialog();
                break;
            case R.id.menu_list_settings:
                openListSettingsDialog();
                break;
            default:
                break;
        }
        return true;
    }

    private void openChartSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View sortView = LayoutInflater.from(getContext()).inflate(R.layout.chart_settings_layout, null);
        builder.setView(sortView);
        builder.setTitle("图表设置");
        final AlertDialog dialog = builder.show();
        ChartViewHolder holer = new ChartViewHolder(sortView, dialog);
        holer.setDefaultRadioChecked(mStatisticsRecyclerAdapter.getChartType(), mStatisticsRecyclerAdapter.getChartSortMethod(), mStatisticsRecyclerAdapter.isChartSortAscend());
    }

    private void openListSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View sortView = LayoutInflater.from(getContext()).inflate(R.layout.list_settings_layout, null);
        builder.setView(sortView);
        builder.setTitle("列表设置");
        final AlertDialog dialog = builder.show();
        SortViewHolder holer = new SortViewHolder(sortView, dialog);
        holer.setDefaultRadioChecked(mStatisticsRecyclerAdapter.getListSortType(), mStatisticsRecyclerAdapter.isListSortAscend());
    }

    public DateType getCurrentSelectedDateType() {
        return mSelectedDateType;
    }

    public Calendar getStartCalendar() {
        return TimeUtils.getChinaTime(mStartCalendar.getTimeInMillis());
    }

    public Calendar getEndCalendar() {
        return TimeUtils.getChinaTime(mEndCalendar.getTimeInMillis());
    }

    public long getSelectedCustomStartTimeMs() {
        return mSelectedCustomStartTimeMs;
    }

    public long getSelectedCustomEndTimeMs() {
        return mSelectedCustomEndTimeMs;
    }

    class ChartViewHolder {
        @BindView(R.id.stat_radio_group)
        RadioGroup statRadioGroup;

        @BindView(R.id.sort_radio_group)
        RadioGroup sortRadioGroup;

        @BindView(R.id.order_radio_group)
        RadioGroup orderRadioGroup;

        final AlertDialog alertDialog;

        ChartViewHolder(View view, AlertDialog dialog) {
            ButterKnife.bind(this, view);
            alertDialog = dialog;
        }

        void setDefaultRadioChecked(BarChartType chartType,
                                    BarChartSortMethod chartSortMethod,
                                    boolean chartSortAscend) {
            if (chartType == BarChartType.Times) {
                statRadioGroup.check(R.id.radio_times);
            } else if (chartType == BarChartType.TotalTime) {
                statRadioGroup.check(R.id.radio_total_time);
            } else {
                statRadioGroup.check(R.id.radio_times);
            }
            if (chartSortMethod == BarChartSortMethod.Count) {
                sortRadioGroup.check(R.id.radio_total);
            } else {
                sortRadioGroup.check(R.id.radio_time);
            }
            if (chartSortAscend) {
                orderRadioGroup.check(R.id.radio_ascend);
            } else {
                orderRadioGroup.check(R.id.radio_descend);
            }
        }

        @OnClick(R.id.btn_ok)
        void onOkClicked() {
            BarChartType chartType = BarChartType.Times;
            BarChartSortMethod chartSortMethod;
            boolean isAscend = false;
            if (orderRadioGroup.getCheckedRadioButtonId() == R.id.radio_ascend) {
                isAscend = true;
            }
            if (sortRadioGroup.getCheckedRadioButtonId() == R.id.radio_total) {
                chartSortMethod = BarChartSortMethod.Count;
            } else {
                chartSortMethod = BarChartSortMethod.Time;
            }
            if (statRadioGroup.getCheckedRadioButtonId() == R.id.radio_times) {
                chartType = BarChartType.Times;
            } else if (statRadioGroup.getCheckedRadioButtonId() == R.id.radio_total_time) {
                chartType = BarChartType.TotalTime;
            }
            mStatisticsRecyclerAdapter.setChartType(chartType, chartSortMethod, isAscend);
            alertDialog.dismiss();
        }

        @OnClick(R.id.btn_cancle)
        void onCancleClicked() {
            alertDialog.dismiss();
        }
    }

    class SortViewHolder {
        @BindView(R.id.sort_radio_group)
        RadioGroup sortRadioGroup;

        @BindView(R.id.order_radio_group)
        RadioGroup orderRadioGroup;

        final AlertDialog alertDialog;

        SortViewHolder(View view, AlertDialog dialog) {
            ButterKnife.bind(this, view);
            alertDialog = dialog;
        }

        void setDefaultRadioChecked(RecordsSortType sortType, boolean sortAscend) {
            if (sortType == RecordsSortType.Time) {
                sortRadioGroup.check(R.id.radio_time);
            } else if (sortType == RecordsSortType.Days) {
                sortRadioGroup.check(R.id.radio_days);
            } else if (sortType == RecordsSortType.Count) {
                sortRadioGroup.check(R.id.radio_count);
            } else {
                sortRadioGroup.check(R.id.radio_time);
            }
            if (sortAscend) {
                orderRadioGroup.check(R.id.radio_ascend);
            } else {
                orderRadioGroup.check(R.id.radio_descend);
            }
        }


        @OnClick(R.id.btn_ok)
        void onOkClicked() {
            boolean isAscend = false;
            if (orderRadioGroup.getCheckedRadioButtonId() == R.id.radio_ascend) {
                isAscend = true;
            }
            switch (sortRadioGroup.getCheckedRadioButtonId()) {
                case R.id.radio_time:
                    mStatisticsRecyclerAdapter.setListSortType(RecordsSortType.Time, isAscend);
                    break;
                case R.id.radio_days:
                    mStatisticsRecyclerAdapter.setListSortType(RecordsSortType.Days, isAscend);
                    break;
                case R.id.radio_count:
                    mStatisticsRecyclerAdapter.setListSortType(RecordsSortType.Count, isAscend);
                    break;
                default:
                    break;
            }
            alertDialog.dismiss();
        }

        @OnClick(R.id.btn_cancle)
        void onCancleClicked() {
            alertDialog.dismiss();
        }

    }

    private void initStartEndCalendar() {
        switch (mSelectedDateType) {
            case DAY:
                mStartCalendar = getNowDayCalendar();
                mEndCalendar = getNowDayCalendar();
                break;
            case WEEK:
                mStartCalendar = getMondayCalendar();
                mEndCalendar = getSundayCalendar();
                break;
            case MONTH:
                mStartCalendar = getMonthBeginCalendar();
                mEndCalendar = getMonthEndCalendar();
                break;
            case YEAR:
                mStartCalendar = getYearBeginCalendar();
                mEndCalendar = getYearEndCalendar();
                break;
            case QUARTER:
                mStartCalendar = getQuarterBeginCalendar();
                mEndCalendar = getQuarterEndCalendar();
                break;
            case CUSTOM:
                setCustomStartEndCalendar();
                break;
            default:
                break;
        }
    }

    void setCustomStartEndCalendar() {
        if (mSelectedCustomStartTimeMs > 0) {
            mStartCalendar = TimeUtils.getChinaDate(mSelectedCustomStartTimeMs);
        } else {
            mStartCalendar.setTimeInMillis(0);
        }
        if (mSelectedCustomEndTimeMs > 0) {
            mEndCalendar = TimeUtils.getChinaDate(mSelectedCustomEndTimeMs);
        } else {
            mEndCalendar.setTimeInMillis(0);
        }
    }

    private Calendar getMonthBeginCalendar() {
        Calendar calendar = getNowDayCalendar();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar;
    }

    private Calendar getMonthEndCalendar() {
        Calendar calendar = getNowDayCalendar();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar;
    }

    private Calendar getYearBeginCalendar() {
        Calendar calendar = getNowDayCalendar();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        return calendar;
    }

    private Calendar getYearEndCalendar() {
        Calendar calendar = getNowDayCalendar();
        calendar.set(Calendar.DAY_OF_MONTH, 31);
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        return calendar;
    }


    private Calendar getQuarterBeginCalendar() {
        Calendar calendar = getNowDayCalendar();
        int month = calendar.get(Calendar.MONTH);
        int quarterStartMonth = 0;
        if (month <= Calendar.MARCH) {
            quarterStartMonth = Calendar.JANUARY;
        } else if (month <= Calendar.JUNE) {
            quarterStartMonth = Calendar.APRIL;
        } else if (month <= Calendar.SEPTEMBER) {
            quarterStartMonth = Calendar.JULY;
        } else {
            quarterStartMonth = Calendar.OCTOBER;
        }
        calendar.set(Calendar.MONTH, quarterStartMonth);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar;
    }

    private Calendar getQuarterEndCalendar() {
        Calendar calendar = getNowDayCalendar();
        int month = calendar.get(Calendar.MONTH);
        int quarterEndMonth = 0;
        if (month <= Calendar.MARCH) {
            quarterEndMonth = Calendar.MARCH;
        } else if (month <= Calendar.JUNE) {
            quarterEndMonth = Calendar.JUNE;
        } else if (month <= Calendar.SEPTEMBER) {
            quarterEndMonth = Calendar.SEPTEMBER;
        } else {
            quarterEndMonth = Calendar.DECEMBER;
        }
        calendar.set(Calendar.MONTH, quarterEndMonth);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar;
    }

    @Override
    public void onEnable() {
        Logger.d("Statistics::onEnable");
        setContents();
    }

    private void setContents() {
        setSelectedDateText();
        refreshChart();
    }

    void initChart() {
        mStatisticsRecyclerAdapter = new TimeStatisticsRecyclerAdapter((BaseActivity) getActivity());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mStatisticsRecyclerAdapter);
    }

    void refreshChart() {
        List<RealmTimeRecord> records;
        if (mSelectedDateType == DateType.ALL) {
            records = RealmHelper.getAllFinishedTimeRecords();
        } else {
            records = RealmHelper.getTimeRecordsByTime(mStartCalendar.getTimeInMillis(),
                    mEndCalendar.getTimeInMillis()+TimeUtils.ONE_DAY_MILLISENDS);
        }

        if (TextUtils.isEmpty(mCurrentSearchText)) {
            mStatisticsRecyclerAdapter.setDataList(records);
        } else {
            List<RealmTimeRecord> subRecords = new ArrayList<>();
            for (RealmTimeRecord record : records) {
                boolean titleIsMatch = StringUtils.find(record.getTitle(), mCurrentSearchText) >= 0;
                boolean bodyIsMatch = StringUtils.find(record.getSummary(), mCurrentSearchText) >= 0;
                if (titleIsMatch || bodyIsMatch) {
                    subRecords.add(record);
                }
            }
            mStatisticsRecyclerAdapter.setDataList(subRecords);
        }
    }

    private Calendar getNowDayCalendar() {
        return TimeUtils.getChinaDate(new Date().getTime());
    }

    private Calendar getMondayCalendar() {
        Calendar calendar = getNowDayCalendar();
        Integer week = calendar.get(Calendar.DAY_OF_WEEK);
        if (week == Calendar.SUNDAY) {
            week = 8;
        }
        week -= 2; // MONDAY: 0, ..., SUNDAY: 6
        calendar.add(Calendar.DAY_OF_MONTH, -week);
        return calendar;
    }

    private Calendar getSundayCalendar() {
        Calendar calendar = getMondayCalendar();
        calendar.add(Calendar.DAY_OF_MONTH, 6);
        return calendar;
    }

    private void setSelectedDateText() {
        switch (mSelectedDateType) {
            case ALL:
                mTextSelectedDate.setText("全部时间");
                break;
            case DAY:
                mTextSelectedDate.setText(getDayText());
                break;
            case WEEK:
                mTextSelectedDate.setText(getWeekText());
                break;
            case MONTH:
                mTextSelectedDate.setText(getMonthText());
                break;
            case YEAR:
                mTextSelectedDate.setText(getYearText());
                break;
            case QUARTER:
                mTextSelectedDate.setText(getQuarterText());
                break;
            case CUSTOM:
                mTextSelectedDate.setText(getCustomText());
                break;
            default:
                break;
        }
        if (mSelectedDateType == DateType.ALL) {
            mImgLeftArrow.setVisibility(View.GONE);
            mImgRightArrow.setVisibility(View.GONE);
        } else {
            mImgLeftArrow.setVisibility(View.VISIBLE);
            mImgRightArrow.setVisibility(View.VISIBLE);
        }
    }

    private String getDateText(String prefix) {
        StringBuilder stringBuilder = new StringBuilder();
        Calendar calendar = getNowDayCalendar();
        if (calendar.getTimeInMillis() >= mStartCalendar.getTimeInMillis() &&
                calendar.getTimeInMillis() <= mEndCalendar.getTimeInMillis() &&
                !TextUtils.isEmpty(prefix)) {
            stringBuilder.append(prefix);
            stringBuilder.append(" ");
        } else {
            stringBuilder.append(mStartCalendar.get(Calendar.YEAR));
            stringBuilder.append(".");
        }
        stringBuilder.append(mStartCalendar.get(Calendar.MONTH)+1);
        stringBuilder.append(".");
        stringBuilder.append(mStartCalendar.get(Calendar.DAY_OF_MONTH));
        if (mStartCalendar.getTimeInMillis() < mEndCalendar.getTimeInMillis()) {
            stringBuilder.append(" ~ ");
            if (mEndCalendar.get(Calendar.YEAR) > mStartCalendar.get(Calendar.YEAR)) {
                if (calendar.getTimeInMillis() >= mStartCalendar.getTimeInMillis() &&
                        calendar.getTimeInMillis() <= mEndCalendar.getTimeInMillis() &&
                        !TextUtils.isEmpty(prefix)) {
                } else {
                    stringBuilder.append(mEndCalendar.get(Calendar.YEAR));
                    stringBuilder.append(".");
                }
            }
            stringBuilder.append(mEndCalendar.get(Calendar.MONTH)+1);
            stringBuilder.append(".");
            stringBuilder.append(mEndCalendar.get(Calendar.DAY_OF_MONTH));
        }
        return stringBuilder.toString();
    }

    private String getDayText() {
        return getDateText(getString(R.string.today));
    }

    private String getWeekText() {
        return getDateText(getString(R.string.this_week));
    }

    private String getMonthText() {
        return getDateText(getString(R.string.this_month));
    }

    private String getYearText() {
        return getDateText(getString(R.string.this_year));
    }

    private String getQuarterText() {
        return getDateText(getString(R.string.this_quarter));
    }

    private String getCustomText() {
        return getDateText("");
    }

    @OnClick(R.id.img_search)
    void onSearchClicked() {
        SearchActivity.open(getContext(), text -> {
            searchRecords(text);
        });
    }

    @OnClick(R.id.text_selected_date)
    void onTextSelectedDateClicked() {
        View view = View.inflate(getContext(), R.layout.bottom_menu_layout, null);
        final PopWindow popWindow = new PopWindow.Builder(getActivity())
                .setStyle(PopWindow.PopWindowStyle.PopUp)
                .addContentView(view)
                .create();
        TimeSelectDialog dialog = new TimeSelectDialog(view, mSelectedDateType);
        dialog.setStartEndTime(mStartCalendar.getTimeInMillis(), mEndCalendar.getTimeInMillis());
        dialog.setOnOkClickedListener((dateType, startTimeMs, endTimeMs) -> {
            popWindow.dismiss();
            mSelectedDateType = dateType;
            mSelectedCustomStartTimeMs = startTimeMs;
            mSelectedCustomEndTimeMs = endTimeMs;
            initStartEndCalendar();
            setContents();
        });
        ButterKnife.bind(dialog, view);
        popWindow.show();
    }

    public String getCurrentSearchText() {
        return mCurrentSearchText;
    }

    private void searchRecords(String text) {
        text = text.trim();
        mCurrentSearchText = text;
        refreshChart();
    }

    @OnClick(R.id.img_left_arrow)
    void onLeftArrowClicked() {
        switch (mSelectedDateType) {
            case ALL:
                mTextSelectedDate.setText("全部时间");
                break;
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
                int offset = (int)((mSelectedCustomEndTimeMs-mSelectedCustomStartTimeMs)/TimeUtils.ONE_DAY_MILLISENDS) + 1;
                addDays(-offset);
                break;

            default:
                break;
        }
        setContents();
    }

    @OnClick(R.id.img_right_arrow)
    void onRightArrowClicked() {
        switch (mSelectedDateType) {
            case ALL:
                mTextSelectedDate.setText("全部时间");
                break;
            case DAY:
                addDays(1);
                break;
            case WEEK:
                addDays(7);
                break;
            case MONTH:
                addMonths(1);
                break;
            case YEAR:
                addYears(1);
                break;
            case QUARTER:
                addMonths(3);
                break;
            case CUSTOM:
                int offset = (int)((mSelectedCustomEndTimeMs-mSelectedCustomStartTimeMs)/TimeUtils.ONE_DAY_MILLISENDS) + 1;
                addDays(offset);
                break;
            default:
                break;
        }
        setContents();
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


    class TimeSelectDialog {
        private int[] mAllDoneImageIds = {R.id.img_all_done, R.id.img_day_done, R.id.img_week_done,
                R.id.img_month_done, R.id.img_quarter_done, R.id.img_year_done};
        private View mView;
        private DateType mDateType;
        private OnOkClickedListener mOnOkClickedListener;

        long mCustomStartTimeMillis;
        long mCustomEndTimeMillis;

        Switch mCustomDateSwitch;
        TextView mTextStartTime;
        TextView mTextEndTime;


        TimeSelectDialog(View view, DateType selectedDateType) {
            mView = view;
            mDateType = selectedDateType;
            mCustomDateSwitch = mView.findViewById(R.id.custom_date_switch);
            mTextStartTime = mView.findViewById(R.id.text_start_time);
            mTextEndTime = mView.findViewById(R.id.text_end_time);
            setDoneFlagVisible();
        }

        void setOnOkClickedListener(OnOkClickedListener listener) {
            mOnOkClickedListener = listener;
        }

        @OnClick(R.id.layout_option_all)
        void onTimeAllSelected() {
            mDateType = DateType.ALL;
            setDoneFlagVisible();
            onOkClicked();
        }

        @OnClick(R.id.layout_option_day)
        void onTimeDaySelected() {
            mDateType = DateType.DAY;
            setDoneFlagVisible();
            onOkClicked();
        }

        @OnClick(R.id.layout_option_week)
        void onTimeWeekSelected() {
            mDateType = DateType.WEEK;
            setDoneFlagVisible();
            onOkClicked();
        }

        @OnClick(R.id.layout_option_month)
        void onTimeMonthSelected() {
            mDateType = DateType.MONTH;
            setDoneFlagVisible();
            onOkClicked();
        }

        @OnClick(R.id.layout_option_quarter)
        void onTimeQuarterSelected() {
            mDateType = DateType.QUARTER;
            setDoneFlagVisible();
            onOkClicked();
        }

        @OnClick(R.id.layout_option_year)
        void onTimeYearSelected() {
            mDateType = DateType.YEAR;
            setDoneFlagVisible();
            onOkClicked();
        }

        @OnClick(R.id.layout_ok)
        void onOkClicked() {
            if (mDateType == DateType.CUSTOM) {
                if (mCustomStartTimeMillis > 0 && mCustomEndTimeMillis > 0
                        && mCustomStartTimeMillis > mCustomEndTimeMillis) {
                    Toast.makeText(getContext(), getString(R.string.end_date_error), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (mOnOkClickedListener != null) {
                mOnOkClickedListener.onOk(mDateType, mCustomStartTimeMillis, mCustomEndTimeMillis);
            }
        }

        @OnClick(R.id.layout_start_time)
        void onStartTimeClicked() {
            long currentTimeMs = System.currentTimeMillis();
            if (mCustomStartTimeMillis > 0) {
                currentTimeMs = mCustomStartTimeMillis;
            }
            showCustomDialogTimePicker(currentTimeMs, Type.YEAR_MONTH_DAY, true,
                    (timePickerView, millseconds) -> {
                mCustomStartTimeMillis = millseconds;
                setStartTimeText();
            });
        }

        @OnClick(R.id.layout_end_time)
        void onEndTimeClicked() {
            long currentTimeMs = System.currentTimeMillis();
            if (mCustomEndTimeMillis > 0) {
                currentTimeMs = mCustomEndTimeMillis;
            }
            showCustomDialogTimePicker(currentTimeMs, Type.YEAR_MONTH_DAY, true,
                    (timePickerView, millseconds) -> {
                mCustomEndTimeMillis = millseconds;
                setEndTimeText();
            });
        }

        void setStartTimeText() {
            if (mCustomStartTimeMillis > 0) {
                mTextStartTime.setText(TimeUtils.getChinaTimeString(mCustomStartTimeMillis, "yyyy年MM月dd日"));
            } else {
                mTextStartTime.setText("不限");
            }
        }

        void setEndTimeText() {
            if (mCustomEndTimeMillis > 0) {
                mTextEndTime.setText(TimeUtils.getChinaTimeString(mCustomEndTimeMillis, "yyyy年MM月dd日"));
            } else {
                mTextEndTime.setText("不限");
            }
        }

        void showCustomDialogTimePicker(long currentTimeMs, Type type, boolean cyclic, OnDateSetListener listener) {
            Calendar minDate = TimeUtils.getCalendar(1970, Calendar.JANUARY, 1);
            Calendar maxDate = TimeUtils.getCalendar(2100, Calendar.DECEMBER, 31);
            long minMillis = minDate.getTimeInMillis();
            long maxMills = maxDate.getTimeInMillis();
            TimePickerDialog mDialogAll = new TimePickerDialog.Builder()
                    .setCallBack(listener)
                    .setTitleStringId("日期选择")
                    .setCancelStringId("取消")
                    .setSureStringId("确定")
                    .setCyclic(cyclic)
                    .setMinMillseconds(minMillis)
                    .setMaxMillseconds(maxMills)
                    .setCurrentMillseconds(currentTimeMs)
                    .setThemeColor(getResources().getColor(R.color.colorAccent))
                    .setType(type)
                    .setWheelItemTextNormalColor(getResources().getColor(R.color.timetimepicker_default_text_color))
                    .setWheelItemTextSelectorColor(getResources().getColor(R.color.timepicker_toolbar_bg))
                    .setWheelItemTextSize(12)
                    .build();
            mDialogAll.show(getFragmentManager(), "");
        }

        @OnCheckedChanged(R.id.custom_date_switch)
        void onCustomDateSwitchChecked(boolean isChecked) {
            if (isChecked) {
                mLastSelectedDataType = mDateType;
                mDateType = DateType.CUSTOM;
            } else {
                mDateType = mLastSelectedDataType;
            }
            setDoneFlagVisible();
        }

        void hideAllDoneFlags() {
            for (int id : mAllDoneImageIds) {
                mView.findViewById(id).setVisibility(View.GONE);
            }
        }

        void setDoneFlagVisible() {
            hideAllDoneFlags();
            mView.findViewById(R.id.layout_time_select).setVisibility(View.GONE);
            switch (mDateType) {
                case ALL:
                    mView.findViewById(R.id.img_all_done).setVisibility(View.VISIBLE);
                    break;
                case DAY:
                    mView.findViewById(R.id.img_day_done).setVisibility(View.VISIBLE);
                    break;
                case WEEK:
                    mView.findViewById(R.id.img_week_done).setVisibility(View.VISIBLE);
                    break;
                case MONTH:
                    mView.findViewById(R.id.img_month_done).setVisibility(View.VISIBLE);
                    break;
                case YEAR:
                    mView.findViewById(R.id.img_year_done).setVisibility(View.VISIBLE);
                    break;
                case QUARTER:
                    mView.findViewById(R.id.img_quarter_done).setVisibility(View.VISIBLE);
                    break;
                case CUSTOM:
                    mView.findViewById(R.id.layout_time_select).setVisibility(View.VISIBLE);
                    mCustomDateSwitch.setChecked(true);
                    break;
                default:
                    break;
            }
        }

        void setStartEndTime(long startTimeInMillis, long endTimeInMillis) {
            mCustomStartTimeMillis = startTimeInMillis;
            mCustomEndTimeMillis = endTimeInMillis;
            setStartTimeText();
            setEndTimeText();
        }
    }

    public interface OnOkClickedListener {
        void onOk(DateType dateType, long startTimeMs, long endTimeMs);
    }
}
