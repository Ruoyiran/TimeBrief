package com.royran.timebrief.utils;

import com.royran.timebrief.R;
import com.royran.timebrief.ui.activity.BaseActivity;

public class StringUtils {
    public static String formatTotalTimeToString(long totalMilliseconds) {
        int hour = (int) (totalMilliseconds / 3600000L);
        int min = (int) (totalMilliseconds / 60000L % 60L);
        int sec = (int) (totalMilliseconds / 1000L % 60L);
        if (sec > 30) {
            min += 1;
        }
        if (min >= 60) {
            hour += 1;
            min = 0;
        }
        StringBuilder sb = new StringBuilder();
        if (hour > 0 && min > 0) {
            sb.append(hour);
            sb.append(BaseActivity.getInstance().getString(R.string.hour_short));

            sb.append(min);
            sb.append(BaseActivity.getInstance().getString(R.string.minute_short));
        } else {
            if (hour > 0) {
                sb.append(hour);
                sb.append(BaseActivity.getInstance().getString(R.string.hour));
            }
            if (min > 0) {
                sb.append(min);
                sb.append(BaseActivity.getInstance().getString(R.string.minute));
            }
            if (hour == 0 && min == 0) {
                sb.append("0");
                sb.append(BaseActivity.getInstance().getString(R.string.minute));
            }
        }
        return sb.toString();
    }

    public static String formatTotalTimeToStringEn(long totalMilliseconds) {
        int hour = (int) (totalMilliseconds / 3600000L);
        int min = (int) (totalMilliseconds / 60000L % 60L);
        int sec = (int) (totalMilliseconds / 1000L % 60L);
        if (sec > 30) {
            min += 1;
        }
        if (min >= 60) {
            hour += 1;
            min = 0;
        }
        StringBuilder sb = new StringBuilder();
        if (hour > 0) {
            sb.append(hour).append("h");
        }
        if (min > 0) {
            sb.append(min).append("m");
        }
        if (hour == 0 && min == 0) {
            sb.append("0m");
        }
        return sb.toString();
    }

    // pattern may contain '*' and '?'
    // pattern按*分割后，子串里可能含有?,没法用String.find, 所以针对含?的字符串，
    // 结合KMP算法，实现了find函数，之后再将pattern按*分割，
    // 在输入字符串中按顺序查找子串，已实现find含有*和?的字符串
    public static int find(final String str, final String pattern) {
        if (isEmpty(str)) {
            return -1;
        }
        if (isEmpty(pattern)) {
            return -1;
        }
        String[] items = pattern.split("\\*");
        int i = 0;
        int ret = -1;
        for (String s : items) {
            int index = kmpFind(str, s, i);
            if (index < 0) {
                return -1;
            }
            return index;
//            if (i == 0) {
//                ret = index;
//            }
//            i = index + s.length();
        }
        return ret;
    }

    private static boolean isEmpty(final String str) {
        return str == null || str.isEmpty();
    }

    // str may contain '?'
    private static int[] getNextArray(final String str) {
        if (isEmpty(str)) {
            return null;
        }
        int[] next = new int[str.length()];
        int k = -1;
        int j = 0;
        next[0] = -1;
        while (j < str.length() - 1) {
            if (k == -1 || str.charAt(k) == str.charAt(j) || str.charAt(k) == '?' || str.charAt(j) == '?') {
                k++;
                j++;
                next[j] = k;
            } else {
                k = next[k];
            }
        }
        return next;
    }

    // pattern may contain '?'
    private static int kmpFind(final String str, final String pattern, int start) {
        if (isEmpty(str)) {
            return -1;
        }
        int[] next = getNextArray(pattern);
        if (next == null) {
            return -1;
        }
        int i = start;
        while (i < str.length()) {
            int j = 0;
            while (j < pattern.length()) {
                if (i >= str.length()) {
                    return -1;
                }
                char a = Character.toLowerCase(str.charAt(i));
                char b = Character.toLowerCase(pattern.charAt(j));
                if (a == b || pattern.charAt(j) == '?') {
                    i++;
                    j++;
                } else {
                    break;
                }
            }
            i -= j;
            if (j == pattern.length()) {
                return i;
            }
            int move = j - next[j];
            i += move;
        }
        return -1;
    }
}
