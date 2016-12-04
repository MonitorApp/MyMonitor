package com.outsource.monitor.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.TextureView;


/**
 * @author xh2009cn
 * @version 1.0.0
 */
public abstract class BaseTextureView extends TextureView implements TextureView.SurfaceTextureListener {

    abstract void drawCanvas(Canvas canvas);
    protected int FRAME_DURATION = 50;//刷新频率
    private static final int STATE_STOPPED = 0x00;
    private static final int STATE_DRAWING = 0x01;
    private static final int STATE_PAUSE = 0x02;
    private static final int STATE_STOPPING = 0x03;
    private static final int STATE_BACKGROUND_WHEN_DRAWING = 0x04;
    private static final int STATE_BACKGROUND_WHEN_PAUSE = 0x05; //When app went background the canvas will clear, we need to redraw the last frame.

    private int mState = STATE_STOPPED;
    private final DrawThread mDrawThread = new DrawThread();
    private long mLastDrawTime;
    private Paint mBgPaint;

    protected int mWidth;
    protected int mHeight;

    /**
     * @param context mContext
     */
    public BaseTextureView(Context context) {
        super(context);
        init();
    }

    /**
     * @param context mContext
     * @param attrs   attrs
     */
    public BaseTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * @param context  mContext
     * @param attrs    attrs
     * @param defStyle defStyle
     */
    public BaseTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * 开始绘制
     */
    public void start() {
        if (mState == STATE_PAUSE || mState == STATE_STOPPING || mState == STATE_DRAWING) {
            return;
        }
        if (mDrawThread != null) {
            mState = STATE_DRAWING;
            if (!mDrawThread.isStart) {
                mDrawThread.start();
            } else {
                synchronized (mDrawThread) {
                    mDrawThread.notify();
                }
            }
        }
    }

    /**
     * 暂停
     */
    public boolean pause() {
        if (mState == STATE_DRAWING) {
            mState = STATE_PAUSE;
            return true;
        }
        return false;
    }

    /**
     * 恢复
     */
    public boolean resume() {
        if (mState == STATE_PAUSE) {
            mState = STATE_DRAWING;
            synchronized (mDrawThread) {
                mDrawThread.notify();
            }
            return true;
        }
        return false;
    }

    public boolean isPaused() {
        return mState == STATE_PAUSE;
    }

    public boolean isRunning() {
        return mState == STATE_DRAWING;
    }

    /**
     * 停止绘制
     */
    public void stop() {
        if (mState == STATE_STOPPED) {
            return;
        }
        int oldState = mState;
        mState = STATE_STOPPING;
        if (oldState == STATE_PAUSE) {
            synchronized (mDrawThread) {
                mDrawThread.notify();
            }
        }
    }

    public void onDestroy() {
        mDrawThread.interrupt();
        synchronized (mDrawThread) {
            mDrawThread.notify();
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mState == STATE_BACKGROUND_WHEN_DRAWING) {
            mState = STATE_DRAWING;
            synchronized (mDrawThread) {
                mDrawThread.notify();
            }
        } else if (mState == STATE_PAUSE) {
            mState = STATE_BACKGROUND_WHEN_PAUSE;
            synchronized (mDrawThread) {
                mDrawThread.notify();
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mState == STATE_DRAWING) {
            mState = STATE_BACKGROUND_WHEN_DRAWING;
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private void init() {
        setSurfaceTextureListener(this);

        mBgPaint = new Paint();
        mBgPaint.setColor(Color.BLACK);
        mBgPaint.setStyle(Paint.Style.FILL);
    }

    private class DrawThread extends Thread {
        private boolean isStart;

        @Override
        public void run() {
            isStart = true;
            try {
                while (!isInterrupted()) {
                    if (mState == STATE_DRAWING || mState == STATE_STOPPING || mState == STATE_BACKGROUND_WHEN_PAUSE) {
                        draw();
                        Thread.sleep(FRAME_DURATION);
                    } else {
                        synchronized (this) {
                            mDrawThread.wait();
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void draw() {
            Canvas canvas = lockCanvas(null);
            if (canvas != null) {
                clearCanvas(canvas);
                if (mState == STATE_STOPPING) {
                    unlockCanvasAndPost(canvas);
                    mState = STATE_STOPPED;
                    return;
                }
                synchronized (BaseTextureView.this) {
                    if ((System.currentTimeMillis() - mLastDrawTime) >= FRAME_DURATION) {
                        mLastDrawTime = System.currentTimeMillis();
                    } else {
                        try {
                            Thread.sleep(FRAME_DURATION - System.currentTimeMillis() + mLastDrawTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                drawCanvas(canvas);
                unlockCanvasAndPost(canvas);
                if (mState == STATE_BACKGROUND_WHEN_PAUSE) { //Redraw the last frame when draw thread on pause state and app resume from background
                    mState = STATE_PAUSE;
                }
            }
        }
    }

    private void clearCanvas(Canvas canvas) {
//        Paint paint = new Paint();
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//        canvas.drawPaint(paint);
        canvas.drawPaint(mBgPaint);
    }
}
