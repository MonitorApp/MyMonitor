package com.outsource.monitor.monitor.discrete;

import com.outsource.monitor.monitor.base.ui.BasePlayFragment;

/**
 * Created by xionghao on 2017/3/5.
 */

public class ContentFragmentDiscrete extends BasePlayFragment implements DiscreteDataReceiver {
    public static BasePlayFragment newInstance() {
        return new ContentFragmentDiscrete();
    }

    @Override
    public void onReceiveDiscreteData() {

    }

    @Override
    public void onReceiveDiscreteHead() {

    }
}
