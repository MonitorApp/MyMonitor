package com.outsource.monitor.monitor.df.fragment;

import android.support.v4.app.Fragment;

import com.outsource.monitor.config.Tab;
import com.outsource.monitor.monitor.base.ui.BaseFuncFragment;
import com.outsource.monitor.monitor.base.ui.BasePlayFragment;

/**
 * Created by xionghao on 2016/12/17.
 */

public class DfFragment extends BaseFuncFragment {

    public static DfFragment newInstance() {
        return new DfFragment();
    }

    @Override
    public Tab tab() {
        return Tab.DF;
    }

    @Override
    public BasePlayFragment createContentFragment() {
        return ContentFragmentDf.newInstance();
    }
}
