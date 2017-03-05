package com.outsource.monitor.monitor.itu.model;

/**
 * Created by Administrator on 2016/11/5.
 */
public class ItuHead {

    public ItuHead() {

    }

    public ItuHead(String name, String unit, float defaultMinValue) {
        this.name = name;
        this.unit = unit;
        this.defaultMinValue = defaultMinValue;
    }

    public String name;
    public String unit;
    public float defaultMinValue;
}
