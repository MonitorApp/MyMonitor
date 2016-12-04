package com.outsource.monitor.utils;

import java.util.Calendar;

/**
 * Created by Administrator on 2016/10/5.
 */
public class DateUtils {

    public static long getCurrentSecondTime(long currentTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long getCurrentMinuteTime(long currentTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
