package com.outsource.monitor.monitor.itu.model;

/**
 * Created by Administrator on 2016/11/5.
 */
public class ItuTimePercentageThreshold {

    public ItuTimePercentageThreshold() {

    }

    public String name;
    public String unit;
    public int threshold;

    public ItuTimePercentageThreshold(String name, String unit, int threshold) {
        this.name = name;
        this.unit = unit;
        this.threshold = threshold;
    }
}
