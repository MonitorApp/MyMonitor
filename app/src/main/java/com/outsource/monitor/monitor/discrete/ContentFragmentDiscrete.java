package com.outsource.monitor.monitor.discrete;

import android.support.v4.app.Fragment;

/**
 * Created by xionghao on 2017/3/5.
 */

public class ContentFragmentDiscrete extends Fragment implements DiscreteDataReceiver {
    public static Fragment newInstance() {
        return new ContentFragmentDiscrete();
    }

    @Override
    public void onReceiveDiscreteData() {

    }

    @Override
    public void onReceiveDiscreteHead() {

    }
}
