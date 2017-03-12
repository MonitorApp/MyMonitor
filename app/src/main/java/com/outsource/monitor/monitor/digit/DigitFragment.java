package com.outsource.monitor.monitor.digit;

import com.outsource.monitor.config.Tab;
import com.outsource.monitor.monitor.base.ui.BaseFuncFragment;
import com.outsource.monitor.monitor.base.ui.BasePlayFragment;

/**
 * Created by xionghao on 2017/3/5.
 */

public class DigitFragment extends BaseFuncFragment {

    public static DigitFragment newInstance() {
        return new DigitFragment();
    }

    @Override
    public Tab tab() {
        return Tab.DIGIT;
    }

    @Override
    public BasePlayFragment createContentFragment() {
        return ContentFragmentDigit.newInstance();
    }
}
