package com.outsource.monitor.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.Toast;

import com.github.mikephil.charting.buffer.ColorBuffer;
import com.github.mikephil.charting.components.XAxis;
import com.outsource.monitor.ifpan.model.FallRow;
import com.outsource.monitor.model.FrequencyLevel;
import com.outsource.monitor.parser.IfpanParser48278;
import com.outsource.monitor.service.DataReceiver;
import com.outsource.monitor.service.IfpanDataReceiver;
import com.outsource.monitor.utils.CollectionUtils;
import com.outsource.monitor.utils.DateUtils;
import com.outsource.monitor.utils.DisplayUtils;
import com.outsource.monitor.utils.LogUtils;
import com.outsource.monitor.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Administrator on 2016/10/6.
 */
public class FallsLevelView extends BaseTextureView implements IfpanDataReceiver {

    private static final int LINE_HEIGHT = DisplayUtils.dp2px(1);//横线高度
    private static int LINE_COUNT;//一屏最大显示的横线数量
    private float mMinFrequency;//最低频率
    private float mMaxFrequency;//最高频率
    private static float FREQUENCY_BAND = 1;//频率差
    private static float FREQUENCY_PER_PX = 1;//1像素占的频率值
    private static final int X_AXIS_HEIGHT = DisplayUtils.dp2px(15);//x轴刻度区域的高度
    private static final int Y_AXIS_WIDTH = DisplayUtils.dp2px(15);//y轴刻度区域的宽度
    private static final float X_UNIT = 10;//x轴一个网格占多少频率
    private static final float Y_UNIT = 10;//y轴一个网格占多少条线

    private Paint mLevelPaint;
    private Paint mXyAxisPaint;
    private Paint mMarkPaint;
    private Paint mMarkTextPaint;

    private int mMarkTextHeight;

    public ConcurrentLinkedQueue<List<FrequencyLevel>> mLevels = new ConcurrentLinkedQueue<>();

    private static final long FALL_YAXIS_UNIT = 1000;//瀑布图两行之间的最小时间间隔
    private ConcurrentLinkedQueue<FallRow> mFallRows = new ConcurrentLinkedQueue<>();
    //瀑布图要显示100秒的数据，如果每条数据都显示的话数据量太大，所以每次把1秒内的数据取平均值合并成一条
    private FallRow mAverageFallRow;
    private int averageCount;//当前的平均值是由多少条数据算出来的

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
        canvas.drawLine(Y_AXIS_WIDTH - DisplayUtils.dp2px(1), mHeight - X_AXIS_HEIGHT, mWidth, mHeight - X_AXIS_HEIGHT, mXyAxisPaint);//x轴刻度线
        canvas.drawLine(Y_AXIS_WIDTH - DisplayUtils.dp2px(1), mHeight - X_AXIS_HEIGHT, Y_AXIS_WIDTH, 0, mXyAxisPaint);//y轴刻度线

        int xUnitCount = (int) (FREQUENCY_BAND / X_UNIT);
        if (xUnitCount == 0) return;
        int xUnitWidth = (mWidth - Y_AXIS_WIDTH) / xUnitCount;
        //画x轴底部刻度值和垂直网格线
        for (int i = 0; i <= xUnitCount; i++) {
            int xValue = (int) (mMinFrequency + i * X_UNIT);
            int x = Y_AXIS_WIDTH + xUnitWidth * i;
            String xMarkStr = String.valueOf(xValue);
            float textWidth = mMarkTextPaint.measureText(xMarkStr);
            canvas.drawText(xMarkStr, 0, xMarkStr.length(), x - textWidth / 2 , mHeight - mMarkTextHeight, mMarkTextPaint);
            canvas.drawLine(x, mHeight - X_AXIS_HEIGHT, x, 0, mMarkPaint);
        }

        //画x轴底部刻度值和水平网格线
        int yUnitCount = (int) (LINE_COUNT / Y_UNIT);
        if (yUnitCount == 0) return;
        int yUnitHeight = (mHeight - X_AXIS_HEIGHT) / yUnitCount;
        for (int i = 0; i <= yUnitCount; i++) {
            int y = mHeight - X_AXIS_HEIGHT - (i + 1) * yUnitHeight;
            canvas.drawLine(Y_AXIS_WIDTH, y, mWidth, y, mMarkPaint);
        }

        int y =  (mLevels.size() - 1) * LINE_HEIGHT;
        for (List<FrequencyLevel> list : mLevels) {
            int start = Y_AXIS_WIDTH;
            for (FrequencyLevel level : list) {
                int distance = (int) ((level.frequency - mMinFrequency) / FREQUENCY_PER_PX);
                mLevelPaint.setColor(Utils.level2Color(level.level));
                canvas.drawLine(start, y, start + distance, y, mLevelPaint);
                start += distance;
            }
            y -= LINE_HEIGHT;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        LINE_COUNT = (mHeight - X_AXIS_HEIGHT) / LINE_HEIGHT;
        calcFrequencyDimension();
    }


    @Override
    public void onReceiveItuData(float[] ituData) {
    }

    @Override
    public void onReceiveBandLevel(List<FrequencyLevel> levels) {
        mLevels.add(levels);
        if (mLevels.size() > LINE_COUNT) {
            mLevels.poll();
        }
    }

    /**
     * 设置频率区间
     * @param minFrequency
     * @param maxFrequency
     */
    public void setFrequencyBand(int minFrequency, int maxFrequency) {
        mMinFrequency = minFrequency;
        mMaxFrequency = maxFrequency;
        FREQUENCY_BAND = maxFrequency - minFrequency;
        if (FREQUENCY_BAND > 0) {
            calcFrequencyDimension();
        } else {
            Toast.makeText(getContext(), "最高频率要大于最低频率", Toast.LENGTH_SHORT).show();
        }
    }

    private void calcFrequencyDimension() {
        if (mWidth > 0 && FREQUENCY_BAND > 0) {
            FREQUENCY_PER_PX = FREQUENCY_BAND / (mWidth - Y_AXIS_WIDTH);
        }
    }

    private void init() {
        mLevelPaint = new Paint();
        mLevelPaint.setStyle(Paint.Style.FILL);
        mLevelPaint.setStrokeWidth(LINE_HEIGHT);

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

    @Override
    public void onReceiveIfpanData(IfpanParser48278.DataValue ifpanData) {
        if (ifpanData == null || CollectionUtils.isEmpty(ifpanData.levelList)) {
            LogUtils.d("中频分析接受数据为空！");
            return;
        }
        if (ColorBuffer.valueCount == 0) {
            ColorBuffer.valueCount = ifpanData.dotCount;
        }
        mCurrentData.set(ifpanData.levelList);
        refreshFallLevels(new FallRow(System.currentTimeMillis(), ifpanData.levelList));
    }

    @Override
    public void onReceiveIfpanHead(IfpanParser48278.DataHead ifpanHeads) {
        if (ifpanHeads == null) {
            LogUtils.d("中频分析帧头为空！");
            return;
        }
        mDataHead.set(ifpanHeads);
        float axisSpan = getDisplaySpan(ifpanHeads.span);

        XAxis fallXAxis = mFallChart.getXAxis();
        fallXAxis.setAxisMinimum(0);
        fallXAxis.setAxisMaximum(axisSpan);
    }

    private void refreshFallLevels(FallRow row) {
        if (mAverageFallRow == null) {
            mAverageFallRow = row;
            averageCount++;
        } else {
            long lastTime = mAverageFallRow.timestamp;
            if (row.timestamp - lastTime > FALL_YAXIS_UNIT) {
                calcNewAverageRow(row);
                addFallRow(mAverageFallRow);
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
            if (averageFallRow.timestamp - head.timestamp > BAR_COUNT * FALL_YAXIS_UNIT) {
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
        if (head == null || (System.currentTimeMillis() - head.timestamp < BAR_COUNT * FALL_YAXIS_UNIT)) {
            return;
        }
        Iterator<FallRow> iterator = mFallRows.iterator();
        while (iterator.hasNext()) {
            FallRow row = iterator.next();
            if (currentTime - row.timestamp > BAR_COUNT * FALL_YAXIS_UNIT) {
                iterator.remove();
            }
        }
    }

}
