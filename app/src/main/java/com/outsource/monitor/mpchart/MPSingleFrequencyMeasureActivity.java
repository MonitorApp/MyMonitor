
package com.outsource.monitor.mpchart;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.outsource.monitor.R;
import com.outsource.monitor.config.Consts;
import com.outsource.monitor.config.DeviceConfig;
import com.outsource.monitor.model.FrequencyLevel;
import com.outsource.monitor.service.DataProviderService;
import com.outsource.monitor.service.DataReceiver;
import com.outsource.monitor.service.ServiceHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MPSingleFrequencyMeasureActivity extends FragmentActivity implements DataReceiver {

    private ServiceHelper mServiceHelper;
    private static final int MAX_LINE_X_AXIS = Consts.MINUTE / DeviceConfig.DATA_SEND_INTERVAL;
    private static final int MIN_MEASURE_LEVEL = 0;
    private static final int MAX_MEASURE_LEVEL = 100;
    private static final int MAX_BAR_X_AXIS = 60;
    private static final float BAR_WIDTH = 0.7f;
    private LineChart mLineChart;
    private BarChart mTimeBarChart;
    private BarChart mMaxLevelBarChart;
    private ConcurrentLinkedQueue<Float> mRealTimeLevels = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Float> mTimePercentages = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Float> mMaxLevels = new ConcurrentLinkedQueue<>();
    private int mHitCount;
    private float mMaxLevel = DeviceConfig.MIN_LEVEL;
    private int currentMillis;

    private static final long REFRESH_LINE_INTERVAL = 100;
    private static final long REFRESH_BAR_INTERVAL = Consts.MINUTE;
    private static final int MSG_ID_REFRESH_LINE = 1;
    private static final int MSG_ID_REFRESH_BAR = 2;

    private Handler mRefreshHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_ID_REFRESH_LINE) {
                refreshLineChart();
                mRefreshHandler.sendEmptyMessageDelayed(MSG_ID_REFRESH_LINE, REFRESH_LINE_INTERVAL);
            } else if (msg.what == MSG_ID_REFRESH_BAR) {
                refreshTimeBarChart();
                refreshMaxLevelBarChart();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.frag_mp_single_frequency_measure);

        initLineChart();
        initTimeBarChart();
        initMaxLevelBarChart();
        initService();

        mRefreshHandler.sendEmptyMessageDelayed(MSG_ID_REFRESH_LINE, REFRESH_LINE_INTERVAL);
        mRefreshHandler.sendEmptyMessageDelayed(MSG_ID_REFRESH_BAR, REFRESH_BAR_INTERVAL);
    }

    private void initLineChart() {
        mLineChart = (LineChart) findViewById(R.id.chart_single_frequency_line);
        YAxis leftAxis = mLineChart.getAxisLeft();
        leftAxis.enableGridDashedLine(10, 10, 0);
        leftAxis.setAxisMinimum(DeviceConfig.MIN_LEVEL);
        leftAxis.setAxisMaximum(DeviceConfig.MAX_LEVEL);

        XAxis xAxis = mLineChart.getXAxis();
        xAxis.enableGridDashedLine(10, 10, 0);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(MAX_LINE_X_AXIS);

        LineData lineData = new LineData(generateLineDataSet());
        mLineChart.setData(lineData);
        mLineChart.invalidate();
    }

    private void initTimeBarChart() {
        mTimeBarChart = (BarChart) findViewById(R.id.chart_single_frequency_time_percentage);
        YAxis leftAxis = mTimeBarChart.getAxisLeft();
        leftAxis.enableGridDashedLine(10, 10, 0);
        leftAxis.setAxisMinimum(0);
        leftAxis.setAxisMaximum(100);

        XAxis xAxis = mTimeBarChart.getXAxis();
        xAxis.enableGridDashedLine(1, 1, 0);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(60);

        BarData barData = new BarData(generateTimeBarData());
        barData.setBarWidth(BAR_WIDTH);
        mTimeBarChart.setData(barData);
        mTimeBarChart.invalidate();
    }

    private void initMaxLevelBarChart() {
        mMaxLevelBarChart = (BarChart) findViewById(R.id.chart_single_frequency_max_level);
        YAxis leftAxis = mMaxLevelBarChart.getAxisLeft();
        leftAxis.enableGridDashedLine(10, 10, 0);
        leftAxis.setAxisMinimum(0);
        leftAxis.setAxisMaximum(100);

        XAxis xAxis = mMaxLevelBarChart.getXAxis();
        xAxis.enableGridDashedLine(1, 1, 0);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(60);

        BarData barData = new BarData(generateMaxLevelBarData());
        barData.setBarWidth(BAR_WIDTH);
        mMaxLevelBarChart.setData(barData);
        mMaxLevelBarChart.invalidate();
    }

    private void initService() {
        mServiceHelper = new ServiceHelper();
        mServiceHelper.setOnServiceConnectListener(new ServiceHelper.OnServiceConnectListener() {
            @Override
            public void onServiceConnected(DataProviderService.SocketBinder service) {
                service.addDataReceiver(MPSingleFrequencyMeasureActivity.this);
                service.setFrequencyRange(DeviceConfig.MIN_FREQUENCY, DeviceConfig.MAX_FREQUENCY);
                service.setLevelRange(DeviceConfig.MIN_LEVEL, DeviceConfig.MAX_LEVEL);
            }
        });
        mServiceHelper.bindService(this);
    }

    private boolean isHit(float level) {
        return level >= MIN_MEASURE_LEVEL && level <= MAX_MEASURE_LEVEL;
    }

    @Override
    public void onReceiveItuData(float[] ituData) {
        float level = ituData[0];
        int size = mRealTimeLevels.size();
        if (size >= MAX_LINE_X_AXIS) {
            mRealTimeLevels.poll();
        }
        mRealTimeLevels.add(level);

        mMaxLevel = Math.max(level, mMaxLevel);
        if (isHit(level)) {
            mHitCount++;
        }
        if (currentMillis >= Consts.MINUTE - DeviceConfig.DATA_SEND_INTERVAL) {
            int timeSize = mTimePercentages.size();
            if (timeSize >= MAX_BAR_X_AXIS) {
                mTimePercentages.poll();
            }
            mTimePercentages.add(mHitCount * 100f / MAX_LINE_X_AXIS);

            int maxLevelSize = mMaxLevels.size();
            if (maxLevelSize >= MAX_BAR_X_AXIS) {
                mMaxLevels.poll();
            }
            mMaxLevels.add(mMaxLevel);
            mHitCount = 0;
            currentMillis = 0;
            mMaxLevel = DeviceConfig.MIN_LEVEL;
            mRefreshHandler.sendEmptyMessage(MSG_ID_REFRESH_BAR);
        } else {
            currentMillis += DeviceConfig.DATA_SEND_INTERVAL;
        }
    }

    @Override
    public void onReceiveBandLevel(List<FrequencyLevel> levels) {
    }

    private LineDataSet generateLineDataSet() {
        ArrayList<Entry> entries = new ArrayList<Entry>();
        int size = mRealTimeLevels.size();
        int xAxis = size - 1;
        for (Float level : mRealTimeLevels) {
            entries.add(0, new Entry(xAxis, level));
            xAxis--;
        }
        LineDataSet set = new LineDataSet(entries, "Line DataSet");
        set.setColor(Color.rgb(240, 238, 70));
        set.setLineWidth(1f);
        set.setDrawCircleHole(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        return set;
    }

    private void refreshLineChart() {
        LineData lineData = mLineChart.getData();
        lineData.removeDataSet(0);
        lineData.addDataSet(generateLineDataSet());
        lineData.notifyDataChanged();
        mLineChart.notifyDataSetChanged();
        mLineChart.invalidate();
    }

    private BarDataSet generateTimeBarData() {
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        int size = mTimePercentages.size();
        int xAxis = size - 1;
        for (Float level : mTimePercentages) {
            yVals1.add(0, new BarEntry(xAxis + BAR_WIDTH / 2, level));
            xAxis--;
        }
        return new BarDataSet(yVals1, "DataSet 1");
    }

    private void refreshTimeBarChart() {
        BarData barData = mTimeBarChart.getData();
        barData.removeDataSet(0);
        barData.addDataSet(generateTimeBarData());
        barData.notifyDataChanged();
        mTimeBarChart.notifyDataSetChanged();
        mTimeBarChart.invalidate();
    }

    private BarDataSet generateMaxLevelBarData() {
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        int size = mMaxLevels.size();
        int xAxis = size - 1;
        for (Float level : mMaxLevels) {
            yVals1.add(0, new BarEntry(xAxis + BAR_WIDTH / 2, level));
            xAxis--;
        }
        return new BarDataSet(yVals1, "DataSet 1");
    }

    private void refreshMaxLevelBarChart() {
        BarData barData = mMaxLevelBarChart.getData();
        barData.removeDataSet(0);
        barData.addDataSet(generateMaxLevelBarData());
        barData.notifyDataChanged();
        mMaxLevelBarChart.notifyDataSetChanged();
        mMaxLevelBarChart.invalidate();
    }
}
