package com.outsource.monitor.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.outsource.monitor.utils.InputMethodUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * 检测软键盘事件ViewGroup，点击软键盘区域以外自动关闭软键盘
 * @author hao.xiong
 * @version 1.0.0
 */
public class DetectSoftInputEventFrameLayout extends FrameLayout {

    private InputMethodUtils.OnSoftInputEventListener mOnSoftInputEventListener;
    private boolean mCloseSoftInputOnTouchOutside = true;
    private boolean mInterceptTouchEvent = false;

    private int mPreviousHeight;

    /**
     * @param context context
     */
    public DetectSoftInputEventFrameLayout(Context context) {
        this(context, null);
    }

    /**
     * @param context context
     * @param attrs attrs
     */
    public DetectSoftInputEventFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * @param context context
     * @param attrs attrs
     * @param defStyle defStyle
     */
    public DetectSoftInputEventFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (mPreviousHeight != 0) {
            if (measureHeight < mPreviousHeight) {
                InputMethodUtils.setIsInputMethodShowing(true);
                if (mOnSoftInputEventListener != null) {
                    mOnSoftInputEventListener.onSoftInputOpened();
                }
                EventBus.getDefault().post(new SoftKeyboardOpenEvent());
                View rootView = getRootView();
                if (rootView != null) {
                   rootView.setBackgroundColor(Color.WHITE);
                }
            } else if (measureHeight > mPreviousHeight) {
                InputMethodUtils.setIsInputMethodShowing(false);
                EventBus.getDefault().post(new SoftKeyboardCloseEvent());
                if (mOnSoftInputEventListener != null) {
                    mOnSoftInputEventListener.onSoftInputClosed();
                }
                View rootView = getRootView();
                if (rootView != null) {
                    rootView.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        }
        mPreviousHeight = measureHeight;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (InputMethodUtils.isInputMethodShowing() && getContext() instanceof Activity && mCloseSoftInputOnTouchOutside) {
            InputMethodUtils.hideSoftInput((Activity)getContext());
        }
        return mInterceptTouchEvent || super.onTouchEvent(event);
    }

    /**
     * 设置软键盘事件监听
     * @param listener listener
     */
    public void setOnSoftInputEventListener(InputMethodUtils.OnSoftInputEventListener listener) {
        mOnSoftInputEventListener = listener;
    }

    /**
     * 点击软键盘外部区域是否关闭软键盘
     * @param closeSoftInputOnTouchOutside closeSoftInputOnTouchOutside
     */
    public void canCloseSoftInputOnTouchOutside(boolean closeSoftInputOnTouchOutside) {
        mCloseSoftInputOnTouchOutside = closeSoftInputOnTouchOutside;
    }

    /**
     * 点击软键盘外部区域是否拦截点击事件
     * @param interceptTouchEvent interceptTouchEvent
     */
    public void interceptTouchEvent(boolean interceptTouchEvent) {
        mInterceptTouchEvent = interceptTouchEvent;
    }

    public static class SoftKeyboardOpenEvent {

    }

    public static class SoftKeyboardCloseEvent {

    }
}
