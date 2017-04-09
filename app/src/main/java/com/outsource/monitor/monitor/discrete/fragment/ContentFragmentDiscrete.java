package com.outsource.monitor.monitor.discrete.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
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
import com.outsource.monitor.monitor.base.parser.MScanParser48278;
import com.outsource.monitor.monitor.base.ui.BasePlayFragment;
import com.outsource.monitor.monitor.discrete.DiscreteDataReceiver;
import com.outsource.monitor.monitor.discrete.widget.MScanFallsLevelView;
import com.outsource.monitor.monitor.fscan.chartformatter.FscanXAxisValueFormatter;
import com.outsource.monitor.monitor.fscan.chartformatter.FscanYAxisValueFormatter;
import com.outsource.monitor.monitor.ifpan.model.FallRow;
import com.outsource.monitor.utils.CollectionUtils;
import com.outsource.monitor.utils.DisplayUtils;
import com.outsource.monitor.utils.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by xionghao on 2017/3/5.
 */

public class ContentFragmentDiscrete extends BasePlayFragment implements DiscreteDataReceiver {

    private float startFreqency = Float.MAX_VALUE;
    private float endFrequency;

    public static BasePlayFragment newInstance() {
        return new ContentFragmentDiscrete();
    }

    private AtomicReference<MScanParser48278.DataHead> mDataHead = new AtomicReference<>();
    private List<Float> mFrequencyList = new ArrayList<>();
    private AtomicReference<Map<Float, Float>> mCurrentData = new AtomicReference<>();
    private ConcurrentLinkedQueue<FallRow> mFallRows = new ConcurrentLinkedQueue<>();
    //瀑布图要显示100秒的数据，如果每条数据都显示的话数据量太大，所以每次把1秒内的数据取平均值合并成一条
    private CombinedChart mChart;
    private MScanFallsLevelView mFallsLevelView;
    private static final long REFRESH_CHART_INTERVAL = 100;
    private static final int MSG_ID_REFRESH_CHART = 1;

    private boolean showLineChart = false;

    public static float DISPLAY_Y_DELTA = 46;//柱状图显示都是从0开始，底下的负数不会填充，所以将所有电平值都加50，保证大于0

    private Handler mRefreshHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_ID_REFRESH_CHART) {
                if (isPlay()) {
                    if (showLineChart) {
                        refreshLineChart();
                    }
                    refreshBarChart();
                    CombinedData data = mChart.getData();
                    data.notifyDataChanged();
                    mChart.notifyDataSetChanged();
                    mChart.invalidate();
                }
                mRefreshHandler.sendEmptyMessageDelayed(MSG_ID_REFRESH_CHART, REFRESH_CHART_INTERVAL);
            }
        }

    };

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_content_mscan, null);
        initCombineChart(view);
        initFallChart(view);
        mRefreshHandler.sendEmptyMessageDelayed(MSG_ID_REFRESH_CHART, 500);

//        if (!isPlay()) {
//            mFallsLevelView.pause();
//        }
        return view;
    }

    private void onFrequencyBandSelected(float startFreq, float endFreq) {
        XAxis xAxis = mChart.getXAxis();
        xAxis.setAxisMinimum(startFreq);
        xAxis.setAxisMaximum(endFreq);
    }

    private void initCombineChart(View view) {
        mChart = (CombinedChart) view.findViewById(R.id.chart_mscan);
        mChart.getDescription().setEnabled(false);
        mChart.setBackgroundColor(Color.TRANSPARENT);
        mChart.setDrawGridBackground(false);
        mChart.setDrawBarShadow(false);
        mChart.setHighlightFullBarEnabled(false);

        // 绘制层次，draw bars behind lines
        mChart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE
        });

        mChart.getAxisRight().setEnabled(false);

        YAxis leftAxis = mChart.getAxisLeft();
        FscanYAxisValueFormatter formatter = new FscanYAxisValueFormatter();
        leftAxis.setValueFormatter(formatter);
        leftAxis.enableGridDashedLine(10, 10, 0);
        leftAxis.setAxisLineColor(Color.parseColor("#403f40"));
        leftAxis.setTextColor(Color.parseColor("#adadad"));
        leftAxis.setAxisMinimum(0);
        leftAxis.setAxisMaximum(80 + DISPLAY_Y_DELTA);

        XAxis xAxis = mChart.getXAxis();
        FscanXAxisValueFormatter xAxisValueFormatter = new FscanXAxisValueFormatter();
        xAxis.setValueFormatter(xAxisValueFormatter);
        xAxis.enableGridDashedLine(10, 10, 0);
        xAxis.setAxisLineColor(Color.parseColor("#403f40"));
        xAxis.setTextColor(Color.parseColor("#adadad"));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setGranularity(0f);

        CombinedData data = new CombinedData();
        data.setData(defaultBarData());
        if (showLineChart) {
            data.setData(defaultLineData());
        }

        mChart.setData(data);
        mChart.invalidate();
        mChart.getLegend().setEnabled(false);
    }

    private void initFallChart(View view) {
        mFallsLevelView = (MScanFallsLevelView) view.findViewById(R.id.fall_mscan);
        mFallsLevelView.start();
    }

    private LineData defaultLineData() {

        LineData d = new LineData();

        ArrayList<Entry> entries = new ArrayList<Entry>();
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

    private BarData defaultBarData() {
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        BarDataSet set1 = new BarDataSet(yVals1, "DataSet 1");

        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);
        data.setValueTextSize(10f);
        data.setValueTextColor(Color.parseColor("#403f40"));
        data.setOrientation(BarData.Orientation.VERTICAL);
        return data;
    }

    private void refreshLineChart() {
        MScanParser48278.DataHead head = mDataHead.get();
        Map<Float, Float> levelMap = mCurrentData.get();
        if (head == null || CollectionUtils.isEmpty(levelMap)) {
            return;
        }
        ArrayList<Entry> entries = new ArrayList<>();
        Iterator<Map.Entry<Float, Float>> iterator = levelMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Float, Float> entry = iterator.next();
            entries.add(new Entry(entry.getKey(),  entry.getValue() + DISPLAY_Y_DELTA));
        }
        LineDataSet set = new LineDataSet(entries, "Line DataSet");
        set.setColor(Color.rgb(240, 238, 70));
        set.setLineWidth(1f);
        set.setDrawCircleHole(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        CombinedData combinedData = mChart.getData();
        LineData lineData = combinedData.getLineData();
        lineData.removeDataSet(0);
        lineData.addDataSet(set);
        lineData.notifyDataChanged();
    }

    private void refreshBarChart() {
        MScanParser48278.DataHead head = mDataHead.get();
        Map<Float, Float> levelMap = mCurrentData.get();
        if (head == null || CollectionUtils.isEmpty(levelMap)) {
            return;
        }
        ArrayList<BarEntry> entries = new ArrayList<>();
        Iterator<Map.Entry<Float, Float>> iterator = levelMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Float, Float> entry = iterator.next();
            entries.add(new BarEntry(entry.getKey(),  entry.getValue() + DISPLAY_Y_DELTA));
        }
        float displaySpan = endFrequency - startFreqency;
        CombinedData data = mChart.getData();
        BarDataSet set = new BarDataSet(entries, "DataSet 1");
        BarData barData = data.getBarData();
        barData.setValueTextSize(10f);
        int count = levelMap.size();
        float barWidth = 0.9f * displaySpan / Math.max(count, 20);
        barData.setBarWidth(barWidth);
        barData.setOrientation(BarData.Orientation.VERTICAL);
        barData.removeDataSet(0);
        barData.addDataSet(set);
        barData.notifyDataChanged();
    }

    @Override
    public void onReceiveDiscreteData(MScanParser48278.DataValue msData) {
        if (msData == null || msData.valueList.size() == 0) {
            LogUtils.d("频段扫描帧头为空！");
            return;
        }
        ArrayList<MScanParser48278.DataValue.ValueItem> values = msData.valueList;
        List<Float> levels = new ArrayList<>(values.size());
        for (MScanParser48278.DataValue.ValueItem v : values) {
            levels.add(v.level);
        }
        MScanParser48278.DataValue.ValueItem v = msData.valueList.get(0);
        int index = v.freqIndex;
        if (index >= 0 && index < mFrequencyList.size()) {
            Map<Float, Float> data = mCurrentData.get();
            if (data == null) {
                data = new HashMap<>();
            }
            data.put(mFrequencyList.get(index), v.level);
            mCurrentData.set(data);
        }
        mFallsLevelView.onReceiveDiscreteData(msData);
    }

    @Override
    public void onReceiveDiscreteHead(MScanParser48278.DataHead msHead) {
        if (msHead == null || CollectionUtils.isEmpty(msHead.freqList)) {
            LogUtils.d("离散扫描接受数据为空！");
            return;
        }
        mFallsLevelView.onReceiveDiscreteHead(msHead);
        List<Long> frequencyList = msHead.freqList;
        for (Long freq : frequencyList) {
            float frequency = DisplayUtils.toDisplayFrequency(freq);
            startFreqency = Math.min(startFreqency, frequency);
            endFrequency = Math.max(endFrequency, frequency);
            mFrequencyList.add(frequency);
        }
        mDataHead.set(msHead);
        mRefreshHandler.post(new Runnable() {
            @Override
            public void run() {
                onFrequencyBandSelected(startFreqency, endFrequency);
            }
        });
    }
}
