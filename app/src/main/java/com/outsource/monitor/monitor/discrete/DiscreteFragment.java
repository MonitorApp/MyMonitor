package com.outsource.monitor.monitor.discrete;

import android.support.v4.app.Fragment;

import com.outsource.monitor.config.Tab;
import com.outsource.monitor.monitor.base.ui.BaseFuncFragment;

/**
 * Created by xionghao on 2017/3/5.
 */

public class DiscreteFragment extends BaseFuncFragment {

    public static DiscreteFragment newInstance() {
        return new DiscreteFragment();
    }

    @Override
    public Tab tab() {
        return Tab.DISCRETE;
    }

    @Override
    public Fragment createContentFragment() {
        return ContentFragmentDiscrete.newInstance();
    }
}
