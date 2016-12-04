package com.outsource.monitor.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.outsource.monitor.ifpan.model.FallRow;
import com.outsource.monitor.model.FrequencyLevel;
import com.outsource.monitor.parser.IfpanParser48278;
import com.outsource.monitor.service.IfpanDataReceiver;
import com.outsource.monitor.utils.CollectionUtils;
import com.outsource.monitor.utils.DisplayUtils;
import com.outsource.monitor.utils.LogUtils;
import com.outsource.monitor.utils.Utils;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Administrator on 2016/10/6.
 */
public class FallsLevelView extends BaseTextureView implements IfpanDataReceiver {

    private static final int X_AXIS_HEIGHT = DisplayUtils.dp2px(15);//x轴刻度区域的高度
    private static final int Y_AXIS_WIDTH = DisplayUtils.dp2px(15);//y轴刻度区域的宽度
    private static final float X_CELL_COUNT = 10;//x轴有多少个网格
    private static final float Y_CELL_COUNT = 10;//y轴有多少个网格
    private static int LINE_HEIGHT = 1;

    private int chartWidth;
    private Paint mLevelPaint;
    private Paint mXyAxisPaint;
    private Paint mMarkPaint;
    private Paint mMarkTextPaint;

    private int mMarkTextHeight;

    private static final int FALL_COUNT = 100;//最大显示的横线数量
    private static final long FALL_YAXIS_UNIT = 1000;//瀑布图两行之间的最小时间间隔
    private ConcurrentLinkedQueue<FallRow> mFallRows = new ConcurrentLinkedQueue<>();
    //瀑布图要显示100秒的数据，如果每条数据都显示的话数据量太大，所以每次把1秒内的数据取平均值合并成一条
    private FallRow mAverageFallRow;
    private int averageCount;//当前的平均值是由多少条数据算出来的
    private float span;//跨距
    private float frequency;

    public FallsLevelView(Context context) {
        super(context);
        init();
    }

    public FallsLevelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    void drawCanvas(Canvas canvas) {
        for (FallRow row : mFallRows) {
            float y = LINE_HEIGHT * (float) (System.currentTimeMillis() - row.timestamp) / FALL_YAXIS_UNIT;
            List<Float> levels = row.mValues;
            int size = levels.size();
            int start = Y_AXIS_WIDTH;
            float distance = chartWidth / (float) size;
            y = Math.min(y, mHeight - X_AXIS_HEIGHT - distance);
            for (Float level : levels) {
                mLevelPaint.setColor(Utils.level2Color(level));
                canvas.drawLine(start, y, start + distance, y, mLevelPaint);
                start += distance;
            }
        }

        canvas.drawLine(Y_AXIS_WIDTH - DisplayUtils.dp2px(1), mHeight - X_AXIS_HEIGHT, mWidth, mHeight - X_AXIS_HEIGHT, mXyAxisPaint);//x轴刻度线
        canvas.drawLine(Y_AXIS_WIDTH - DisplayUtils.dp2px(1), mHeight - X_AXIS_HEIGHT, Y_AXIS_WIDTH, 0, mXyAxisPaint);//y轴刻度线

        int xUnitWidth = (int) ((mWidth - Y_AXIS_WIDTH) / X_CELL_COUNT);
        //画x轴底部刻度值和垂直网格线
        for (int i = 0; i <= X_CELL_COUNT; i++) {
            int xValue = (int) (frequency + i * (span / X_CELL_COUNT));
            int x = Y_AXIS_WIDTH + xUnitWidth * i;
            String xMarkStr = String.valueOf(xValue);
            float textWidth = mMarkTextPaint.measureText(xMarkStr);
            canvas.drawText(xMarkStr, 0, xMarkStr.length(), x - textWidth / 2 , mHeight - mMarkTextHeight, mMarkTextPaint);
            canvas.drawLine(x, mHeight - X_AXIS_HEIGHT, x, 0, mMarkPaint);
        }

        //画x轴底部刻度值和水平网格线
        int yUnitHeight = (int) ((mHeight - X_AXIS_HEIGHT) / Y_CELL_COUNT);
        for (int i = 0; i <= Y_CELL_COUNT; i++) {
            int y = mHeight - X_AXIS_HEIGHT - (i + 1) * yUnitHeight;
            canvas.drawLine(Y_AXIS_WIDTH, y, mWidth, y, mMarkPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        chartWidth = mWidth - Y_AXIS_WIDTH;
        LINE_HEIGHT = (mHeight - X_AXIS_HEIGHT) / FALL_COUNT;
        mLevelPaint.setStrokeWidth(LINE_HEIGHT);
    }

    private void init() {
        mLevelPaint = new Paint();
        mLevelPaint.setStyle(Paint.Style.FILL);

        mXyAxisPaint = new Paint();
        mXyAxisPaint.setColor(Color.parseColor("#403f40"));
        mXyAxisPaint.setAntiAlias(true);
        mXyAxisPaint.setStyle(Paint.Style.FILL);
        mXyAxisPaint.setStrokeWidth(DisplayUtils.dp2px(1f));

        mMarkPaint = new Paint();
        mMarkPaint.setColor(Color.parseColor("#403f40"));
        mMarkPaint.setAntiAlias(true);
        mMarkPaint.setStyle(Paint.Style.FILL);
        mMarkPaint.setStrokeWidth(1);

        mMarkTextPaint = new Paint();
        mMarkTextPaint.setColor(Color.parseColor("#403f40"));
        mMarkTextPaint.setTextSize(DisplayUtils.dp2px(11));
        Paint.FontMetrics fm = mMarkTextPaint.getFontMetrics();
        mMarkTextHeight = (int) ((fm.descent - fm.ascent) / 2) - DisplayUtils.dp2px(4);
    }

    @Override
    public void onReceiveIfpanData(IfpanParser48278.DataValue ifpanData) {
        if (ifpanData == null || CollectionUtils.isEmpty(ifpanData.levelList)) {
            LogUtils.d("中频分析接受数据为空！");
            return;
        }
        updateFallLevels(new FallRow(System.currentTimeMillis(), ifpanData.levelList));
    }

    @Override
    public void onReceiveIfpanHead(IfpanParser48278.DataHead ifpanHeads) {
        if (ifpanHeads == null) {
            LogUtils.d("中频分析帧头为空！");
            return;
        }
        span = getDisplaySpan(ifpanHeads.span);
//        frequency = ifpanHeads.frequence;
    }

    private void updateFallLevels(FallRow row) {
        if (mAverageFallRow == null) {
            mAverageFallRow = row;
            averageCount++;
        } else {
            long lastTime = mAverageFallRow.timestamp;
            if (row.timestamp - lastTime > FALL_YAXIS_UNIT) {
                calcNewAverageRow(row);
                addFallRow(mAverageFallRow);
//                removeExpiredFallRows();
                mAverageFallRow = null;
                averageCount = 0;
            } else {
                calcNewAverageRow(row);
                averageCount++;
            }
        }
    }

    private void addFallRow(FallRow averageFallRow) {
        long timestamp = averageFallRow.timestamp;
//        averageFallRow.timestamp = (timestamp / FALL_YAXIS_UNIT) * FALL_YAXIS_UNIT;//去掉毫秒，防止出现2条数据在同一行的现象
        mFallRows.add(averageFallRow);
        FallRow head = mFallRows.peek();
        if (head != null) {
            if (averageFallRow.timestamp - head.timestamp > FALL_COUNT * FALL_YAXIS_UNIT) {
                mFallRows.poll();
            }
        }
    }

    private void calcNewAverageRow(FallRow row) {
        List<Float> avgLevels = mAverageFallRow.mValues;
        int avgSize = avgLevels.size();
        int valueSize = row.mValues.size();
        for (int i = 0; i < valueSize; i++) {
            if (i < avgSize) {
                float resultLevel = (avgLevels.get(i) * averageCount + row.mValues.get(i)) / (averageCount + 1);
                avgLevels.set(i, resultLevel);
            } else {
                avgLevels.add(row.mValues.get(i));
            }
        }
    }

    private void removeExpiredFallRows() {
        long currentTime = System.currentTimeMillis();
        FallRow head = mFallRows.peek();
        if (head == null || (System.currentTimeMillis() - head.timestamp < FALL_COUNT * FALL_YAXIS_UNIT)) {
            return;
        }
        Iterator<FallRow> iterator = mFallRows.iterator();
        while (iterator.hasNext()) {
            FallRow row = iterator.next();
            if (currentTime - row.timestamp > FALL_COUNT * FALL_YAXIS_UNIT) {
                iterator.remove();
            }
        }
    }

    private float getDisplaySpan(long realSpan) {
        return  realSpan / (float) 1000;
    }

}
