package com.outsource.monitor.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;

import com.outsource.monitor.utils.PromptUtils;


/**
 * @author xh2009cn
 * @version 1.0.0
 */
public abstract class BaseTextureView extends TextureView implements TextureView.SurfaceTextureListener {

    protected abstract void drawCanvas(Canvas canvas);
    protected int FRAME_DURATION = 50;//刷新频率
    private static final int STATE_STOPPED = 0x00;
    private static final int STATE_DRAWING = 0x01;
    private static final int STATE_PAUSE = 0x02;

    private int mState = STATE_STOPPED;
    private DrawThread mDrawThread;
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
        mState = STATE_DRAWING;
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
        mState = STATE_STOPPED;
    }

    public void onDestroy() {
        if (mDrawThread != null) {
            try {
                synchronized (mDrawThread) {
                    mDrawThread.interrupt();
                }
            } catch (Throwable e) {
                e.printStackTrace();
                PromptUtils.showToast("onDestroy error!" + e.getMessage());
            }
            mDrawThread = null;
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
        Log.e("BaseTexureView", "onSurfaceTextureAvailable");
        mDrawThread = new DrawThread();
        mDrawThread.start();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.e("BaseTexureView", "onSurfaceTextureDestroyed");
        if (mDrawThread != null) {
            try {
                synchronized (mDrawThread) {
                    mDrawThread.interrupt();
                }
            } catch (Throwable e) {
                e.printStackTrace();
                PromptUtils.showToast("onSurfaceTextureDestroyed error!" + e.getMessage());
            }
            mDrawThread = null;
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

        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    if (mState == STATE_DRAWING) {
                        synchronized (this) {
                            draw();
                        }
                    }
                    Thread.sleep(FRAME_DURATION);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void draw() {
            try {
                Canvas canvas = lockCanvas(null);
                if (canvas != null) {
                    clearCanvas(canvas);
                    if ((System.currentTimeMillis() - mLastDrawTime) >= FRAME_DURATION) {
                        mLastDrawTime = System.currentTimeMillis();
                    } else {
                        try {
                            Thread.sleep(FRAME_DURATION - System.currentTimeMillis() + mLastDrawTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    drawCanvas(canvas);
                    unlockCanvasAndPost(canvas);
                }
            } catch (Throwable e) {
                e.printStackTrace();
//                PromptUtils.showToast("drawCanvas error!" + e.getMessage());
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
