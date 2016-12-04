
package com.outsource.monitor.mpchart;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.outsource.monitor.R;
import com.outsource.monitor.model.FrequencyLevel;
import com.outsource.monitor.service.DataProviderService;
import com.outsource.monitor.service.DataReceiver;
import com.outsource.monitor.service.ServiceHelper;
import com.outsource.monitor.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

public class MPBandScanActivity extends FragmentActivity implements DataReceiver {

    private ServiceHelper mServiceHelper;
    private static final float MIN_LEVEL = -40;
    private static final float MAX_LEVEL = 120;
    private static final float LEVEL_RANGE = MAX_LEVEL - MIN_LEVEL;
    private static final float MIN_FREQUENCY = 90;
    private static final float MAX_FREQUENCY = 160;
    private static final float FREQUENCY_RANGE = MAX_FREQUENCY - MIN_FREQUENCY;
    private static final float BAR_HEIGHT = 2;
    private static final int BAR_COUNT = (int) (LEVEL_RANGE / BAR_HEIGHT);
    public ConcurrentLinkedQueue<List<FrequencyLevel>> mFallLevels = new ConcurrentLinkedQueue<>();
    private CombinedChart mChart;
    private final int itemCount = 100;
    private AtomicReference<LineDataSet> mCurrentFrequencyRange = new AtomicReference<>();
    private AtomicReference<BarDataSet> mFallData = new AtomicReference<>();
    private static final long REFRESH_INTERVAL = 100;
    private static final int MSG_ID_REFRESH = 1;

    private Handler mRefreshHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_ID_REFRESH) {
                refreshLineChart();
                refreshFallChart();
                CombinedData data = mChart.getData();
                data.notifyDataChanged();
                mChart.notifyDataSetChanged();
                mChart.invalidate();
                mRefreshHandler.sendEmptyMessageDelayed(MSG_ID_REFRESH, REFRESH_INTERVAL);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_mp_band_scan);

        mChart = (CombinedChart) findViewById(R.id.chart_band_scan);
        mChart.getDescription().setEnabled(false);
        mChart.setBackgroundColor(Color.BLACK);
        mChart.setDrawGridBackground(false);
        mChart.setDrawBarShadow(false);
        mChart.setHighlightFullBarEnabled(false);

        // 绘制层次，draw bars behind lines
        mChart.setDrawOrder(new DrawOrder[]{
                DrawOrder.BAR, DrawOrder.LINE
        });

        mChart.getAxisRight().setEnabled(false);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.enableGridDashedLine(10, 10, 0);
        leftAxis.setAxisMinimum(MIN_LEVEL - LEVEL_RANGE);
        leftAxis.setAxisMaximum(MAX_LEVEL);
        leftAxis.setAxisLineColor(Color.parseColor("#403f40"));
        leftAxis.setTextColor(Color.parseColor("#adadad"));

        XAxis xAxis = mChart.getXAxis();
        xAxis.enableGridDashedLine(10, 10, 0);
        xAxis.setAxisLineColor(Color.parseColor("#403f40"));
        xAxis.setTextColor(Color.parseColor("#adadad"));
        xAxis.setPosition(XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(MIN_FREQUENCY);
        xAxis.setAxisMaximum(MAX_FREQUENCY);
//        xAxis.setGranularity(0f);

        CombinedData data = new CombinedData();
        data.setData(generateHorizontalBarData());
        data.setData(generateLineData());

        mChart.setData(data);
        mChart.invalidate();

        mServiceHelper = new ServiceHelper(this);
        mServiceHelper.fetchService(new ServiceHelper.OnServiceConnectedListener() {
            @Override
            public void onServiceConnected(DataProviderService.SocketBinder service) {
                service.addDataReceiver(MPBandScanActivity.this);
                service.setFrequencyRange(MIN_FREQUENCY, MAX_FREQUENCY);
                service.setLevelRange(MIN_LEVEL, MAX_LEVEL);
            }
        });

        mRefreshHandler.sendEmptyMessageDelayed(MSG_ID_REFRESH, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mServiceHelper.release();
    }

    private LineData generateLineData() {

        LineData d = new LineData();

        ArrayList<Entry> entries = new ArrayList<Entry>();
//        for (int index = 0; index < itemCount; index++) {
//            entries.add(new Entry(MIN_FREQUENCY + FREQUENCY_RANGE * index / itemCount, getRandom(LEVEL_RANGE, MIN_LEVEL)));
//        }

        LineDataSet set = new LineDataSet(entries, "Line DataSet");
        set.setColor(Color.rgb(240, 238, 70));
        set.setLineWidth(1f);
        set.setDrawCircleHole(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        d.addDataSet(set);

        return d;
    }

    private BarData generateHorizontalBarData() {
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
//        int itemCount = (int) (LEVEL_RANGE / BAR_HEIGHT);
//        for (int i = 0; i < itemCount; i++) {
//            float yStart = MIN_LEVEL - LEVEL_RANGE;
//            yVals1.add(new BarEntry(yStart + LEVEL_RANGE * i / itemCount, getRandom(FREQUENCY_RANGE, MIN_FREQUENCY)));
//        }

        BarDataSet set1 = new BarDataSet(yVals1, "DataSet 1");

        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);
        data.setValueTextSize(10f);
        data.setBarWidth(BAR_HEIGHT);
        data.setOrientation(BarData.Orientation.HORIZONTAL);
        return data;
    }

    @Override
    public void onReceiveItuData(float[] ituData) {

    }

    @Override
    public void onReceiveBandLevel(List<FrequencyLevel> levels) {
        refreshLineData(levels);
        refreshHorizontalBarData(levels);
    }

    private void refreshLineData(List<FrequencyLevel> levels) {
        ArrayList<Entry> entries = new ArrayList<Entry>();
        for (FrequencyLevel level : levels) {
            entries.add(new Entry(level.frequency, level.level));
        }
        final LineDataSet set = new LineDataSet(entries, "Line DataSet");
        set.setColor(Color.rgb(240, 238, 70));
        set.setLineWidth(1f);
        set.setDrawCircleHole(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        mCurrentFrequencyRange.set(set);
    }

    private void refreshLineChart() {
        LineDataSet set = mCurrentFrequencyRange.get();
        if (set != null) {
            CombinedData data = mChart.getData();
            LineData lineData = data.getLineData();
            lineData.removeDataSet(0);
            lineData.addDataSet(set);
            lineData.notifyDataChanged();
        }
    }

    private void refreshHorizontalBarData(List<FrequencyLevel> levels) {
        mFallLevels.add(levels);
        if (mFallLevels.size() > BAR_COUNT) {
            mFallLevels.poll();
        }
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        int size = mFallLevels.size();
        int i = 0;
        for (List<FrequencyLevel> ls : mFallLevels) {
            int[] colors = new int[levels.size()];
            int j = 0;
            for (FrequencyLevel level : ls) {
                colors[j++] = Utils.level2Color(level.level);
            }
            yVals1.add(new BarEntry(MIN_LEVEL - (size - i++) * BAR_HEIGHT, MAX_FREQUENCY, colors));
        }

        final BarDataSet set1 = new BarDataSet(yVals1, "DataSet 1");
        mFallData.set(set1);
    }

    private void refreshFallChart() {
        BarDataSet set = mFallData.get();
        if (set != null) {
            CombinedData data = mChart.getData();
            BarData barData = data.getBarData();
            barData.removeDataSet(0);
            barData.addDataSet(set);
            barData.notifyDataChanged();
        }
    }
}
