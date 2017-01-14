package com.outsource.monitor.floating;

import android.app.Activity;
import android.os.Build;
import android.view.View;


/**
 * Created by xionghao on 2017/1/3.
 */

public class FloatingManager {


    private static FloatingManager sInstance;
    private IFloatingManager mFloatingManager;
    private View mFloatingView;

    public static FloatingManager getInstance() {
        if (sInstance == null) {
            synchronized (FloatingManager.class) {
                sInstance = new FloatingManager();
            }
        }
        return sInstance;
    }

    private FloatingManager() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            mFloatingManager = new FloatingMangerImplKitKat();
//        } else {
            mFloatingManager = new FloatingMangerImpl();
//        }
    }

    void onActivityResume(Activity activity) {
        mFloatingManager.onActivityResume(activity);
    }

    void onActivityPause(Activity activity) {
        mFloatingManager.onActivityPause(activity);
    }

    public void show() {
        mFloatingManager.setNeedShow(true);
    }

    public void hide() {
        mFloatingManager.setNeedShow(false);
    }

    public void setFloatingView(View view) {
        mFloatingView = view;
        mFloatingManager.setFloatingView(view);
    }

    public View getFloatingView() {
        return mFloatingView;
    }
}
