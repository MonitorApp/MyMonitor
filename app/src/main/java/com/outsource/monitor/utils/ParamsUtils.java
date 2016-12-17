package com.outsource.monitor.utils;

import com.outsource.monitor.ifpan.IfpanParam;

/**
 * Created by xionghao on 2016/12/17.
 */

public class ParamsUtils {

    public static void saveIfpanParam(IfpanParam param) {
        PreferenceUtils.putFloat("ifpan_frequency", param.frequency);
        PreferenceUtils.putInt("ifpan_band", param.band);
        PreferenceUtils.putInt("ifpan_span", param.span);
    }

    public static IfpanParam getIfpanParam() {
        return new IfpanParam(PreferenceUtils.getFloat("ifpan_frequency", 98.1f), PreferenceUtils.getInt("ifpan_band", 30), PreferenceUtils.getInt("ifpan_span", 15));
    }
}
