package com.outsource.monitor.service;

import com.outsource.monitor.parser.ItuParser48278;

import java.util.List;

/**
 * 单频测量数据接受回调接口
 * Created by Administrator on 2016/10/2.
 */
public interface ItuDataReceiver {
    //单频测量接收到itu数据
    void onReceiveItuData(List<Float> ituData);
    //单频测量接收到itu描述头
    void onReceiveItuHead(ItuParser48278.DataHead ituHead);
}
