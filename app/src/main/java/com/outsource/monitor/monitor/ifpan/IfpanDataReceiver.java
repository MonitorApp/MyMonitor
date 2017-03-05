package com.outsource.monitor.monitor.ifpan;

import com.outsource.monitor.monitor.base.parser.IfpanParser48278;

/**
 * 中频分析数据接受回调接口
 * Created by Administrator on 2016/10/2.
 */
public interface IfpanDataReceiver {
    //中频分析接收到数据
    void onReceiveIfpanData(IfpanParser48278.DataValue ifpanData);
    //中频分析接收到描述头
    void onReceiveIfpanHead(IfpanParser48278.DataHead ifpanHead);
}
