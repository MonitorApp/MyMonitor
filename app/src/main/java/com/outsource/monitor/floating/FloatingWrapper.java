package com.outsource.monitor.floating;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.outsource.monitor.utils.DisplayUtils;


/**
 * Created by hao.xiong on 2016/6/24.
 */
public class FloatingWrapper extends FrameLayout {

    private static final long HIDE_DURATION = 2000;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    private int mInitX = DisplayUtils.getScreenWidth() - DisplayUtils.dp2px(32);
    private int mInitY = DisplayUtils.dp2px(0);
    private int mTouchDownX = -1;
    private int mTouchDownY = -1;
    private int mTouchDownPosX;
    private int mTouchDownPosY;
    private boolean mIsMoveAccept;
    private long mTouchDownTime;
    private boolean isAttachToWindow = false;

    private Handler mAutoHideHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            int viewWidth = getMeasuredWidth();
//            int viewHeight = getMeasuredHeight();
//            if (mParams.x == 0) {
//                mParams.x -= viewWidth / 2;
//                mWindowManager.updateViewLayout(FloatingWrapper.this, mParams);
//            } else if (mParams.x == DisplayUtils.getScreenWidth() - viewWidth) {
//                mParams.x += viewWidth / 2;
//                mWindowManager.updateViewLayout(FloatingWrapper.this, mParams);
//            } else if (mParams.y == 0) {
//                mParams.y -= viewHeight / 2;
//                mWindowManager.updateViewLayout(FloatingWrapper.this, mParams);
//            } else if (mParams.y == DisplayUtils.getScreenHeight() - viewHeight) {
//                mParams.y += viewHeight / 2;
//                mWindowManager.updateViewLayout(FloatingWrapper.this, mParams);
//            }
        }
    };

    public FloatingWrapper(Context context) {
        super(context);
        init(context);
    }

    public FloatingWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mParams = new WindowManager.LayoutParams();
        mParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        mParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        mParams.format = PixelFormat.TRANSLUCENT;
        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        mParams.x = mInitX;
        mParams.y = mInitY;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                mAutoHideHandler.removeMessages(0);
                showFloatingView();
                mTouchDownX = (int) ev.getRawX();
                mTouchDownY = (int) ev.getRawY();
                mTouchDownTime = ev.getDownTime();
                mTouchDownPosX = mParams.x;
                mTouchDownPosY = mParams.y;
            } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
                if (mTouchDownTime != ev.getDownTime()) {
                    return true;
                }
                int moveX = (int) (ev.getRawX() - mTouchDownX);
                int moveY = (int) (ev.getRawY() - mTouchDownY);
                mParams.x = mTouchDownPosX + moveX;
                mParams.y = mTouchDownPosY + moveY;
                mParams.x = Math.max(0, mParams.x);
                mParams.y = Math.max(0, mParams.y);
                int viewWidth = getMeasuredWidth();
                int viewHeight = getMeasuredHeight();
                mParams.x = Math.min(DisplayUtils.getScreenWidth() - viewWidth, mParams.x);
                mParams.y = Math.min(DisplayUtils.getScreenHeight() - viewHeight, mParams.y);
                int moveThreshold = ViewConfiguration.get(getContext()).getScaledTouchSlop();
                mIsMoveAccept = Math.abs(moveX) > moveThreshold || Math.abs(moveY) > moveThreshold;
                if (mIsMoveAccept) {
                    mWindowManager.updateViewLayout(this, mParams);
                }
            } else if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
                mAutoHideHandler.sendEmptyMessageDelayed(0, HIDE_DURATION);
                if (mIsMoveAccept) {
                    mIsMoveAccept = false;
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.dispatchTouchEvent(ev);
    }

    public void addViewToWindow(View view) {
        addView(view);
        mWindowManager.addView(this, mParams);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isShouldHide()) {
            mAutoHideHandler.sendEmptyMessageDelayed(0, HIDE_DURATION);
        }
        isAttachToWindow = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAutoHideHandler.removeMessages(0);
        isAttachToWindow = false;
    }

    private boolean isShouldHide() {
        int viewWidth = getMeasuredWidth();
        int viewHeight = getMeasuredHeight();
        return mParams.x == 0 || mParams.y == 0 || mParams.x == DisplayUtils.getScreenWidth() - viewWidth || mParams.y == DisplayUtils.getScreenHeight() - viewHeight;
    }

    private void showFloatingView() {
        int viewWidth = getMeasuredWidth();
        int viewHeight = getMeasuredHeight();
        if (mParams.x < 0) {
            mParams.x = 0;
            mWindowManager.updateViewLayout(FloatingWrapper.this, mParams);
        } else if (mParams.x > DisplayUtils.getScreenWidth() - viewWidth) {
            mParams.x = DisplayUtils.getScreenWidth() - viewWidth;
            mWindowManager.updateViewLayout(FloatingWrapper.this, mParams);
        } else if (mParams.y < 0) {
            mParams.y = 0;
            mWindowManager.updateViewLayout(FloatingWrapper.this, mParams);
        } else if (mParams.y > DisplayUtils.getScreenHeight() - viewHeight) {
            mParams.y = DisplayUtils.getScreenHeight() - viewHeight;
            mWindowManager.updateViewLayout(FloatingWrapper.this, mParams);
        }
    }

    public boolean isAttachToWindow() {
        return isAttachToWindow;
    }
}
