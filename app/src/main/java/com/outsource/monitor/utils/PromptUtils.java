package com.outsource.monitor.utils;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.outsource.monitor.ActivityManager;
import com.outsource.monitor.base.BaseActivity;

/**
 * 提示Util总汇
 *
 * @author hao.xiong
 * @version 1.0.0
 */
public class PromptUtils {

    private static Context sContext; //Application 的context
    private static Toast sToast;

    /**
     * 初始化
     *
     * @param context context
     */
    public static void init(Context context) {
        sContext = context;
        sToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
    }

    private static void showToast(String message, int duration) {
        sToast.setText(message);
        sToast.setDuration(duration);
        sToast.show();
    }

    /**
     * 显示一个Toast提示
     * @param message  提示信息
     */
    public static void showToast(String message) {
        showToast(message, Toast.LENGTH_SHORT);
    }

    /**
     * 显示一个Toast提示
     * @param resId    提示信息资源Id
     */
    public static void showToast(int resId) {
        showToast(sContext.getString(resId));
    }

    /**
     * 显示一个长时间的Toast提示
     * @param message message
     */
    public static void showLongToast(String message) {
        showToast(message, Toast.LENGTH_LONG);
    }

    /**
     * 显示一个长时间的Toast提示
     * @param resId 提示信息资源Id
     */
    public static void showLongToast(int resId) {
        showLongToast(sContext.getString(resId));
    }

    public static void showLoadingDialog() {
        showLoadingDialog("加载中…");
    }

    public static void showLoadingDialog(String message) {
        Activity activity = ActivityManager.instance().getCurrentActivity();
        if (activity instanceof BaseActivity) {
            if (activity.isFinishing()) return;
            ((BaseActivity) activity).showLoadingDialog(message);
        }
    }

    public static void dismissLoadingDialog() {
        Activity activity = ActivityManager.instance().getCurrentActivity();
        if (activity instanceof BaseActivity) {
            if (activity.isFinishing()) return;
            ((BaseActivity) activity).dismissLoadingDialog();
        }
    }
}
