package com.outsource.monitor.ifpan.model;

import com.outsource.monitor.utils.PreferenceUtils;

/**
 * Created by xionghao on 2016/12/17.
 */

public class IfpanParam {

    public float frequency;
    public int band;
    public int span;

    public IfpanParam(float frequency, int band, int span) {
        this.frequency = frequency;
        this.band = band;
        this.span = span;
    }

    public static IfpanParam loadFromCache() {
        return new IfpanParam(PreferenceUtils.getFloat("ifpan_frequency", 98.1f), PreferenceUtils.getInt("ifpan_band", 30), PreferenceUtils.getInt("ifpan_span", 15));
    }

    public void save() {
        PreferenceUtils.putFloat("ifpan_frequency", frequency);
        PreferenceUtils.putInt("ifpan_band", band);
        PreferenceUtils.putInt("ifpan_span", span);
    }
}
