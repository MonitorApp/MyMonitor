package com.outsource.monitor.singlefrequency.model;

/**
 * Created by Administrator on 2016/11/5.
 */
public class ItuItemData {

    public ItuItemData() {

    }

    public ItuItemData(float realtimeValue, float averageValue, float maxValue, float minValue) {
        this.realtimeValue = realtimeValue;
        this.averageValue = averageValue;
        this.maxValue = maxValue;
        this.minValue = minValue;
    }

    public float realtimeValue;
    public float averageValue;
    public float maxValue;
    public float minValue;
}
