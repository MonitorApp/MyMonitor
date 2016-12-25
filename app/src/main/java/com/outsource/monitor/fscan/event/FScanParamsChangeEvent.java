package com.outsource.monitor.fscan.event;

import com.outsource.monitor.fscan.model.FscanParam;

/**
 * Created by xionghao on 2016/12/17.
 */

public class FscanParamsChangeEvent {

    public FscanParam param;

    public FscanParamsChangeEvent(FscanParam param) {
        this.param = param;
    }
}
