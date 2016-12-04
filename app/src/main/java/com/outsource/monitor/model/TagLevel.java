package com.outsource.monitor.model;

/**
 * Created by Administrator on 2016/10/6.
 */
public class TagLevel extends Level {

    public String name;

    public TagLevel(float level, long timestamp, String name) {
        super(level, timestamp);
        this.name = name;
    }
}
