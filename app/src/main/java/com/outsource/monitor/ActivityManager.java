package com.outsource.monitor;

import android.app.Activity;

import java.util.HashSet;
import java.util.Set;

/**
 * Activity的管理
 *
 * @author hao.xiong
 * @version 1.0.0
 */
public final class ActivityManager {

    /**
     * 保存在栈里的所有Activity
     */
    private Set<Activity> mActivities = new HashSet<Activity>();
    /**
     * 当前显示的Activity，必须是onResume状态
     */
    private Activity mCurrentActivity = null;
    /**
     * 栈顶Activity，onCreate的时候添加
     */
    private Activity mTopActivity = null;
    /**
     * 栈顶Activity
     */
    private Activity mLastActivity = null;

    private static ActivityManager sInstance;

    /**
     * 获取ActivityManager实例
     *
     * @return ActivityManager实例
     */
    public static ActivityManager instance() {
        if (sInstance == null) {
            sInstance = new ActivityManager();
        }
        return sInstance;
    }

    private ActivityManager() {

    }

    /**
     * 当Activity执行onCreate时调用 - 保存启动的Activity
     *
     * @param activity 执行onCreate的Activity
     */
    public void onCreate(Activity activity) {
        mActivities.add(activity);
        mTopActivity = activity;
    }

    /**
     * 获取栈顶Activity
     * @return Activity
     */
    public Activity getTopActivity() {
        return mTopActivity;
    }

    /**
     * 当Activity执行onDestroy时调用 - 移除销毁的Activity
     *
     * @param activity 执行onDestroy时的Activity
     */
    public void onDestroy(Activity activity) {
        if (mLastActivity == activity) {
            mLastActivity = null;
        }
        if (mTopActivity == activity) {
            mTopActivity = null;
        }
        mActivities.remove(activity);
    }

    /**
     * 关闭所有activity
     */
    public void finishActivities() {
        for (Activity activity : mActivities) {
            activity.finish();
        }
        mActivities.clear();
    }

    /**
     * 当Activity执行onResume时调用 - 保存当前显示的activity，更新栈顶Activity
     *
     * @param activity 执行onResume的Activity
     */
    public void onResume(Activity activity) {
        mCurrentActivity = activity;
        mTopActivity = activity;
    }

    /**
     * 当Activity执行onPause时调用 - 清除当前显示的Activity
     *
     * @param activity 执行onPause的Activity
     */
    public void onPause(Activity activity) {
        mCurrentActivity = null;
        mLastActivity = activity;
    }

    /**
     * 获取当前显示的Activity
     *
     * @return 当前显示的Activity，可能为空
     */
    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    /**
     * 是否为当前的Activity
     *
     * @param activity activity
     * @return 是：true
     */
    public boolean isCurrentActivity(Activity activity) {
        return mCurrentActivity == activity;
    }

    /**
     * 获取栈顶的Activity
     *
     * @return 栈顶的Activity
     */
    public Activity getLastActivity() {
        return mLastActivity;
    }

    /**
     * 获取所有的Activities
     *
     * @return Activities
     */
    public Set<Activity> getActivities() {
        return mActivities;
    }
}
