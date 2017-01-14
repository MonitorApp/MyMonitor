package com.outsource.monitor.floating;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

/**
 * api>=19的悬浮窗可以直接用TYPE_TOAST类型，只需要attachToWindow一次，然后通过View的Visibility来控制显示隐藏
 * api<19如果悬浮窗用TYPE_TOAST类型会出现点击事件无效问题，所以只能用TYPE_APPLICATION_PANEL，在每个Activity的onResume、onPause回调中attachToWindow和detachFromWindow
 * Created by xionghao on 2017/1/4.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
class FloatingMangerImplKitKat extends BaseFloatingManager {

    @Override
    int windowType() {
        return WindowManager.LayoutParams.TYPE_TOAST;
    }

    @Override
    public void onActivityResume(final Activity activity) {
        if (activity == null || activity.isFinishing()) return;
        super.onActivityResume(activity);
        if (isNeedShow() && mFloatingWrapper != null) {
            mFloatingWrapper.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityPause(Activity activity) {
        if (activity == null || activity.isFinishing()) return;
        if (mFloatingWrapper != null) {
            mFloatingWrapper.setVisibility(View.GONE);
        }
    }

    @Override
    void attachToWindow(Activity activity) {
        WindowManager windowManager = (WindowManager) activity.getApplication().getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(mFloatingWrapper, createWindowLayoutParams());
    }

    @Override
    protected WindowManager.LayoutParams createWindowLayoutParams() {
        WindowManager.LayoutParams params = super.createWindowLayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_TOAST;
        return params;
    }
}
