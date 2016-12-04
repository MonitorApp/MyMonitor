package com.outsource.monitor.service;

import com.outsource.monitor.parser.IfpanParser48278;
import com.outsource.monitor.parser.ItuParser48278;

import java.util.List;

/**
 * 中频分析数据接受回调接口
 * Created by Administrator on 2016/10/2.
 */
public interface IfpanDataReceiver {
    //单频测量接收到itu数据
    void onReceiveIfpanData(IfpanParser48278.DataValue ifpanData);
    //单频测量接收到itu描述头
    void onReceiveIfpanHead(IfpanParser48278.DataHead ifpanHeads);
}
