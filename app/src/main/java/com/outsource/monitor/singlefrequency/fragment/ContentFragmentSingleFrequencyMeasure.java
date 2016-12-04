package com.outsource.monitor.singlefrequency.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

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
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.outsource.monitor.R;
import com.outsource.monitor.config.Consts;
import com.outsource.monitor.config.DeviceConfig;
import com.outsource.monitor.config.PreferenceKey;
import com.outsource.monitor.parser.Command;
import com.outsource.monitor.parser.ItuParser48278;
import com.outsource.monitor.service.ConnectCallback;
import com.outsource.monitor.service.DataProviderService;
import com.outsource.monitor.service.ItuDataReceiver;
import com.outsource.monitor.service.ServiceHelper;
import com.outsource.monitor.singlefrequency.adapter.MeasureItemAdapter;
import com.outsource.monitor.singlefrequency.chartformatter.ITUXAxisTimeValueFormatter;
import com.outsource.monitor.singlefrequency.chartformatter.ITUXAxisValueFormatter;
import com.outsource.monitor.singlefrequency.chartformatter.ITUYAxisValueFormatter;
import com.outsource.monitor.singlefrequency.model.ItuItemData;
import com.outsource.monitor.singlefrequency.model.ItuLevel;
import com.outsource.monitor.utils.CollectionUtils;
import com.outsource.monitor.utils.LogUtils;
import com.outsource.monitor.utils.PreferenceUtils;
import com.outsource.monitor.utils.PromptUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 单频测量，上面曲线统计扫描的实时电平值，一屏最多显示2秒前的数据；
 * 每屏显示完了会统计一次数据，在下面两个柱状图里显示时间占用度（即电平区间命中率）和最大电平值
 * Created by xionghao on 2016/10/2.
 */
public class ContentFragmentSingleFrequencyMeasure extends Fragment implements ItuDataReceiver {

    public static Fragment newInstance() {
        return new ContentFragmentSingleFrequencyMeasure();
    }

    private ServiceHelper mServiceHelper;
    private static final int MAX_LINE_X_AXIS = Consts.SECONDS * 2;//最多显示2秒前的数据，单位为ms
    private static final int MIN_MEASURE_LEVEL = 0;
    private static final int MAX_MEASURE_LEVEL = 100;
    private static final int MAX_BAR_X_AXIS = 60;
    private static final float BAR_WIDTH = 0.7f;
    private LineChart mLineChart;
    private BarChart mTimeBarChart;
    private BarChart mMaxLevelBarChart;
    private TextView mTvMeasureItemName;
    private TextView mTvRealTimeLevel;
    private TextView mTvTimePercentageTitle;
    private TextView mTvMaxValueTitle;
    private AtomicReference<List<ItuParser48278.DataHead.HeadItem>> mItuHead = new AtomicReference<>();
    private List<ConcurrentLinkedQueue<ItuLevel>> mRealTimeLevels = new ArrayList<>();
    private List<ConcurrentLinkedQueue<Float>> mTimePercentages = new ArrayList<>();
    private List<ConcurrentLinkedQueue<Float>> mMaxLevels = new ArrayList<>();
    private List<Integer> hitCountPerScreen = new ArrayList<>();//一屏内命中次数
    private List<Integer> totalCountPerScreen = new ArrayList<>();//一屏内扫描的电平次数
    private List<Float> maxLevelPerScreen = new ArrayList<>();
    private MeasureItemAdapter mMeasureItemAdapter;
    private long frame;//当前接收到第几帧，用于计算平均值，长时间运行会导致溢出
    private List<ItuItemData> mCurrentItemsData = new ArrayList<>(0);
    private int choosePosition;
    private int measureItemCount;
    private long lastCollectTime = -1;

    private static final long REFRESH_LINE_INTERVAL = 100;
    private static final long REFRESH_BAR_INTERVAL = MAX_LINE_X_AXIS;
    private static final long REFRESH_MEASURE_ITEM_INTERVAL = Consts.SECONDS;
    private static final int MSG_ID_INIT_MEASURE_ITEM = 1;
    private static final int MSG_ID_REFRESH_LINE = 2;
    private static final int MSG_ID_REFRESH_BAR = 3;
    private static final int MSG_ID_REFRESH_MEASURE_ITEM = 4;
    private long lastRefreshLineTime;
    private long lastRefreshBarTime;
    private long lastRefreshListTime;

    private Handler mRefreshHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_ID_INIT_MEASURE_ITEM) {
                initMeasureItems();
            }
            if (msg.what == MSG_ID_REFRESH_LINE) {
                if (System.currentTimeMillis() - lastRefreshLineTime < REFRESH_LINE_INTERVAL) {
                    sendEmptyMessageDelayed(msg.what, REFRESH_LINE_INTERVAL - System.currentTimeMillis() + lastRefreshLineTime);
                    return;
                }
                refreshLineChart();
                mRefreshHandler.sendEmptyMessageDelayed(MSG_ID_REFRESH_LINE, REFRESH_LINE_INTERVAL);
                lastRefreshLineTime = System.currentTimeMillis();
            } else if (msg.what == MSG_ID_REFRESH_BAR) {
                if (System.currentTimeMillis() - lastRefreshBarTime < REFRESH_BAR_INTERVAL) {
                    sendEmptyMessageDelayed(msg.what, REFRESH_BAR_INTERVAL - System.currentTimeMillis() + lastRefreshBarTime);
                    return;
                }
                refreshTimeBarChart();
                refreshMaxLevelBarChart();
                mRefreshHandler.sendEmptyMessageDelayed(MSG_ID_REFRESH_LINE, REFRESH_BAR_INTERVAL);
                lastRefreshBarTime = System.currentTimeMillis();
            } else if (msg.what == MSG_ID_REFRESH_MEASURE_ITEM) {
                if (System.currentTimeMillis() - lastRefreshListTime < REFRESH_MEASURE_ITEM_INTERVAL) {
                    sendEmptyMessageDelayed(msg.what, REFRESH_MEASURE_ITEM_INTERVAL - System.currentTimeMillis() + lastRefreshListTime);
                    return;
                }
                refreshMeasureItemList();
                mRefreshHandler.sendEmptyMessageDelayed(MSG_ID_REFRESH_MEASURE_ITEM, REFRESH_MEASURE_ITEM_INTERVAL);
                lastRefreshListTime = System.currentTimeMillis();
            }
            lastRefreshLineTime = System.currentTimeMillis();
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_mp_single_frequency_measure, null);
        initMeasureTypes(view);
        initLineChart(view);
        initTimeBarChart(view);
        initMaxLevelBarChart(view);
        initService();

        mRefreshHandler.sendEmptyMessageDelayed(MSG_ID_REFRESH_LINE, REFRESH_LINE_INTERVAL);
        mRefreshHandler.sendEmptyMessageDelayed(MSG_ID_REFRESH_BAR, REFRESH_BAR_INTERVAL);
        mRefreshHandler.sendEmptyMessageDelayed(MSG_ID_REFRESH_MEASURE_ITEM, REFRESH_BAR_INTERVAL);
        return view;
    }

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

    private void initMeasureTypes(View view) {
        RecyclerView rvMeasureItems = (RecyclerView) view.findViewById(R.id.rv_single_frequency_measure_items);
        rvMeasureItems.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvMeasureItems.setNestedScrollingEnabled(false);
        mMeasureItemAdapter = new MeasureItemAdapter();
        mMeasureItemAdapter.setOnItemClickListener(new MeasureItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                choosePosition = position;
                onMeasureItemChanged();
            }
        });
        rvMeasureItems.setAdapter(mMeasureItemAdapter);
    }

    private void onMeasureItemChanged() {
        List<ItuParser48278.DataHead.HeadItem> ituHeads = mItuHead.get();
        if (ituHeads != null && choosePosition < ituHeads.size()) {
            ItuParser48278.DataHead.HeadItem head = ituHeads.get(choosePosition);
            mTvMeasureItemName.setText(head.name + "[" + head.unit + "]");
            mTvTimePercentageTitle.setText(head.name + "时间占用度");
            mTvMaxValueTitle.setText(head.name + "最大值");
        }
        if (choosePosition < mCurrentItemsData.size()) {
            mTvRealTimeLevel.setText(Float.toString(mCurrentItemsData.get(choosePosition).realtimeValue));
        }
    }

    private void initMeasureItems() {
        List<ItuParser48278.DataHead.HeadItem> ituHeads = mItuHead.get();
        mMeasureItemAdapter.initWithItuHeads(ituHeads);
        onMeasureItemChanged();
    }


    private void initLineChart(View view) {
        mTvMeasureItemName = (TextView) view.findViewById(R.id.tv_sgl_selected_measure_item_name);
        mTvRealTimeLevel = (TextView) view.findViewById(R.id.tv_sgl_selected_measure_item_realtime_value);
        mTvTimePercentageTitle = (TextView) view.findViewById(R.id.tv_sgl_selected_measure_item_time_percentage);
        mTvMaxValueTitle = (TextView) view.findViewById(R.id.tv_sgl_selected_measure_item_max_value);
        mLineChart = (LineChart) view.findViewById(R.id.chart_single_frequency_line);
        IAxisValueFormatter yValueFormatter = new ITUYAxisValueFormatter();
        YAxis leftAxis = mLineChart.getAxisLeft();
        leftAxis.setValueFormatter(yValueFormatter);
        leftAxis.enableGridDashedLine(10, 10, 0);
//        leftAxis.setAxisMinimum(DeviceConfig.MIN_LEVEL);
//        leftAxis.setAxisMaximum(DeviceConfig.MAX_LEVEL);

        IAxisValueFormatter xValueFormatter = new ITUXAxisValueFormatter();
        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setValueFormatter(xValueFormatter);
        xAxis.enableGridDashedLine(10, 10, 0);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(MAX_LINE_X_AXIS);

        LineData lineData = new LineData(generateLineDataSet());
        mLineChart.setData(lineData);
        mLineChart.invalidate();
    }

    private void initTimeBarChart(View view) {
        mTimeBarChart = (BarChart) view.findViewById(R.id.chart_single_frequency_time_percentage);
        YAxis leftAxis = mTimeBarChart.getAxisLeft();
        leftAxis.enableGridDashedLine(10, 10, 0);
        leftAxis.setAxisMinimum(0);
        leftAxis.setAxisMaximum(100);

        XAxis xAxis = mTimeBarChart.getXAxis();
        ITUXAxisTimeValueFormatter formatter = new ITUXAxisTimeValueFormatter();
        formatter.setTimeUnit(MAX_LINE_X_AXIS);
        xAxis.setValueFormatter(formatter);
        xAxis.enableGridDashedLine(1, 1, 0);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(60);

        BarData barData = new BarData(generateTimeBarData());
        barData.setBarWidth(BAR_WIDTH);
        mTimeBarChart.setData(barData);
        mTimeBarChart.invalidate();
    }

    private void initMaxLevelBarChart(View view) {
        mMaxLevelBarChart = (BarChart) view.findViewById(R.id.chart_single_frequency_max_level);
        YAxis leftAxis = mMaxLevelBarChart.getAxisLeft();
        leftAxis.enableGridDashedLine(10, 10, 0);
//        leftAxis.setAxisMinimum(0);
//        leftAxis.setAxisMaximum(DeviceConfig.MAX_LEVEL);

        XAxis xAxis = mMaxLevelBarChart.getXAxis();
        ITUXAxisTimeValueFormatter formatter = new ITUXAxisTimeValueFormatter();
        formatter.setTimeUnit(MAX_LINE_X_AXIS);
        xAxis.setValueFormatter(formatter);
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
            public void onServiceConnected(final DataProviderService.SocketBinder service) {
                service.addItuDataReceiver(ContentFragmentSingleFrequencyMeasure.this);
                service.setFrequencyRange(DeviceConfig.MIN_FREQUENCY, DeviceConfig.MAX_FREQUENCY);
                service.setLevelRange(DeviceConfig.MIN_LEVEL, DeviceConfig.MAX_LEVEL);
                String ip = PreferenceUtils.getString(PreferenceKey.DEVICE_IP);
                int port = PreferenceUtils.getInt(PreferenceKey.DEVICE_PORT);
                service.connect(ip, port, new ConnectCallback() {
                    @Override
                    public void onConnectSuccess() {
                        String cmd = "RMTP:SGLFREQ:4403000100113:frequency:97.1MHz\nifbw:15kHz\nspan:15kHz\nrecordthreshold:=40\ndemodmode:FM\n#";
                        Command command = new Command(cmd, Command.Type.ITU);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        DataProviderService.SocketBinder service = mServiceHelper.getService();
        if (service != null) {
            service.disconnect();
        }
    }

    private boolean isHit(float level) {
        return level >= MIN_MEASURE_LEVEL && level <= MAX_MEASURE_LEVEL;
    }

    private void refreshMeasureItemData(List<Float> ituData) {
        int len = ituData.size();
        for (int i = 0; i < len; i++) {
            float realtimeValue = ituData.get(i);
            if (i < mCurrentItemsData.size()) {
                ItuItemData data = mCurrentItemsData.get(i);
                data.realtimeValue = realtimeValue;
                data.averageValue = (data.averageValue * frame + realtimeValue) / (frame + 1);
                data.maxValue = Math.max(realtimeValue, data.maxValue);
                data.minValue = Math.min(realtimeValue, data.minValue);
            }
        }
    }

    @Override
    public void onReceiveItuData(List<Float> ituData) {
        if (ituData == null || ituData.size() != measureItemCount) {
            LogUtils.d("业务数据长度出错！有" + measureItemCount + "个测量项，返回了" + (ituData == null ? 0 : ituData.size()) + "条业务数据");
            return;
        }
        refreshMeasureItemData(ituData);
        frame++;
        if (choosePosition >= ituData.size()) return;

        int len = ituData.size();
        for (int i = 0; i < len; i++) {
            float level = ituData.get(i);
            mRealTimeLevels.get(i).add(new ItuLevel(System.currentTimeMillis(), level));
            ItuLevel head = mRealTimeLevels.get(i).peek();
            if (head != null && (System.currentTimeMillis() - head.timestamp) > MAX_LINE_X_AXIS) {
                mRealTimeLevels.get(i).poll();
            }
            maxLevelPerScreen.set(i, Math.max(level, maxLevelPerScreen.get(i)));
            if (isHit(level)) {
                hitCountPerScreen.set(i, hitCountPerScreen.get(i) + 1);
            }
            totalCountPerScreen.set(i, totalCountPerScreen.get(i) + 1);
        }
        if (lastCollectTime == -1) {
            lastCollectTime = System.currentTimeMillis();
            return;
        }
        if (System.currentTimeMillis() - lastCollectTime > MAX_LINE_X_AXIS) {
            for (int i = 0; i < len; i++) {
                int timeSize = mTimePercentages.get(i).size();
                if (timeSize >= MAX_BAR_X_AXIS) {
                    mTimePercentages.get(i).poll();
                }
                float timePercentage = totalCountPerScreen.get(i) == 0 ? 0f : hitCountPerScreen.get(i) * 100f / totalCountPerScreen.get(i);
                mTimePercentages.get(i).add(timePercentage);

                int maxLevelSize = mMaxLevels.get(i).size();
                if (maxLevelSize >= MAX_BAR_X_AXIS) {
                    mMaxLevels.get(i).poll();
                }
                float maxLevel = maxLevelPerScreen.get(i);
                mMaxLevels.get(i).add(maxLevel);
                hitCountPerScreen.set(i,  0);
                totalCountPerScreen.set(i, 0);
                maxLevelPerScreen.set(i, mItuHead.get().get(i).minVal);
            }
            lastCollectTime = System.currentTimeMillis();
            mRefreshHandler.sendEmptyMessage(MSG_ID_REFRESH_BAR);
        }
    }

    @Override
    public void onReceiveItuHead(List<ItuParser48278.DataHead.HeadItem> ituHeads) {
        mItuHead.set(ituHeads);
        if (!CollectionUtils.isEmpty(ituHeads)) {
            int size = ituHeads.size();
            for (int i = 0; i < size; i++) {
                mRealTimeLevels.add(new ConcurrentLinkedQueue<ItuLevel>());
                mTimePercentages.add(new ConcurrentLinkedQueue<Float>());
                mMaxLevels.add(new ConcurrentLinkedQueue<Float>());
                hitCountPerScreen.add(0);
                totalCountPerScreen.add(0);
                maxLevelPerScreen.add(ituHeads.get(i).minVal);
                mCurrentItemsData.add(new ItuItemData());
            }
            mRefreshHandler.sendEmptyMessage(MSG_ID_INIT_MEASURE_ITEM);
        }
        measureItemCount = ituHeads != null ? ituHeads.size() : 0;
    }

    private LineDataSet generateLineDataSet() {
        ArrayList<Entry> entries = new ArrayList<Entry>();
        if (choosePosition < mRealTimeLevels.size()) {
            ConcurrentLinkedQueue<ItuLevel> realtimeLevels = mRealTimeLevels.get(choosePosition);
            for (ItuLevel level : realtimeLevels) {
                entries.add(0, new Entry(System.currentTimeMillis() - level.timestamp, level.level));
            }
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

    private void refreshMeasureItemList() {
        mMeasureItemAdapter.update(mCurrentItemsData);
    }
    private void refreshLineChart() {
        LineData lineData = mLineChart.getData();
        lineData.removeDataSet(0);
        lineData.addDataSet(generateLineDataSet());
        lineData.notifyDataChanged();
        mLineChart.notifyDataSetChanged();
        mLineChart.invalidate();
        if (choosePosition < mCurrentItemsData.size()) {
            mTvRealTimeLevel.setText(Float.toString(mCurrentItemsData.get(choosePosition).realtimeValue));
        }
    }

    private BarDataSet generateTimeBarData() {
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        if (choosePosition < mTimePercentages.size()) {
            ConcurrentLinkedQueue<Float> timePercentages = mTimePercentages.get(choosePosition);
            int size = timePercentages.size();
            int xAxis = size - 1;
            for (Float level : timePercentages) {
                yVals1.add(0, new BarEntry(xAxis + BAR_WIDTH / 2, level));
                xAxis--;
            }
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
        if (choosePosition < mMaxLevels.size()) {
            ConcurrentLinkedQueue<Float> maxLevels = mMaxLevels.get(choosePosition);
            int size = maxLevels.size();
            int xAxis = size - 1;
            for (Float level : maxLevels) {
                yVals1.add(0, new BarEntry(xAxis + BAR_WIDTH / 2, level));
                xAxis--;
            }
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
