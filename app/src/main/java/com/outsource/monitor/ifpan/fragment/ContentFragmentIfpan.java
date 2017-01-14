
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
import android.widget.TextView;

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
import com.outsource.monitor.activity.MonitorCenterActivity;
import com.outsource.monitor.event.PlayBallStateEvent;
import com.outsource.monitor.event.PlayPauseEvent;
import com.outsource.monitor.ifpan.chartformatter.IFPANXAxisValueFormatter;
import com.outsource.monitor.ifpan.model.FallRow;
import com.outsource.monitor.ifpan.model.IfpanParam;
import com.outsource.monitor.parser.IfpanParser48278;
import com.outsource.monitor.ifpan.IfpanDataReceiver;
import com.outsource.monitor.itu.chartformatter.ITUYAxisValueFormatter;
import com.outsource.monitor.utils.CollectionUtils;
import com.outsource.monitor.utils.DisplayUtils;
import com.outsource.monitor.utils.LogUtils;
import com.outsource.monitor.utils.PromptUtils;
import com.outsource.monitor.widget.FallsLevelView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

public class ContentFragmentIfpan extends Fragment implements IfpanDataReceiver {

    public static ContentFragmentIfpan newInstance() {
        return new ContentFragmentIfpan();
    }

    private AtomicReference<IfpanParser48278.DataHead> mDataHead = new AtomicReference<>();
    private AtomicReference<List<Float>> mCurrentData = new AtomicReference<>();
    private ConcurrentLinkedQueue<FallRow> mFallRows = new ConcurrentLinkedQueue<>();
    //瀑布图要显示100秒的数据，如果每条数据都显示的话数据量太大，所以每次把1秒内的数据取平均值合并成一条
    private CombinedChart mChart;
    private IFPANXAxisValueFormatter mXValueFormatter;
    private FallsLevelView mFallsLevelView;
    private static final long REFRESH_CHART_INTERVAL = 100;
    private static final int MSG_ID_REFRESH_CHART = 1;

    private TextView mTvCurrentFrequencyLevel;
    private TextView mTvAnalyseInfo;
    private boolean isPlay = false;
    private boolean showBarData = false;

    private Handler mRefreshHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_ID_REFRESH_CHART) {
                if (isPlay) {
                    refreshTitle();
                    refreshLineChart();
                    if (showBarData) {
                        refreshBarChart();
                    }
                    CombinedData data = mChart.getData();
                    data.notifyDataChanged();
                    mChart.notifyDataSetChanged();
                    mChart.invalidate();
                }
                mRefreshHandler.sendEmptyMessageDelayed(MSG_ID_REFRESH_CHART, REFRESH_CHART_INTERVAL);
            }
        }

    };

    private void refreshTitle() {
        IfpanParser48278.DataHead head = mDataHead.get();
        List<Float> levels = mCurrentData.get();
        if (head != null && !CollectionUtils.isEmpty(levels)) {
            long frequency = head.frequence;
            float displayFrequency = DisplayUtils.toDisplayFrequency(frequency);
            mTvCurrentFrequencyLevel.setText(String.format("频率：%.4fMHz，电平：%.1fdBpV", displayFrequency, levels.get(levels.size() / 2)));
            int maxLevelIndex = 0;
            float maxLevel = 0;
            int size = levels.size();
            for (int i = 0; i < size; i++) {
                Float level = levels.get(i);
                if (level > maxLevel) {
                    maxLevel = level;
                    maxLevelIndex = i;
                }
            }
            long deltaSpan = maxLevelIndex * head.span / size - head.span / 2;
            float displayDelta = DisplayUtils.toDisplaySpan(deltaSpan);
            long maxLevelFrequency = frequency + deltaSpan;
            float displayMaxLevelFrequency = DisplayUtils.toDisplayFrequency(maxLevelFrequency);
            mTvAnalyseInfo.setText(String.format("峰值：%.4fMHz，%.1fdBpV，△f:%.1fkHz", displayMaxLevelFrequency, maxLevel, displayDelta));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayPauseEvent(PlayPauseEvent event) {
        if (event.isPlay) {
            IfpanParam param = IfpanParam.loadFromCache();
            if (param.frequency == 0 || param.band == 0 || param.span == 0) {
                PromptUtils.showToast("请先设置有效的中频分析参数再开始");
                EventBus.getDefault().post(new PlayBallStateEvent(false));
                return;
            }
        }
        isPlay = event.isPlay;
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_content_ifpan, null);
        mXValueFormatter = new IFPANXAxisValueFormatter();
        mTvCurrentFrequencyLevel = (TextView) view.findViewById(R.id.tv_ifpan_current_frequency_level);
        mTvAnalyseInfo = (TextView) view.findViewById(R.id.tv_ifpan_analyse_info);
        initCombineChart(view);
        initFallChart(view);
        mRefreshHandler.sendEmptyMessageDelayed(MSG_ID_REFRESH_CHART, 500);

        EventBus.getDefault().register(this);

        if (((MonitorCenterActivity) getActivity()).isPlaying()) {
            IfpanParam param = IfpanParam.loadFromCache();
            if (param.frequency == 0 || param.band == 0 || param.span == 0) {
                EventBus.getDefault().post(new PlayBallStateEvent(false));
            } else {
                isPlay = true;
            }
        }
        if (!isPlay) {
            mFallsLevelView.pause();
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    private void initCombineChart(View view) {
        mChart = (CombinedChart) view.findViewById(R.id.chart_middle_frequency_analyse);
        mChart.getDescription().setEnabled(false);
        mChart.setBackgroundColor(Color.TRANSPARENT);
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
        if (showBarData) {
            data.setData(defaultBarData());
        }
        data.setData(defaultLineData());

        mChart.setData(data);
        mChart.invalidate();
    }

    private void initFallChart(View view) {
        mFallsLevelView = (FallsLevelView) view.findViewById(R.id.fall_middle_frequency_analyse);
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
        IfpanParser48278.DataHead head = mDataHead.get();
        List<Float> values = mCurrentData.get();
        if (head == null || CollectionUtils.isEmpty(values)) {
            return;
        }
        ArrayList<Entry> entries = new ArrayList<Entry>();
        float displaySpan = DisplayUtils.toDisplaySpan(head.span);
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
        float displaySpan = DisplayUtils.toDisplaySpan(head.span);
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

    @Override
    public void onReceiveIfpanData(IfpanParser48278.DataValue ifpanData) {
        if (ifpanData == null || CollectionUtils.isEmpty(ifpanData.levelList)) {
            LogUtils.d("中频分析接受数据为空！");
            return;
        }
        mFallsLevelView.onReceiveIfpanData(ifpanData);
//        if (ColorBuffer.valueCount == 0) {
//            ColorBuffer.valueCount = ifpanData.dotCount;
//        }
        mCurrentData.set(ifpanData.levelList);
    }

    @Override
    public void onReceiveIfpanHead(IfpanParser48278.DataHead ifpanHeads) {
        if (ifpanHeads == null) {
            LogUtils.d("中频分析帧头为空！");
            return;
        }
        mFallsLevelView.onReceiveIfpanHead(ifpanHeads);
        mDataHead.set(ifpanHeads);
        XAxis xAxis = mChart.getXAxis();
        float axisSpan = DisplayUtils.toDisplaySpan(ifpanHeads.span);
        mXValueFormatter.setSpan(axisSpan);
        xAxis.setAxisMinimum(-axisSpan / 2);
        xAxis.setAxisMaximum(axisSpan / 2);
    }
}
