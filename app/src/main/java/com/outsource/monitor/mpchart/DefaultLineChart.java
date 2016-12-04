package com.outsource.monitor.mpchart;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.outsource.monitor.R;

/**
 * Created by Administrator on 2016/10/23.
 */
public class DefaultLineChart extends LineChart {

    public DefaultLineChart(Context context) {
        super(context);
    }

    public DefaultLineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DefaultLineChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();
        setTouchEnabled(true);
        setDragEnabled(true);
        setScaleEnabled(true);
        setPinchZoom(true);

        getDescription().setEnabled(false);
        setBackgroundColor(Color.BLACK);
        setDrawGridBackground(false);
        getAxisRight().setEnabled(false);

        YAxis leftAxis = getAxisLeft();
        leftAxis.setAxisLineColor(getResources().getColor(R.color.axis_line));
        leftAxis.setTextColor(getResources().getColor(R.color.axis_text));

        XAxis xAxis = getXAxis();
        xAxis.setAxisLineColor(getResources().getColor(R.color.axis_line));
        xAxis.setTextColor(getResources().getColor(R.color.axis_text));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
    }
}
