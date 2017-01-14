package com.outsource.monitor.df;

import com.outsource.monitor.parser.DFParser48278;

/**
 * 单频测向数据接受回调接口
 * Created by xionghao on 2016/12/25.
 */

public interface DfDataReceiver {
    //单频测向接收到数据
    void onReceiveDfData(DFParser48278.DataValue dfData);
    //单频测向接收到描述头
    void onReceiveDfHead(DFParser48278.DataHead dfHead);
}
