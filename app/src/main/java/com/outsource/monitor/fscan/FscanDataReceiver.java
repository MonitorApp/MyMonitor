package com.outsource.monitor.fscan;

import com.outsource.monitor.parser.FscanParser48278;

/**
 * 频段扫描数据接受回调接口
 * Created by xionghao on 2016/12/25.
 */

public interface FscanDataReceiver {
    //频段扫描接收到数据
    void onReceiveFScanData(FscanParser48278.DataValue fscanData);
    //频段扫描接收到描述头
    void onReceiveFScanHead(FscanParser48278.DataHead fscanHead);
}
