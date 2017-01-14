package com.outsource.monitor.floating;


import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Created by xionghao on 2017/1/4.
 */

public class FloatingActivityLifecycleCallback implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        FloatingManager.getInstance().onActivityResume(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        FloatingManager.getInstance().onActivityPause(activity);
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
