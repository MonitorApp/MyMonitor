package com.outsource.monitor.floating;

import android.app.Activity;
import android.view.View;

/**
 * Created by xionghao on 2017/1/4.
 */

interface IFloatingManager {

    void onActivityResume(Activity activity);

    void onActivityPause(Activity activity);

    boolean isNeedShow();

    void setNeedShow(boolean needShow);

    void setFloatingView(View view);
}
