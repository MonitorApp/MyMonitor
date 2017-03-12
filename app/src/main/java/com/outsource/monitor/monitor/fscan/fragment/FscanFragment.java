package com.outsource.monitor.monitor.fscan.fragment;

import com.outsource.monitor.config.Tab;
import com.outsource.monitor.monitor.base.ui.BaseFuncFragment;
import com.outsource.monitor.monitor.base.ui.BasePlayFragment;

/**
 * Created by xionghao on 2016/12/17.
 */

public class FscanFragment extends BaseFuncFragment {

    public static FscanFragment newInstance() {
        return new FscanFragment();
    }

    @Override
    public Tab tab() {
        return Tab.FSCAN;
    }

    @Override
    public BasePlayFragment createContentFragment() {
        return ContentFragmentFscan.newInstance();
    }
}
