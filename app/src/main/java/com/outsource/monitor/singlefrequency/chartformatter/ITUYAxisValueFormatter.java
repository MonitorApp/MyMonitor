package com.outsource.monitor.singlefrequency.chartformatter;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

public class ITUYAxisValueFormatter implements IAxisValueFormatter
{

    public ITUYAxisValueFormatter() {
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return String.format("%.1f", value);
    }

    @Override
    public int getDecimalDigits() {
        return 1;
    }
}
