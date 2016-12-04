package com.outsource.monitor.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.view.ScaleGestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.outsource.monitor.model.FrequencyLevel;
import com.outsource.monitor.model.Level;
import com.outsource.monitor.service.DataReceiver;
import com.outsource.monitor.utils.CollectionUtils;
import com.outsource.monitor.utils.DateUtils;
import com.outsource.monitor.utils.DisplayUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author xh2009cn
 * @version 1.0.0
 */
public class LevelTextureView extends BaseTextureView implements DataReceiver, ScaleGestureDetector.OnScaleGestureListener {

    private static final int DURATION_SPAN = 60 * 1000;//一屏显示的时长，单位毫秒
    private static final float LEVEL_SPAN = 100;//一屏显示的电平数
    protected static int DURATION_PER_PX = 1;//一像素占的毫秒数
    protected static float LEVEL_PER_PX = 1;//一像素占的电平数
    private static final int MAX_SAVE_DURATION = 10 * 60 * 1000;//最大存储时长，单位毫秒
    protected static final int X_AXIS_HEIGHT = DisplayUtils.dp2px(15);//x轴刻度区域的高度
    protected static final int Y_AXIS_WIDTH = DisplayUtils.dp2px(15);//y轴刻度区域的宽度
    private static final int X_UNIT = 1000;
    private static final float Y_UNIT = 10;

    private Paint mChartPaint;
    private Paint mXyAxisPaint;
    private Paint mMarkPaint;
    private Paint mMarkTextPaint;
    private int mMarkTextHeight;

    private float mScaleX = 1.0f;
    private float mScaleY = 1.0f;

    private ScaleGestureDetector mScaleGestureDetector;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("mm:ss");
    private Date mMarkDate = new Date();

    public List<Level> mLevels = new LinkedList<>();
    private ReentrantLock mLock = new ReentrantLock();

    /**
     * @param context mContext
     */
    public LevelTextureView(Context context) {
        super(context);
        init();
    }

    /**
     * @param context mContext
     * @param attrs   attrs
     */
    public LevelTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * @param context  mContext
     * @param attrs    attrs
     * @param defStyle defStyle
     */
    public LevelTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    void drawCanvas(Canvas canvas) {
        canvas.drawLine(Y_AXIS_WIDTH - DisplayUtils.dp2px(1), mHeight - X_AXIS_HEIGHT, mWidth, mHeight - X_AXIS_HEIGHT, mXyAxisPaint);//x轴刻度线
        canvas.drawLine(Y_AXIS_WIDTH - DisplayUtils.dp2px(1), mHeight - X_AXIS_HEIGHT, Y_AXIS_WIDTH, 0, mXyAxisPaint);//y轴刻度线

        int xUnitCount = DURATION_SPAN / X_UNIT;
        int xUnitWidth = (mWidth - Y_AXIS_WIDTH) / xUnitCount;

        long currentTime = System.currentTimeMillis();
        long lastSecondTime = DateUtils.getCurrentSecondTime(currentTime);
        for (int i = 0; i <= xUnitCount; i++) {
            int x = (int) (Y_AXIS_WIDTH + i * xUnitWidth  + (currentTime - lastSecondTime) / DURATION_PER_PX);
            int xValue = i * X_UNIT;
            mMarkDate.setTime(lastSecondTime - xValue);
            String xMarkStr = mDateFormat.format(mMarkDate);
            float textWidth = mMarkTextPaint.measureText(xMarkStr);
            canvas.drawText(xMarkStr, 0, xMarkStr.length(), x - textWidth / 2 , mHeight - mMarkTextHeight, mMarkTextPaint);
            canvas.drawLine(x, mHeight - X_AXIS_HEIGHT, x, 0, mMarkPaint);
        }

        int yUnitCount = (int) (LEVEL_SPAN / Y_UNIT);
        int yUnitHeight = (mHeight - X_AXIS_HEIGHT) / yUnitCount;
        for (int i = 0; i <= yUnitCount; i++) {
            int y = mHeight - X_AXIS_HEIGHT - (i + 1) * yUnitHeight;
            float yValue = Y_UNIT * i;
            canvas.drawLine(Y_AXIS_WIDTH, y, mWidth, y, mMarkPaint);
            String yValueStr = String.format("%.1f", yValue);
            canvas.drawText(yValueStr, 0, yValueStr.length(), 0, y + yUnitHeight + (i == yUnitCount ? mMarkTextHeight : mMarkTextHeight / 2), mMarkTextPaint);
        }

        Path path = null;
        mLock.lock();
        for (Level level : mLevels) {
            long distance = (currentTime - level.timestamp) / DURATION_PER_PX;
            if (path == null) {
                path = new Path();
                path.moveTo(distance + Y_AXIS_WIDTH, mHeight - X_AXIS_HEIGHT - level.level / LEVEL_PER_PX);
            } else {
                path.lineTo(distance + Y_AXIS_WIDTH, mHeight - X_AXIS_HEIGHT - level.level / LEVEL_PER_PX);
            }
        }
        mLock.unlock();
        if (path != null) {
            canvas.drawPath(path, mChartPaint);
        }
        canvas.save();
        canvas.scale(mScaleX, mScaleY);
        canvas.restore();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        DURATION_PER_PX = DURATION_SPAN / (mWidth - Y_AXIS_WIDTH);
        LEVEL_PER_PX = LEVEL_SPAN / (mHeight - X_AXIS_HEIGHT);
    }

    @Override
    public void onReceiveItuData(float[] ituData) {
        float level = ituData[0];
        long currentTime = System.currentTimeMillis();
        mLock.lock();
        mLevels.add(new Level(level, currentTime));
        Level head = mLevels.get(0);
        if (currentTime - head.timestamp > MAX_SAVE_DURATION) {
            mLevels.remove(0);
        }
        mLock.unlock();
    }

    @Override
    public void onReceiveBandLevel(List<FrequencyLevel> levels) {

    }

    private void init() {
        mChartPaint = new Paint();
        mChartPaint.setColor(Color.BLACK);
        mChartPaint.setAntiAlias(true);
        mChartPaint.setStyle(Paint.Style.STROKE);
        mChartPaint.setStrokeWidth(DisplayUtils.dp2px(1f));

        mXyAxisPaint = new Paint();
        mXyAxisPaint.setColor(Color.BLACK);
        mXyAxisPaint.setAntiAlias(true);
        mXyAxisPaint.setStyle(Paint.Style.FILL);
        mXyAxisPaint.setStrokeWidth(DisplayUtils.dp2px(1f));

        mMarkPaint = new Paint();
        mMarkPaint.setColor(Color.GRAY);
        mMarkPaint.setAntiAlias(true);
        mMarkPaint.setStyle(Paint.Style.FILL);
        mMarkPaint.setStrokeWidth(1);

        mMarkTextPaint = new Paint();
        mMarkTextPaint.setColor(Color.BLACK);
        mMarkTextPaint.setTextSize(10);
        Paint.FontMetrics fm = mMarkTextPaint.getFontMetrics();
        mMarkTextHeight = (int) Math.ceil(fm.descent - fm.ascent);

        mScaleGestureDetector = new ScaleGestureDetector(getContext(), this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mScaleGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        mScaleX = detector.getCurrentSpanX();
        mScaleY = detector.getCurrentSpanY();
        float scale = detector.getScaleFactor();
        Log.d("xh", "mScaleX " + mScaleX + " mScaleY " + mScaleY + " scale " + scale);
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }
}
