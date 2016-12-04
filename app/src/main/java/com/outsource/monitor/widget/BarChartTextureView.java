package com.outsource.monitor.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;

import com.outsource.monitor.model.FrequencyLevel;
import com.outsource.monitor.model.Level;
import com.outsource.monitor.service.DataReceiver;
import com.outsource.monitor.utils.DateUtils;
import com.outsource.monitor.utils.DisplayUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Administrator on 2016/10/5.
 */
public class BarChartTextureView extends BaseTextureView implements DataReceiver {

    private static final int DURATION_SPAN = 60 * 60 * 1000;//一屏显示的时长，1小时，单位毫秒
    private static final float LEVEL_SPAN = 100;//一屏显示的电平数
    private static int DURATION_PER_PX = 1;//一像素占的毫秒数
    private static float LEVEL_PER_PX = 1;//一像素占的电平数
    private static final int MAX_SAVE_DURATION = 2 * 60 * 60 * 1000;//最大存储时长，2小时，单位毫秒
    private static final int X_AXIS_HEIGHT = DisplayUtils.dp2px(15);//x轴刻度区域的高度
    private static final int Y_AXIS_WIDTH = DisplayUtils.dp2px(15);//y轴刻度区域的宽度
    private static final int X_UNIT = 60 * 1000;
    private static final float Y_UNIT = 10;
    private static final int BAR_WIDTH = DisplayUtils.dp2px(4);

    private Paint mBarPaint;
    private Paint mXyAxisPaint;
    private Paint mMarkPaint;
    private Paint mMarkTextPaint;
    private int mMarkTextHeight;

    private SimpleDateFormat mDateFormat = new SimpleDateFormat("hh:mm");
    private Date mMarkDate = new Date();
    private float mMaxLevelCurrentMinute;

    public List<Level> mMaxLevels = new ArrayList<>();
    private ReentrantLock mLock = new ReentrantLock();

    public BarChartTextureView(Context context) {
        super(context);
        init();
    }

    public BarChartTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        DURATION_PER_PX = DURATION_SPAN / (mWidth - Y_AXIS_WIDTH);
        LEVEL_PER_PX = LEVEL_SPAN / (mHeight - X_AXIS_HEIGHT);
        Log.d("xh", "DURATION_PER_PX " + DURATION_PER_PX + " LEVEL_PER_PX " + LEVEL_PER_PX);
    }

    @Override
    void drawCanvas(Canvas canvas) {
        canvas.drawLine(Y_AXIS_WIDTH - DisplayUtils.dp2px(1), mHeight - X_AXIS_HEIGHT, mWidth, mHeight - X_AXIS_HEIGHT, mXyAxisPaint);//x轴刻度线
        canvas.drawLine(Y_AXIS_WIDTH - DisplayUtils.dp2px(1), mHeight - X_AXIS_HEIGHT, Y_AXIS_WIDTH, 0, mXyAxisPaint);//y轴刻度线

        int xUnitCount = DURATION_SPAN / X_UNIT;
        int xUnitWidth = (mWidth - Y_AXIS_WIDTH) / xUnitCount;

        long currentTime = System.currentTimeMillis();
        long lastSecondTime = DateUtils.getCurrentMinuteTime(currentTime);
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

        mLock.lock();
        for (Level level : mMaxLevels) {
            long distance = (currentTime - level.timestamp) / DURATION_PER_PX;
            canvas.drawRect(Y_AXIS_WIDTH + Math.max(distance - BAR_WIDTH / 2, 0), mHeight - X_AXIS_HEIGHT - level.level / LEVEL_PER_PX, distance + Y_AXIS_WIDTH + BAR_WIDTH / 2, mHeight - X_AXIS_HEIGHT, mBarPaint);
        }
        mLock.unlock();
    }


    @Override
    public void onReceiveItuData(float[] ituData) {
        float level = ituData[0];
        long currentTime = System.currentTimeMillis();
        long currentMinuteTime = DateUtils.getCurrentMinuteTime(currentTime);
        mLock.lock();
        Level head = mMaxLevels.size() > 0 ? mMaxLevels.get(0) : null;
        mLock.unlock();
        long lastTime = head != null ? head.timestamp : 0;
        Log.d("xh", "currentTime " + mDateFormat.format(currentTime) + " currentMinuteTime " + mDateFormat.format(currentMinuteTime) + " lastTime " + mDateFormat.format(lastTime) + " duration " + (currentTime - lastTime));
        if (currentTime - lastTime < X_UNIT) {
            mMaxLevelCurrentMinute = Math.max(mMaxLevelCurrentMinute, level);
        } else {
            mLock.lock();
            mMaxLevels.add(new Level(mMaxLevelCurrentMinute, currentMinuteTime));
            mLock.unlock();
            Log.d("xh", "mMaxLevelCurrentMinute " + mMaxLevelCurrentMinute);
            mMaxLevelCurrentMinute = level;
        }
    }

    @Override
    public void onReceiveBandLevel(List<FrequencyLevel> levels) {

    }

    private void init() {
        mBarPaint = new Paint();
        mBarPaint.setColor(Color.BLACK);
        mBarPaint.setAntiAlias(true);
        mBarPaint.setStyle(Paint.Style.FILL);
        mBarPaint.setStrokeWidth(1);

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
    }
}
