package com.outsource.monitor.monitor.digit;

import com.outsource.monitor.monitor.base.ui.BasePlayFragment;

/**
 * Created by xionghao on 2017/3/5.
 */

public class ContentFragmentDigit extends BasePlayFragment implements DigitDataReceiver {
    public static BasePlayFragment newInstance() {
        return new ContentFragmentDigit();
    }

    @Override
    public void onReceiveDigitData() {

    }

    @Override
    public void onReceiveDigitHead() {

    }
}
