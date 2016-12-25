package com.outsource.monitor.df.event;

import com.outsource.monitor.df.model.DfParam;

/**
 * Created by xionghao on 2016/12/25.
 */

public class DfParamChangeEvent {

    public DfParam param;

    public DfParamChangeEvent(DfParam param) {
        this.param = param;
    }
}
