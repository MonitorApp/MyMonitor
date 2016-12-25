package com.outsource.monitor.df.model;

import com.outsource.monitor.utils.PreferenceUtils;

/**
 * Created by xionghao on 2016/12/25.
 */

public class DfParam {

    public float frequency;

    public DfParam(float frequency) {
        this.frequency = frequency;
    }

    public static DfParam loadFromCache() {
        return new DfParam(PreferenceUtils.getFloat("df_frequency", 0));
    }

    public void save() {
        PreferenceUtils.putFloat("df_frequency", frequency);
    }
}
