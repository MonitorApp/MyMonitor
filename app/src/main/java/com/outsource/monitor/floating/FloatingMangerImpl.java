package com.outsource.monitor.floating;

import android.app.Activity;
import android.content.Context;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;

/**
 * api>=19的悬浮窗可以直接用TYPE_TOAST类型，只需要attachToWindow一次，然后通过View的Visibility来控制显示隐藏
 * api<19如果悬浮窗用TYPE_TOAST类型会出现点击事件无效问题，所以只能用TYPE_APPLICATION_PANEL，在每个Activity的onResume、onPause回调中attachToWindow和detachFromWindow
 * Created by xionghao on 2017/1/4.
 */
class FloatingMangerImpl extends BaseFloatingManager {

    @Override
    int windowType() {
        return WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
    }

    @Override
    public void onActivityResume(Activity activity) {
        if (activity == null || activity.isFinishing()) return;
        if (mFloatingWrapper != null && mFloatingWrapper.isAttachToWindow()) {//如果悬浮窗已经attachToWindow，需要先从原来的窗口detach，然后再attach到新的窗口
            detachFromWindow();
        }
        super.onActivityResume(activity);
    }

    @Override
    public void onActivityPause(Activity activity) {
        if (activity == null || activity.isFinishing()) return;
        detachFromWindow();
    }

    @Override
    void attachToWindow(final Activity activity) {
        final View decorView = activity.getWindow().getDecorView();
        decorView.post(new Runnable() {
            @Override
            public void run() {
                if (activity.isFinishing()) return;
                IBinder token = decorView.getWindowToken();
                if (token == null) return;
                WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
                windowManager.addView(mFloatingWrapper, createWindowLayoutParams());
            }
        });
    }

    @Override
    protected WindowManager.LayoutParams createWindowLayoutParams() {
        WindowManager.LayoutParams params = super.createWindowLayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        return params;
    }

    private void detachFromWindow() {
        if (mFloatingWrapper != null && mFloatingWrapper.isAttachToWindow() && mFloatingWrapper.getContext() instanceof Activity) {
            Activity activity = (Activity) mFloatingWrapper.getContext();
            WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
            windowManager.removeViewImmediate(mFloatingWrapper);
        }
    }
}
