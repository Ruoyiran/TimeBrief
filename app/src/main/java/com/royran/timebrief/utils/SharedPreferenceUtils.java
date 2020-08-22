package com.royran.timebrief.utils;


import android.content.Context;
import android.content.SharedPreferences;

import com.royran.timebrief.TimeBriefApp;


public class SharedPreferenceUtils {

    private static final String CONFIG = "TIME_LIFE_SHARED_CONFIG";

    public static SharedPreferences getSharedPreferences(String name) {
        return TimeBriefApp.getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public static void clear(String name) {
        getSharedPreferences(CONFIG).edit().clear().apply();
    }

    public static void write(String key, String val) {
        getSharedPreferences(CONFIG).edit().putString(key, val).apply();
    }

    public static void write(String key, boolean val) {
        getSharedPreferences(CONFIG).edit().putBoolean(key, val).apply();
    }

    public static void write(String key, int val) {
        getSharedPreferences(CONFIG).edit().putInt(key, val).apply();
    }

    public static void write(String key, long val) {
        getSharedPreferences(CONFIG).edit().putLong(key, val).apply();
    }

    public static String read(String key, String def) {
        return getSharedPreferences(CONFIG).getString(key, def);
    }

    public static boolean read(String key, boolean def) {
        return getSharedPreferences(CONFIG).getBoolean(key, def);
    }

    public static int read(String key, int def) {
        return getSharedPreferences(CONFIG).getInt(key, def);
    }

    public static long read(String key, long def) {
        return getSharedPreferences(CONFIG).getLong(key, def);
    }
}
