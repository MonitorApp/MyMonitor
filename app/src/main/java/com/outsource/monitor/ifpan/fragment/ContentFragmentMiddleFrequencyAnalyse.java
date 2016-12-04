
package com.outsource.monitor.ifpan.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.github.mikephil.charting.buffer.ColorBuffer;
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
import com.github.mikephil.charting.data.HorizontalBarData;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.outsource.monitor.R;
import com.outsource.monitor.config.PreferenceKey;
import com.outsource.monitor.ifpan.chartformatter.IFPANXAxisValueFormatter;
import com.outsource.monitor.ifpan.model.FallRow;
import com.outsource.monitor.mpchart.DefaultBarChart;
import com.outsource.monitor.parser.Command;
import com.outsource.monitor.parser.IfpanParser48278;
import com.outsource.monitor.service.ConnectCallback;
import com.outsource.monitor.service.DataProviderService;
import com.outsource.monitor.service.IfpanDataReceiver;
import com.outsource.monitor.service.ServiceHelper;
import com.outsource.monitor.singlefrequency.chartformatter.ITUYAxisValueFormatter;
import com.outsource.monitor.utils.CollectionUtils;
import com.outsource.monitor.utils.LogUtils;
import com.outsource.monitor.utils.PreferenceUtils;
import com.outsource.monitor.utils.PromptUtils;
import com.outsource.monitor.utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

public class ContentFragmentMiddleFrequencyAnalyse extends Fragment implements IfpanDataReceiver {

    public static Fragment newInstance() {
        return new ContentFragmentMiddleFrequencyAnalyse();
    }

    private ServiceHelper mServiceHelper;
    private static final float BAR_HEIGHT = 1;
    private static final int BAR_COUNT = (int) (50 / BAR_HEIGHT);
    private static final long FALL_YAXIS_UNIT = 1000;//瀑布图两行之间的最小时间间隔
    private AtomicReference<IfpanParser48278.DataHead> mDataHead = new AtomicReference<>();
    private AtomicReference<List<Float>> mCurrentData = new AtomicReference<>();
    private ConcurrentLinkedQueue<FallRow> mFallRows = new ConcurrentLinkedQueue<>();
    //瀑布图要显示100秒的数据，如果每条数据都显示的话数据量太大，所以每次把1秒内的数据取平均值合并成一条
    private FallRow mAverageFallRow;
    private int averageCount;//当前的平均值是由多少条数据算出来的
    private CombinedChart mChart;
    private IFPANXAxisValueFormatter mXValueFormatter;
    private CombinedChart mFallChart;
    private static final long REFRESH_CHART_INTERVAL = 100;
    private static final long REFRESH_FALL_INTERVAL = 1000;
    private static final int MSG_ID_REFRESH_CHART = 1;
    private static final int MSG_ID_REFRESH_FALL = 2;

    private Handler mRefreshHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_ID_REFRESH_CHART) {
                refreshLineChart();
                refreshBarChart();
                CombinedData data = mChart.getData();
                data.notifyDataChanged();
                mChart.notifyDataSetChanged();
                mChart.invalidate();
                mRefreshHandler.sendEmptyMessageDelayed(MSG_ID_REFRESH_CHART, REFRESH_CHART_INTERVAL);
            } else if (msg.what == MSG_ID_REFRESH_FALL) {
                removeExpiredFallRows();
                refreshFallChart();
                CombinedData data = mFallChart.getData();
                data.notifyDataChanged();
                mFallChart.notifyDataSetChanged();
                mFallChart.invalidate();
                mRefreshHandler.sendEmptyMessageDelayed(MSG_ID_REFRESH_FALL, REFRESH_FALL_INTERVAL);
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
        View view = inflater.inflate(R.layout.fragment_content_fragment_middle_frequency_analyse, null);
        mXValueFormatter = new IFPANXAxisValueFormatter();
        initCombineChart(view);
        initFallChart(view);
        initService();
        mRefreshHandler.sendEmptyMessageDelayed(MSG_ID_REFRESH_CHART, 500);
        mRefreshHandler.sendEmptyMessageDelayed(MSG_ID_REFRESH_FALL, 1500);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        DataProviderService.SocketBinder service = mServiceHelper.getService();
        if (service != null) {
            service.disconnect();
        }
    }

    private void initCombineChart(View view) {
        mChart = (CombinedChart) view.findViewById(R.id.chart_middle_frequency_analyse);
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
        ITUYAxisValueFormatter formatter = new ITUYAxisValueFormatter();
        leftAxis.setValueFormatter(formatter);
        leftAxis.enableGridDashedLine(10, 10, 0);
        leftAxis.setAxisLineColor(Color.parseColor("#403f40"));
        leftAxis.setTextColor(Color.parseColor("#adadad"));

        XAxis xAxis = mChart.getXAxis();
        xAxis.setValueFormatter(mXValueFormatter);
        xAxis.enableGridDashedLine(10, 10, 0);
        xAxis.setAxisLineColor(Color.parseColor("#403f40"));
        xAxis.setTextColor(Color.parseColor("#adadad"));
        xAxis.setPosition(XAxisPosition.BOTTOM);
//        xAxis.setGranularity(0f);

        CombinedData data = new CombinedData();
        data.setData(defaultBarData());
        data.setData(defaultLineData());

        mChart.setData(data);
        mChart.invalidate();
    }

    private void initFallChart(View view) {
        mFallChart = (CombinedChart) view.findViewById(R.id.fall_middle_frequency_analyse);
        mFallChart.getDescription().setEnabled(false);
        mFallChart.setBackgroundColor(Color.BLACK);
        mFallChart.setDrawGridBackground(false);
        mFallChart.setDrawBarShadow(false);
        mFallChart.setHighlightFullBarEnabled(false);
        mFallChart.getAxisLeft().setEnabled(false);
        mFallChart.getAxisRight().setEnabled(false);

        YAxis leftAxis = mFallChart.getAxisLeft();
        leftAxis.enableGridDashedLine(10, 10, 0);
        leftAxis.setAxisLineColor(Color.parseColor("#403f40"));
        leftAxis.setTextColor(Color.parseColor("#adadad"));
        leftAxis.setAxisMinimum(0);
        leftAxis.setAxisMaximum(BAR_COUNT);
        leftAxis.setGranularity(0);
        leftAxis.setGranularityEnabled(false);

        XAxis xAxis = mFallChart.getXAxis();
        xAxis.setValueFormatter(mXValueFormatter);
        xAxis.enableGridDashedLine(10, 10, 0);
        xAxis.setAxisLineColor(Color.parseColor("#403f40"));
        xAxis.setTextColor(Color.parseColor("#adadad"));
        xAxis.setPosition(XAxisPosition.BOTTOM);
        xAxis.setGranularity(0f);

        CombinedData data = new CombinedData();
        data.setData(defaultHorizontalBarData());
        mFallChart.setTouchEnabled(false);
        mFallChart.setDrawBarShadow(false);
        mFallChart.setHighlightFullBarEnabled(false);
        mFallChart.setAutoScaleMinMaxEnabled(false);
        mFallChart.setScaleEnabled(false);
        mFallChart.setData(data);
        mFallChart.invalidate();
    }

    private void initService() {
        mServiceHelper = new ServiceHelper();
        mServiceHelper.setOnServiceConnectListener(new ServiceHelper.OnServiceConnectListener() {
            @Override
            public void onServiceConnected(final DataProviderService.SocketBinder service) {
                service.addIfpanDataReceiver(ContentFragmentMiddleFrequencyAnalyse.this);
                String ip = PreferenceUtils.getString(PreferenceKey.DEVICE_IP);
                int port = PreferenceUtils.getInt(PreferenceKey.DEVICE_PORT);
                service.connect(ip, port, new ConnectCallback() {
                    @Override
                    public void onConnectSuccess() {
                        String cmd = "RMTP:IFANALYSIS:4403000100113:frequency:98.1MHz\nifbw:30kHz\nspan:15kHz\nrecordthreshold:=40\ndemodmode:FM\n#";
                        Command command = new Command(cmd, Command.Type.IFPAN);
                        service.sendCommand(command);
                    }

                    @Override
                    public void onConnectFail(String message) {
                        PromptUtils.showToast(message);
                    }
                });
            }
        });
        mServiceHelper.bindService(getActivity());
    }

    private LineData defaultLineData() {

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

    private BarData defaultBarData() {
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
        data.setValueTextColor(Color.parseColor("#403f40"));
        data.setOrientation(BarData.Orientation.VERTICAL);
        return data;
    }

    private HorizontalBarData defaultHorizontalBarData() {
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
//        int itemCount = (int) (LEVEL_RANGE / BAR_HEIGHT);
//        for (int i = 0; i < itemCount; i++) {
//            float yStart = MIN_LEVEL - LEVEL_RANGE;
//            yVals1.add(new BarEntry(yStart + LEVEL_RANGE * i / itemCount, getRandom(FREQUENCY_RANGE, MIN_FREQUENCY)));
//        }

        BarDataSet set1 = new BarDataSet(yVals1, "DataSet 1");

        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(set1);

        HorizontalBarData data = new HorizontalBarData(dataSets);
        data.setValueTextSize(10f);
        data.setBarWidth(BAR_HEIGHT * 1.3f);
        data.setOrientation(BarData.Orientation.HORIZONTAL);
        return data;
    }

    private void refreshLineChart() {
        IfpanParser48278.DataHead head = mDataHead.get();
        List<Float> values = mCurrentData.get();
        if (head == null || CollectionUtils.isEmpty(values)) {
            return;
        }
        ArrayList<Entry> entries = new ArrayList<Entry>();
        float displaySpan = getDisplaySpan(head.span);
        float start = -displaySpan / 2;
        int count = values.size();
        for (int i = 0; i < count; i++) {
            Float value = values.get(i);
            entries.add(new Entry(start + i * displaySpan / count, value));
        }
        LineDataSet set = new LineDataSet(entries, "Line DataSet");
        set.setColor(Color.rgb(240, 238, 70));
        set.setLineWidth(1f);
        set.setDrawCircleHole(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        CombinedData data = mChart.getData();
        LineData lineData = data.getLineData();
        lineData.removeDataSet(0);
        lineData.addDataSet(set);
        lineData.notifyDataChanged();
    }

    private void refreshBarChart() {
        IfpanParser48278.DataHead head = mDataHead.get();
        List<Float> values = mCurrentData.get();
        if (head == null || CollectionUtils.isEmpty(values)) {
            return;
        }
        ArrayList<BarEntry> entries = new ArrayList<BarEntry>();
        float displaySpan = getDisplaySpan(head.span);
        float start = -displaySpan / 2;
        int count = values.size();
        for (int i = 0; i < count; i++) {
            Float value = values.get(i);
            entries.add(new BarEntry(start + i * displaySpan / count, value));
        }
        CombinedData data = mChart.getData();
        BarDataSet set = new BarDataSet(entries, "DataSet 1");
        BarData barData = data.getBarData();
        barData.setValueTextSize(10f);
        barData.setBarWidth(displaySpan / count / 6);
        barData.setOrientation(BarData.Orientation.VERTICAL);
        barData.removeDataSet(0);
        barData.addDataSet(set);
        barData.notifyDataChanged();
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

    private void refreshFallChart() {
        IfpanParser48278.DataHead head = mDataHead.get();
        if (head == null || mFallRows.size() == 0) {
            return;
        }
        ArrayList<BarEntry> entries = new ArrayList<BarEntry>();
        for (FallRow row : mFallRows) {
            long duration = System.currentTimeMillis() - row.timestamp;
            int valueSize = row.mValues.size();
            int[] colors = new int[valueSize];
            for (int i = 0; i < valueSize; i++) {
                colors[i] = Utils.level2Color(row.mValues.get(i));
            }
            BarEntry entry = new BarEntry(BAR_COUNT - (float) duration / FALL_YAXIS_UNIT, getDisplaySpan(mDataHead.get().span), colors);
            entries.add(entry);
        }
        BarDataSet set = new BarDataSet(entries, "DataSet 1");

        CombinedData data = mFallChart.getData();
        HorizontalBarData barData = data.getHorizontalBarData();
        barData.removeDataSet(0);
        barData.addDataSet(set);
        barData.notifyDataChanged();
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
        XAxis xAxis = mChart.getXAxis();
        float axisSpan = getDisplaySpan(ifpanHeads.span);
        mXValueFormatter.setSpan(axisSpan);
        xAxis.setAxisMinimum(-axisSpan / 2);
        xAxis.setAxisMaximum(axisSpan / 2);

        XAxis fallXAxis = mFallChart.getXAxis();
        fallXAxis.setAxisMinimum(0);
        fallXAxis.setAxisMaximum(axisSpan);
    }

    private float getDisplaySpan(long realSpan) {
        return  realSpan / (float) 1000;
    }
}
