package com.outsource.monitor.monitor.ifpan.chartformatter;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

public class IFPANXAxisValueFormatter implements IAxisValueFormatter
{

    private float frequency;
    private float span;

    public IFPANXAxisValueFormatter() {
    }

    public void setSpan(float span) {
        this.span = span;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        value += frequency;
        String xFormat = String.format("%.1fMHz", value);
        if (value == frequency) {
            return xFormat;
        }
        return "";
    }

    @Override
    public int getDecimalDigits() {
        return 1;
    }
}
