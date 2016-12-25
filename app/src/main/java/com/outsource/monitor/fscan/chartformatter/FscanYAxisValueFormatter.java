package com.outsource.monitor.fscan.chartformatter;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.outsource.monitor.fscan.fragment.ContentFragmentFscan;

public class FscanYAxisValueFormatter implements IAxisValueFormatter
{

    public FscanYAxisValueFormatter() {
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return String.format("%.1f", value - ContentFragmentFscan.DISPLAY_Y_DELTA);
    }

    @Override
    public int getDecimalDigits() {
        return 1;
    }
}
