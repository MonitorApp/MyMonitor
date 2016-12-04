package com.outsource.monitor.service;

/**
 * Created by Administrator on 2016/11/1.
 */
public interface ConnectCallback {
    void onConnectSuccess();
    void onConnectFail(String message);
}
