
package com.outsource.monitor.fscan.fragment;

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
import com.outsource.monitor.fscan.adapter.FscanRangeAdapter;
import com.outsource.monitor.fscan.chartformatter.FscanXAxisValueFormatter;
import com.outsource.monitor.fscan.chartformatter.FscanYAxisValueFormatter;
import com.outsource.monitor.fscan.model.FscanParam;
import com.outsource.monitor.ifpan.model.FallRow;
import com.outsource.monitor.parser.FscanParser48278;
import com.outsource.monitor.fscan.FscanDataReceiver;
import com.outsource.monitor.utils.CollectionUtils;
import com.outsource.monitor.utils.DisplayUtils;
import com.outsource.monitor.utils.LogUtils;
import com.outsource.monitor.utils.PromptUtils;
import com.outsource.monitor.widget.FScanFallsLevelView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

public class ContentFragmentFscan extends Fragment implements FscanDataReceiver {

    private FscanRangeAdapter mFScanRangeAdapter;

    public static ContentFragmentFscan newInstance() {
        return new ContentFragmentFscan();
    }

    private AtomicReference<FscanParser48278.DataHead> mDataHead = new AtomicReference<>();
    private AtomicReference<List<Float>> mCurrentData = new AtomicReference<>();
    private ConcurrentLinkedQueue<FallRow> mFallRows = new ConcurrentLinkedQueue<>();
    //瀑布图要显示100秒的数据，如果每条数据都显示的话数据量太大，所以每次把1秒内的数据取平均值合并成一条
    private CombinedChart mChart;
    private FScanFallsLevelView mFallsLevelView;
    private static final long REFRESH_CHART_INTERVAL = 100;
    private static final int MSG_ID_REFRESH_CHART = 1;

    private boolean isPlay = false;
    private boolean showLineChart = false;

    private int choosePosition;

    public static float DISPLAY_Y_DELTA = 46;//柱状图显示都是从0开始，底下的负数不会填充，所以将所有电平值都加50，保证大于0

    private Handler mRefreshHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_ID_REFRESH_CHART) {
                if (isPlay) {
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayPauseEvent(PlayPauseEvent event) {
        if (event.isPlay) {
            FscanParam param = FscanParam.loadFromCache();
            if (param.startFrequency == 0 || param.endFrequency == 0 || param.step == 0) {
                PromptUtils.showToast("请先设置有效的参数再开始");
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
        View view = inflater.inflate(R.layout.frag_content_fscan, null);
        initFrequencyRangs(view);
        initCombineChart(view);
        initFallChart(view);
        mRefreshHandler.sendEmptyMessageDelayed(MSG_ID_REFRESH_CHART, 500);

        EventBus.getDefault().register(this);

        if (((MonitorCenterActivity) getActivity()).isPlaying()) {
            FscanParam param = FscanParam.loadFromCache();
            if (param.startFrequency == 0 || param.endFrequency == 0 || param.step == 0) {
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

    private void initFrequencyRangs(View view) {
        RecyclerView rvFrequencyBands = (RecyclerView) view.findViewById(R.id.rv_fscan_items);
        rvFrequencyBands.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvFrequencyBands.setNestedScrollingEnabled(false);
        mFScanRangeAdapter = new FscanRangeAdapter();
        mFScanRangeAdapter.setOnItemClickListener(new FscanRangeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (choosePosition == position) return;
                choosePosition = position;
                if (mDataHead.get() != null && position < mDataHead.get().fscanParamList.size()) {
                    onFrequencyBandSelected(mDataHead.get().fscanParamList.get(position));
                }
            }
        });
        rvFrequencyBands.setAdapter(mFScanRangeAdapter);
    }

    private void onFrequencyBandSelected(FscanParser48278.DataHead.FcanParam param) {
        XAxis xAxis = mChart.getXAxis();
        xAxis.setAxisMinimum(DisplayUtils.toDisplayFrequency(param.startFreq));
        xAxis.setAxisMaximum(DisplayUtils.toDisplayFrequency(param.endFreq));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    private void initCombineChart(View view) {
        mChart = (CombinedChart) view.findViewById(R.id.chart_fscan);
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
        xAxis.setPosition(XAxisPosition.BOTTOM);
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
        mFallsLevelView = (FScanFallsLevelView) view.findViewById(R.id.fall_fscan);
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
        FscanParser48278.DataHead head = mDataHead.get();
        List<Float> values = mCurrentData.get();
        if (head == null || choosePosition >= mDataHead.get().fscanParamList.size() || CollectionUtils.isEmpty(values)) {
            return;
        }
        ArrayList<Entry> entries = new ArrayList<Entry>();
        FscanParser48278.DataHead.FcanParam param = head.fscanParamList.get(choosePosition);
        float start = DisplayUtils.toDisplayFrequency(param.startFreq);
        float displaySpan = DisplayUtils.toDisplayFrequency(param.endFreq - param.startFreq);
        int count = values.size();
        for (int i = 0; i < count; i++) {
            Float value = values.get(i);
            entries.add(new Entry(start + i * displaySpan / count,  value + DISPLAY_Y_DELTA));
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
        FscanParser48278.DataHead head = mDataHead.get();
        List<Float> values = mCurrentData.get();
        if (head == null || choosePosition >= mDataHead.get().fscanParamList.size() || CollectionUtils.isEmpty(values)) {
            return;
        }
        ArrayList<BarEntry> entries = new ArrayList<BarEntry>();
        FscanParser48278.DataHead.FcanParam param = head.fscanParamList.get(choosePosition);
        float start = DisplayUtils.toDisplayFrequency(param.startFreq);
        float displaySpan = DisplayUtils.toDisplayFrequency(param.endFreq - param.startFreq);
        int count = values.size();
        for (int i = 0; i < count; i++) {
            Float value = values.get(i);
            entries.add(new BarEntry(start + i * displaySpan / count, value + DISPLAY_Y_DELTA));
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
    public void onReceiveFScanData(FscanParser48278.DataValue fscanData) {
        if (fscanData == null || CollectionUtils.isEmpty(fscanData.values)) {
            LogUtils.d("频段扫描接受数据为空！");
            return;
        }
        mFallsLevelView.onReceiveFScanData(fscanData);
        mCurrentData.set(fscanData.values);
    }

    @Override
    public void onReceiveFScanHead(final FscanParser48278.DataHead fscanHead) {
        if (fscanHead == null || fscanHead.fscanParamList.size() == 0) {
            LogUtils.d("频段扫描帧头为空！");
            return;
        }
        mDataHead.set(fscanHead);
        mFallsLevelView.onReceiveFScanHead(fscanHead);
        mRefreshHandler.post(new Runnable() {
            @Override
            public void run() {
                mFScanRangeAdapter.update(fscanHead.fscanParamList);
                onFrequencyBandSelected(fscanHead.fscanParamList.get(0));
            }
        });
    }
}
