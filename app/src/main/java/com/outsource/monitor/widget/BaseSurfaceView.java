package com.outsource.monitor.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * 系统评分
 *
 * @author hu.cao
 * @version 1.0.0
 */
public abstract class BaseSurfaceView extends SurfaceView {

    abstract void drawCanvas(Canvas canvas);

    /**
     * surfaceView刷新间隔
     */
    public static final int REFRESH_SCORE_VIEW_INTERVAL = 50; //ms

    private long mLastRefreshScoreTime = 0L;


    private static final int STATE_STOP = 0x00;
    private static final int STATE_DRAWING = 0x01;
    private static final int STATE_PAUSE = 0x02;

    private int mState = STATE_STOP;
    protected int mWidth;
    protected int mHeight;

    private LocalDrawThread mDrawThread;


    /**
     * 开始绘制
     */
    public void start() {
        mState = STATE_DRAWING;

        if (mDrawThread != null) {
            try {
                mDrawThread.start();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        if (getVisibility() != View.VISIBLE) {
            setVisibility(View.VISIBLE);
        }
    }

    /**
     * 暂停
     */
    public void pause() {
        mState = STATE_PAUSE;
    }

    /**
     * 停止绘制
     */
    public void stop() {
        mState = STATE_STOP;

        if (mDrawThread != null && !mDrawThread.isInterrupted()) {
            mDrawThread.interrupt();
        }
    }

    /**
     * @param context  mContext
     */
    public BaseSurfaceView(Context context) {
        super(context);
        init();
    }

    /**
     * @param context  mContext
     * @param attrs  attrs
     */
    public BaseSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * @param context   mContext
     * @param attrs   attrs
     * @param defStyle defStyle
     */
    public BaseSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        getHolder().addCallback(mCallback);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }

    private SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {


        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mDrawThread = new LocalDrawThread(holder);
            if (mState == STATE_DRAWING) {
                mDrawThread.start();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mState = STATE_STOP;
            mDrawThread.interrupt();
        }
    };

    private class LocalDrawThread extends Thread {
        private SurfaceHolder mSurfaceHolder;

        public LocalDrawThread(SurfaceHolder holder) {
            mSurfaceHolder = holder;

        }

        @Override
        public void run() {
            while (mState == STATE_DRAWING || mState == STATE_PAUSE) {
                if (mState == STATE_DRAWING) {
                    draw();
                }
                try {
                    Thread.sleep(REFRESH_SCORE_VIEW_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 绘制
         */
        public void draw() {
            Canvas canvas = mSurfaceHolder.lockCanvas(null);
            if (canvas != null) {
                clearCanvas(canvas);

                synchronized (BaseSurfaceView.this) {
                    if ((System.currentTimeMillis() - mLastRefreshScoreTime) >= REFRESH_SCORE_VIEW_INTERVAL) {
                        mLastRefreshScoreTime = System.currentTimeMillis();
                    } else {
                        try {
                            Thread.sleep(REFRESH_SCORE_VIEW_INTERVAL - System.currentTimeMillis() + mLastRefreshScoreTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                drawCanvas(canvas);
                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private void clearCanvas(Canvas canvas) {
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(paint);
    }
}

