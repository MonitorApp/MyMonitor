package com.outsource.monitor.base;

import com.outsource.monitor.config.ConfigManager;
import com.outsource.monitor.config.UIParam;

import java.util.List;

/**
 * Created by xionghao on 2017/3/5.
 */

public class ParamChangeEvent {

    public ConfigManager.FuncType funcType;
    public List<UIParam> params;

    public ParamChangeEvent(ConfigManager.FuncType funcType, List<UIParam> params) {
        this.funcType = funcType;
        this.params = params;
    }
}
