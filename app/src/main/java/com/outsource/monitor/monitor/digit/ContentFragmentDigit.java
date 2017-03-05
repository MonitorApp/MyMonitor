package com.outsource.monitor.monitor.digit;

import android.support.v4.app.Fragment;

/**
 * Created by xionghao on 2017/3/5.
 */

public class ContentFragmentDigit extends Fragment implements DigitDataReceiver {
    public static Fragment newInstance() {
        return new ContentFragmentDigit();
    }

    @Override
    public void onReceiveDigitData() {

    }

    @Override
    public void onReceiveDigitHead() {

    }
}
