package com.outsource.monitor.monitor.ifpan.fragment;

import android.support.v4.app.Fragment;

import com.outsource.monitor.config.Tab;
import com.outsource.monitor.monitor.base.ui.BaseFuncFragment;

/**
 * Created by xionghao on 2016/12/17.
 */

public class IfpanFragment extends BaseFuncFragment {

    public static IfpanFragment newInstance() {
        return new IfpanFragment();
    }

    @Override
    public Tab tab() {
        return Tab.IFPAN;
    }

    @Override
    public Fragment createContentFragment() {
        return ContentFragmentIfpan.newInstance();
    }
}
