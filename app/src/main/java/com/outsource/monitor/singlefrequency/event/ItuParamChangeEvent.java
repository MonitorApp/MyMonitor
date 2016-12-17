package com.outsource.monitor.singlefrequency.event;

import com.outsource.monitor.singlefrequency.model.SingleFrequencyParam;

/**
 * Created by xionghao on 2016/12/17.
 */

public class ItuParamChangeEvent {

    public SingleFrequencyParam param;

    public ItuParamChangeEvent(SingleFrequencyParam param) {
        this.param = param;
    }
}
