package com.outsource.monitor.monitor.digit;


import com.outsource.monitor.monitor.base.parser.DScanParser48278;

public interface DigitDataReceiver {
    void onReceiveDigitData(DScanParser48278.DataValue dsData);
    void onReceiveDigitHead(DScanParser48278.DataHead dsHead);
}