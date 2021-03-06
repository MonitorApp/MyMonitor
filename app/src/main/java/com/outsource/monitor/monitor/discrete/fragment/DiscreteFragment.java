package com.outsource.monitor.monitor.discrete.fragment;

import android.support.v4.app.Fragment;

import com.outsource.monitor.config.Tab;
import com.outsource.monitor.monitor.base.ui.BaseFuncFragment;
import com.outsource.monitor.monitor.base.ui.BasePlayFragment;

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
    public BasePlayFragment createContentFragment() {
        return ContentFragmentDiscrete.newInstance();
    }

    @Override
    public Fragment createMenuFragment() {
        return MScanMenuFragment.newInstance();
    }
}
