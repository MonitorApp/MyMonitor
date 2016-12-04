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

    public static void putInt(String key, int value) {
        sSharedPreferences.edit().putInt(key, value).apply();
    }

    public static int getInt(String key) {
        return sSharedPreferences.getInt(key, 0);
    }
}
