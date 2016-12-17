package com.outsource.monitor.base;

/**
 * Created by xionghao on 2016/12/17.
 */

public enum  Tab {

    ITU("单频测量"),
    IFPAN("中频分析"),
    BAND_SCAN("频段扫描"),
    DISCRETE_SCAN("离散扫描"),
    DIGIT_SCAN("数字扫描");

    public String name;

    Tab(String name) {
        this.name = name;
    }
}
