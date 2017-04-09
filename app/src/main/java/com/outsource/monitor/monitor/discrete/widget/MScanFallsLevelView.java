package com.outsource.monitor.monitor.discrete.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.outsource.monitor.monitor.base.event.PlayPauseEvent;
import com.outsource.monitor.monitor.base.parser.MScanParser48278;
import com.outsource.monitor.monitor.discrete.DiscreteDataReceiver;
import com.outsource.monitor.monitor.ifpan.model.FallRow;
import com.outsource.monitor.utils.CollectionUtils;
import com.outsource.monitor.utils.DisplayUtils;
import com.outsource.monitor.utils.LogUtils;
import com.outsource.monitor.utils.Utils;
import com.outsource.monitor.widget.BaseTextureView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Administrator on 2016/10/6.
 */
public class MScanFallsLevelView extends BaseTextureView implements DiscreteDataReceiver {

    private static final int X_AXIS_HEIGHT = DisplayUtils.dp2px(15);//x轴刻度区域的高度
    private static final int Y_AXIS_WIDTH = DisplayUtils.dp2px(15);//y轴刻度区域的宽度
    private static float X_CELL_COUNT = 10;//x轴有多少个网格
    private static final float Y_CELL_COUNT = 10;//y轴有多少个网格
    private static int LINE_HEIGHT = 1;

    private int chartWidth;
    private Paint mLevelPaint;
    private Paint mXyAxisPaint;
    private Paint mMarkPaint;
    private Paint mMarkTextPaint;

    private int mMarkTextHeight;

    public static final int FALL_COUNT = 50;//最大显示的横线数量
    private static final long FALL_YAXIS_UNIT = 50;//瀑布图两行之间的最小时间间隔
    private ConcurrentLinkedQueue<FallRow> mFallRows = new ConcurrentLinkedQueue<>();
    //瀑布图要显示100秒的数据，如果每条数据都显示的话数据量太大，所以每次把1秒内的数据取平均值合并成一条
    private FallRow mAverageFallRow;
    private int averageCount;//当前的平均值是由多少条数据算出来的
    private AtomicReference<MScanParser48278.DataHead> mHead = new AtomicReference<>();
    private float startFreqency = Float.MAX_VALUE;
    private float endFrequency;
    private List<Float> mFrequencyList = new ArrayList<>();
    private List<Float> mLevles = new ArrayList<>();

    public MScanFallsLevelView(Context context) {
        super(context);
        init();
    }

    public MScanFallsLevelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void drawCanvas(Canvas canvas) {
        int rowCount = mFallRows.size();
        int index = 0;
        for (FallRow row : mFallRows) {
            float y = LINE_HEIGHT * (rowCount - index - 1);
            List<Float> levels = row.mValues;
            int size = levels.size();
            int start = Y_AXIS_WIDTH;
            float distance = chartWidth / (float) size;
            for (Float level : levels) {
                mLevelPaint.setColor(Utils.level2Color(level));
                canvas.drawLine(start, y, start + distance, y, mLevelPaint);
                start += distance;
            }
            index++;
        }

        canvas.drawLine(Y_AXIS_WIDTH - DisplayUtils.dp2px(1), mHeight - X_AXIS_HEIGHT, mWidth, mHeight - X_AXIS_HEIGHT, mXyAxisPaint);//x轴刻度线
        canvas.drawLine(Y_AXIS_WIDTH - DisplayUtils.dp2px(1), mHeight - X_AXIS_HEIGHT, Y_AXIS_WIDTH, 0, mXyAxisPaint);//y轴刻度线

        int xUnitWidth = (int) ((mWidth - Y_AXIS_WIDTH) / X_CELL_COUNT);
        float span = endFrequency - startFreqency;
        //画x轴底部刻度值和垂直网格线
        for (int i = 0; i <= X_CELL_COUNT; i++) {
            int x = Y_AXIS_WIDTH + xUnitWidth * i;

            String xMarkStr = null;
            if (i < mFrequencyList.size()) {
                xMarkStr = String.format("%.1fMHz", mFrequencyList.get(i));
            }
            if (i < X_CELL_COUNT && !TextUtils.isEmpty(xMarkStr)) {
                float textWidth = mMarkTextPaint.measureText(xMarkStr);
                canvas.drawText(xMarkStr, 0, xMarkStr.length(), x + xUnitWidth / 2 - textWidth / 2 , mHeight - mMarkTextHeight, mMarkTextPaint);
            }

            canvas.drawLine(x, mHeight - X_AXIS_HEIGHT, x, 0, mMarkPaint);
        }

        //画y轴左侧刻度值和水平网格线
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


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayPauseEvent(PlayPauseEvent event) {
        if (event.isPlay) {
            start();
        } else {
            pause();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
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
                mAverageFallRow = null;
                averageCount = 0;
            } else {
                calcNewAverageRow(row);
                averageCount++;
            }
        }
    }

    private void addFallRow(FallRow averageFallRow) {
        mFallRows.add(averageFallRow);
        if (mFallRows.size() > FALL_COUNT) {
            mFallRows.poll();
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

    private float getDisplaySpan(long realSpan) {
        return  realSpan / (float) 1000;
    }

    @Override
    public void onReceiveDiscreteData(MScanParser48278.DataValue msData) {
        if (msData == null || CollectionUtils.isEmpty(msData.valueList)) {
            LogUtils.d("中频分析接受数据为空！");
            return;
        }
        ArrayList<MScanParser48278.DataValue.ValueItem> values = msData.valueList;
        MScanParser48278.DataValue.ValueItem v = values.get(0);
        int freqIndex = v.freqIndex;
        if (freqIndex >= 0 && freqIndex < mLevles.size()) {
            mLevles.set(freqIndex, v.level);
        }
        List<Float> levels = new ArrayList<>();
        levels.addAll(mLevles);
        updateFallLevels(new FallRow(System.currentTimeMillis(), levels));
    }

    @Override
    public void onReceiveDiscreteHead(MScanParser48278.DataHead msHead) {
        if (msHead == null || msHead.freqList.size() == 0) {
            LogUtils.d("离散扫描帧头为空！");
            return;
        }
        mHead.set(msHead);
        for (Long freq : msHead.freqList) {
            float frequency = DisplayUtils.toDisplayFrequency(freq);
            startFreqency = Math.min(startFreqency, frequency);
            endFrequency = Math.max(endFrequency, frequency);
            mFrequencyList.add(frequency);
            mLevles.add(0f);
        }
        X_CELL_COUNT = msHead.freqList.size();
    }
}
