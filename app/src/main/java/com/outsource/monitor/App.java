package com.outsource.monitor;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;
import com.outsource.monitor.floating.FloatingActivityLifecycleCallback;
import com.outsource.monitor.floating.FloatingBall;
import com.outsource.monitor.floating.FloatingManager;
import com.outsource.monitor.utils.DisplayUtils;
import com.outsource.monitor.utils.EnvironmentUtils;
import com.outsource.monitor.utils.PreferenceUtils;
import com.outsource.monitor.utils.PromptUtils;

/**
 * Created by Administrator on 2016/10/2.
 */
public class App extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        EnvironmentUtils.init(this);
        DisplayUtils.init(this);
        PromptUtils.init(this);
        PreferenceUtils.init(this);
        SDKInitializer.initialize(this);
        FloatingManager.getInstance().setFloatingView(new FloatingBall(this));
        registerActivityLifecycleCallbacks(new FloatingActivityLifecycleCallback());
    }
}
