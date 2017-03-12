package com.outsource.monitor.monitor.itu.fragment;

import com.outsource.monitor.config.Tab;
import com.outsource.monitor.monitor.base.ui.BaseFuncFragment;
import com.outsource.monitor.monitor.base.ui.BasePlayFragment;

/**
 * Created by xionghao on 2016/12/17.
 */

public class ItuFragment extends BaseFuncFragment {

    public static ItuFragment newInstance() {
        return new ItuFragment();
    }

    @Override
    public Tab tab() {
        return Tab.ITU;
    }

    @Override
    public BasePlayFragment createContentFragment() {
        return ContentFragmentItu.newInstance();
    }


}
