package com.outsource.monitor.monitor.digit;

import android.support.v4.app.Fragment;

import com.outsource.monitor.config.Tab;
import com.outsource.monitor.monitor.base.ui.BaseFuncFragment;

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
    public Fragment createContentFragment() {
        return ContentFragmentDigit.newInstance();
    }
}
