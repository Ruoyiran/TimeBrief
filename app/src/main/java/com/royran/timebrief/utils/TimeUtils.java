package com.royran.timebrief.utils;

import com.royran.timebrief.R;
import com.royran.timebrief.ui.activity.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtils {
    public static final long ONE_DAY_MILLISENDS = 24 * 60 * 60 * 1000;
    private final static String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";


    public static Calendar getChinaTime(long timestampInMillis) {
        SimpleDateFormat format = new SimpleDateFormat(TIME_FORMAT, Locale.CHINA);
        format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        format.format(timestampInMillis);
        return format.getCalendar();
    }

    public static Calendar getChinaTime(long timestampInMillis, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.CHINA);
        format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        format.format(timestampInMillis);
        return format.getCalendar();
    }

    public static Calendar getChinaTime(Date date, String pattern) {
        return getChinaTime(date.getTime(), pattern);
    }

    public static Calendar getChinaTime(Date date) {
        return getChinaTime(date.getTime());
    }

    public static String getChinaTimeString(long timestampInMillis) {
        return getChinaTimeString(timestampInMillis, TIME_FORMAT);
    }

    public static String getChinaTimeString(long timestampInMillis, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.CHINA);
        format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        return format.format(timestampInMillis);
    }

    public static String getChinaTimeString(Date date) {
        return getChinaTimeString(date.getTime());
    }

    public static Calendar getChinaDate(long timestampInMillis) {
        Calendar calendar = getChinaTime(timestampInMillis);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        return calendar;
    }

    public static String getChinaDateString(long timestampInMillis) {
        Calendar calendar = getChinaTime(timestampInMillis);
        Integer year = calendar.get(Calendar.YEAR);
        Integer month = calendar.get(Calendar.MONTH);
        Integer dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        return String.format("%04d-%02d-%02d", year, month, dayOfMonth);
    }

    public static int getDayOfWeek(long timestampInMillis) {
        Calendar calendar = getChinaTime(timestampInMillis);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public static String getWeekString(long timeMs) {
        Calendar calendar = TimeUtils.getChinaTime(timeMs);
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        week -= 1;
        String[] weeks = BaseActivity.getInstance().getResources().getStringArray(R.array.chinese_week_string_array);
        if (week < 0 || week >= weeks.length) {
            return "";
        }
        return weeks[week];
    }

    public static Calendar getCalendar(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        return calendar;
    }
}
