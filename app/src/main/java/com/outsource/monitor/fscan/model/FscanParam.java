package com.outsource.monitor.fscan.model;

import com.outsource.monitor.utils.PreferenceUtils;

/**
 * Created by xionghao on 2016/12/25.
 */

public class FscanParam {

    public float startFrequency;
    public float endFrequency;
    public int step;

    public FscanParam(float startFrequency, float endFrequency, int step) {
        this.startFrequency = startFrequency;
        this.endFrequency = endFrequency;
        this.step = step;
    }

    public static FscanParam loadFromCache() {
        return new FscanParam(PreferenceUtils.getFloat("fscan_start_frequency", 0), PreferenceUtils.getFloat("fscan_end_frequency", 0), PreferenceUtils.getInt("fscan_step", 0));
    }

    public void save() {
        PreferenceUtils.putFloat("fscan_start_frequency", startFrequency);
        PreferenceUtils.putFloat("fscan_end_frequency", endFrequency);
        PreferenceUtils.putInt("fscan_step", step);
    }
}
