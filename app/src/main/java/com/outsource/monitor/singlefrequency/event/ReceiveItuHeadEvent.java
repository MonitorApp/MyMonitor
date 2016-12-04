package com.outsource.monitor.singlefrequency.event;

import com.outsource.monitor.parser.ItuParser48278;

/**
 * Created by Administrator on 2016/11/6.
 */
public class ReceiveItuHeadEvent {

    public ItuParser48278.DataHead ituHead;

    public ReceiveItuHeadEvent(ItuParser48278.DataHead ituHead) {
        this.ituHead = ituHead;
    }
}
