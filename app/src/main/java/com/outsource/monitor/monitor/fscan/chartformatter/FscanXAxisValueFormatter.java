package com.outsource.monitor.monitor.fscan.chartformatter;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

public class FscanXAxisValueFormatter implements IAxisValueFormatter
{

    private float frequency;

    public FscanXAxisValueFormatter() {
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return String.format("%.1fMHz", value);
    }

    @Override
    public int getDecimalDigits() {
        return 1;
    }
}
