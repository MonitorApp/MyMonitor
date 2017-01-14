package com.outsource.monitor.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;


import java.lang.reflect.Field;

/**
 * 显示相关总汇
 *
 * @author hao.xiong
 * @version 1.0.0
 */
public class DisplayUtils {
    private static DisplayMetrics sDisplayMetrics;
    private static Resources mRes;

    private static final float ROUND_DIFFERENCE = 0.5f;

    /**
     * 初始化操作
     *
     * @param context context
     */
    public static void init(Context context) {
        sDisplayMetrics = context.getResources().getDisplayMetrics();
        mRes = context.getResources();
    }

    /**
     * 获取屏幕宽度 单位：像素
     *
     * @return 屏幕宽度
     */
    public static int getScreenWidth() {
        return sDisplayMetrics.widthPixels;
    }

    /**
     * 获取屏幕高度 单位：像素
     *
     * @return 屏幕高度
     */
    public static int getScreenHeight() {
        return sDisplayMetrics.heightPixels;
    }

    /**
     * 获取状态栏高度
     * @return 状态栏高度
     */
    public static int getStatusBarHeight() {
        final int defaultHeightInDp = 19;
        int height = DisplayUtils.dp2px(defaultHeightInDp);
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            height = mRes.getDimensionPixelSize(Integer.parseInt(field.get(obj).toString()));
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        if (EnvironmentUtils.isFlymeOs()) {
            height = height * 2;
        }
        return height;
    }

    /**
     * 获取屏幕宽度 单位：像素
     *
     * @return 屏幕宽度
     */
    public static float getDensity() {
        return sDisplayMetrics.density;
    }

    /**
     * dp 转 px
     *
     * @param dp dp值
     * @return 转换后的像素值
     */
    public static int dp2px(int dp) {
        return (int) (dp * sDisplayMetrics.density + ROUND_DIFFERENCE);
    }

    /**
     * dp 转 px
     *
     * @param dp dp值
     * @return 转换后的像素值
     */
    public static float dp2px(float dp) {
        return dp * sDisplayMetrics.density + ROUND_DIFFERENCE;
    }

    /**
     * px 转 dp
     *
     * @param px px值
     * @return 转换后的dp值
     */
    public static int px2dp(int px) {
        return (int) (px / sDisplayMetrics.density + ROUND_DIFFERENCE);
    }

    /**
     * 获取dimen资源像素值
     * @param dimenResId dimenResId
     * @return 像素值
     */
    public static int getDimensionPixelSize(int dimenResId) {
        return mRes.getDimensionPixelSize(dimenResId);
    }

    public static float toDisplaySpan(long realSpan) {
        return  realSpan / 1000f;
    }

    public static float toDisplayFrequency(long realFrequency) {
        return  realFrequency / 1000000f;
    }

    public static float toDisplayStep(long step) {
        return step / 1000f;
    }

    public static String toDisplayLevel(float level) {
        return String.format("%.1fdpUv", level);
    }
}
