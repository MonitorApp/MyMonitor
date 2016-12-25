package com.outsource.monitor.itu.chartformatter;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ITUXAxisValueFormatter implements IAxisValueFormatter
{

    private SimpleDateFormat mFormat;
    private Date mDate;

    public ITUXAxisValueFormatter() {
        mFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        mDate = new Date();
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        mDate.setTime(System.currentTimeMillis() - (long) value);
        return mFormat.format(mDate);
    }

    @Override
    public int getDecimalDigits() {
        return 1;
    }
}
