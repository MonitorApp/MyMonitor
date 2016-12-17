package com.outsource.monitor.ifpan.event;

import com.outsource.monitor.ifpan.model.IfpanParam;

/**
 * Created by xionghao on 2016/12/17.
 */

public class IfpanParamsChangeEvent {

    public IfpanParam param;

    public IfpanParamsChangeEvent(IfpanParam param) {
        this.param = param;
    }
}
