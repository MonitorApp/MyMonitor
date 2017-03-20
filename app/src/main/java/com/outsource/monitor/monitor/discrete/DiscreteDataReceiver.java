package com.outsource.monitor.monitor.discrete;

import com.outsource.monitor.monitor.base.parser.MScanParser48278;

public interface DiscreteDataReceiver {
    void onReceiveDiscreteData(MScanParser48278.DataValue msData);
    void onReceiveDiscreteHead(MScanParser48278.DataHead msHead);
}