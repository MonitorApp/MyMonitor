package com.outsource.monitor.floating;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.outsource.monitor.ActivityManager;
import com.outsource.monitor.utils.DisplayUtils;

/**
 * Created by xionghao on 2017/1/4.
 */

abstract class BaseFloatingManager implements IFloatingManager {

    abstract int windowType();
    abstract void attachToWindow(Activity activity);

    protected FloatingWrapper mFloatingWrapper;
    private View mFloatingView;
    private boolean isNeedShow;

    @Override
    public boolean isNeedShow() {
        return isNeedShow;
    }

    @Override
    public void setNeedShow(boolean needShow) {
        isNeedShow = needShow;
        if (needShow) {
            onActivityResume(ActivityManager.instance().getCurrentActivity());
        } else {
            onActivityPause(ActivityManager.instance().getCurrentActivity());
        }
    }

    @Override
    public void setFloatingView(View view) {
        mFloatingView = view;
        if (mFloatingWrapper != null) {
            addFloatingView();
        }
    }

    @Override
    public void onActivityResume(final Activity activity) {
        if (isNeedShow()) {
            if (mFloatingWrapper == null) {
                initFloatingLayout(activity);
            }
            if (!mFloatingWrapper.isAttachToWindow()) {
                attachToWindow(activity);
            }
            mFloatingWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onFloatingButtonClick();
                }
            });
        }
    }

    protected void initFloatingLayout(Activity activity) {
        mFloatingWrapper = new FloatingWrapper(activity);
        addFloatingView();
    }

    private void addFloatingView() {
        if (mFloatingView != null) {
            mFloatingWrapper.removeAllViews();
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            mFloatingWrapper.addView(mFloatingView, params);
        }
    }

    protected WindowManager.LayoutParams createWindowLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.width = DisplayUtils.dp2px(80);
        params.height = DisplayUtils.dp2px(80);
        params.type = windowType();
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        params.format = PixelFormat.TRANSLUCENT;
        params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        params.x = 0;
        params.y = DisplayUtils.dp2px(173);
        return params;
    }

    protected void onFloatingButtonClick() {

    }
}
