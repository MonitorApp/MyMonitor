package com.outsource.monitor.monitor.ifpan.chartformatter;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

public class IFPANXAxisValueFormatter implements IAxisValueFormatter
{

    private float frequency;
    private float max;

    public IFPANXAxisValueFormatter() {
    }

    public void setSpan(float span) {
        max = span;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
//        value -= max / 2;
        return String.format("%.1fkHz", value);
    }

    @Override
    public int getDecimalDigits() {
        return 1;
    }
}
