package com.outsource.monitor.singlefrequency.chartformatter;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ITUXAxisTimeValueFormatter implements IAxisValueFormatter
{

    private SimpleDateFormat mFormat;
    private Date mDate;
    private int mTimeUnit;

    public ITUXAxisTimeValueFormatter() {
        mFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        mDate = new Date();
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        mDate.setTime(System.currentTimeMillis() - (mTimeUnit * (int) value));
        return mFormat.format(mDate);
    }

    @Override
    public int getDecimalDigits() {
        return 1;
    }

    public void setTimeUnit(int unit) {
        mTimeUnit = unit;
    }
}
