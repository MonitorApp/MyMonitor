package com.outsource.monitor.service;

import com.outsource.monitor.model.FrequencyLevel;

import java.util.List;

/**
 * 此前写测试接口，可以删掉
 * Created by Administrator on 2016/10/2.
 */
public interface DataReceiver {
    //单频测量接收到itu数据
    void onReceiveItuData(float[] ituData);
    //接收频段数据
    void onReceiveBandLevel(List<FrequencyLevel> levels);
}
