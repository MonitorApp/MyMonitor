package com.outsource.monitor.config;

/**
 * Created by xionghao on 2016/12/17.
 */

public enum  Tab {

    ITU("单频测量"),
    IFPAN("中频分析"),
    FSCAN("频段扫描"),
    DF("单频测向"),
    DISCRETE("离散扫描"),
    DIGIT("数字扫描");

    public String name;

    Tab(String name) {
        this.name = name;
    }
}
