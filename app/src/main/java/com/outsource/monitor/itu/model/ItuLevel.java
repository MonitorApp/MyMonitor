package com.outsource.monitor.itu.model;

/**
 * Created by Administrator on 2016/11/26.
 */

public class ItuLevel {

    public long timestamp;
    public float level;

    public ItuLevel(long timestamp, float level) {
        this.timestamp = timestamp;
        this.level = level;
    }
}
