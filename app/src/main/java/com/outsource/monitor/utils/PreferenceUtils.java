package com.outsource.monitor.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Administrator on 2016/11/3.
 */
public class PreferenceUtils {

    private static SharedPreferences sSharedPreferences;

    public static void init(Context context) {
        sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void putString(String key, String value) {
        sSharedPreferences.edit().putString(key, value).apply();
    }

    public static String getString(String key) {
        return sSharedPreferences.getString(key, "");
    }

    public static String getString(String key, String defVal) {
        return sSharedPreferences.getString(key, defVal);
    }

    public static void putInt(String key, int value) {
        sSharedPreferences.edit().putInt(key, value).apply();
    }

    public static int getInt(String key) {
        return sSharedPreferences.getInt(key, 0);
    }

    public static int getInt(String key, int defVal) {
        return sSharedPreferences.getInt(key, defVal);
    }

    public static void putFloat(String key, float value)
    {
        sSharedPreferences.edit().putFloat(key, value).apply();
    }

    public static float getFloat(String key, float defVal)
    {
        return sSharedPreferences.getFloat(key, defVal);
    }

    public static void putLong(String key, long value)
    {
        sSharedPreferences.edit().putLong(key, value).apply();
    }

    public static long getLong(String key, long defVal)
    {
        return sSharedPreferences.getLong(key, defVal);
    }
}
