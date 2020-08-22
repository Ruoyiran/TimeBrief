package com.royran.timebrief.utils;

import android.content.Context;
import android.text.format.DateFormat;

import com.royran.timebrief.R;

import java.util.Calendar;

public class DateHelper {
    public static String getWeekString(Context context, Calendar calendar) {
        return getWeekString(context, calendar.get(7));
    }

    public static String getWeekString(Context context, int dayOfWeek) {
        if (dayOfWeek == 0) {
            dayOfWeek = 6;
        } else {
            dayOfWeek -= 1;
        }
        String[] weeks = context.getResources().getStringArray(R.array.day_in_week);
        if (dayOfWeek < 0 || dayOfWeek >= weeks.length) {
            return null;
        }
        return weeks[dayOfWeek];
    }

    public static String getFormatedDate(Calendar paramCalendar, int paramInt) {
        if (paramCalendar == null) {
            return null;
        }
        switch (paramInt) {
            case 2:
            default:
                break;
            case 13:
                switch (paramCalendar.get(2)) {
                    default:
                        break;
                    case 11:
                        return "Dec";
                    case 10:
                        return "Nov";
                    case 9:
                        return "Oct";
                    case 8:
                        return "Sep";
                    case 7:
                        return "Aug";
                    case 6:
                        return "Jul";
                    case 5:
                        return "Jun";
                    case 4:
                        return "May";
                    case 3:
                        return "Apr";
                    case 2:
                        return "Mar";
                    case 1:
                        return "Feb";
                    case 0:
                        return "Jan";
                }
                break;
            case 12:
                return DateFormat.format("d/M/yyyy", paramCalendar.getTime()).toString();
            case 11:
                return DateFormat.format("M/yyyy", paramCalendar.getTime()).toString();
            case 10:
                return DateFormat.format("yyyy年M月d日", paramCalendar.getTime()).toString();
            case 9:
                return DateFormat.format("yyyy-M-d", paramCalendar.getTime()).toString();
            case 8:
                return DateFormat.format("M月", paramCalendar.getTime()).toString();
            case 7:
                return DateFormat.format("HH:mm", paramCalendar.getTime()).toString();
            case 6:
                StringBuilder localStringBuilder = new StringBuilder();
                localStringBuilder.append("公元");
                String str1 = DateFormat.format("yyyy", paramCalendar.getTime()).toString();
                String str2 = DateFormat.format("M", paramCalendar.getTime()).toString();
                int i = paramCalendar.get(5);
                int j = 0;
                for (paramInt = 0; paramInt < str1.length(); paramInt++) {
                    localStringBuilder.append(getChineseDigitString(String.valueOf(str1.charAt(paramInt))));
                }
                localStringBuilder.append("年");
                for (paramInt = j; paramInt < str2.length(); paramInt++) {
                    localStringBuilder.append(getChineseDigitString(String.valueOf(str2.charAt(paramInt))));
                }
                localStringBuilder.append("月");
                localStringBuilder.append(getChineseDigitString(i));
                localStringBuilder.append("日");
                return localStringBuilder.toString();
            case 5:
                return DateFormat.format("dd", paramCalendar.getTime()).toString();
            case 4:
                return DateFormat.format("MM", paramCalendar.getTime()).toString();
            case 3:
                return DateFormat.format("yyyy", paramCalendar.getTime()).toString();
            case 1:
                return DateFormat.format("M/d", paramCalendar.getTime()).toString();
            case 0:
                return DateFormat.format("yyyy年M月", paramCalendar.getTime()).toString();
        }
        return null;
    }

    public static String getChineseDigitString(String paramString) {
        try {
            int i = Integer.valueOf(paramString).intValue();
            return getChineseDigitString(i);
        } catch (Exception e) {
        }
        return "";
    }

    public static String getChineseDigitString(int paramInt) {
        Object localObject1;
        switch (paramInt) {
            default:
                localObject1 = null;
                break;
            case 10:
                localObject1 = "十";
                break;
            case 9:
                localObject1 = "九";
                break;
            case 8:
                localObject1 = "八";
                break;
            case 7:
                localObject1 = "七";
                break;
            case 6:
                localObject1 = "六";
                break;
            case 5:
                localObject1 = "五";
                break;
            case 4:
                localObject1 = "四";
                break;
            case 3:
                localObject1 = "三";
                break;
            case 2:
                localObject1 = "二";
                break;
            case 1:
                localObject1 = "一";
                break;
            case 0:
                localObject1 = "零";
        }
        Object localObject2 = localObject1;
        if (paramInt > 10) {
            localObject2 = localObject1;
            if (paramInt < 100) {
                int i = paramInt / 10;
                paramInt %= 10;
                if ((i == 1) && (paramInt != 0)) {
                    localObject1 = new StringBuilder();
                    ((StringBuilder) localObject1).append("十");
                    ((StringBuilder) localObject1).append(getChineseDigitString(paramInt));
                    localObject2 = ((StringBuilder) localObject1).toString();
                } else if ((i != 1) && (paramInt != 0)) {
                    localObject1 = new StringBuilder();
                    ((StringBuilder) localObject1).append(getChineseDigitString(i));
                    ((StringBuilder) localObject1).append("十");
                    ((StringBuilder) localObject1).append(getChineseDigitString(paramInt));
                    localObject2 = ((StringBuilder) localObject1).toString();
                } else {
                    localObject2 = localObject1;
                    if (i != 1) {
                        localObject1 = new StringBuilder();
                        ((StringBuilder) localObject1).append(getChineseDigitString(i));
                        ((StringBuilder) localObject1).append("十");
                        localObject2 = ((StringBuilder) localObject1).toString();
                    }
                }
            }
        }
        return (String) localObject2;
    }
}
