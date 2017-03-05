package com.outsource.monitor.monitor.itu.fragment;

import android.support.v4.app.Fragment;

import com.outsource.monitor.config.Tab;
import com.outsource.monitor.monitor.base.ui.BaseFuncFragment;

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
    public Fragment createContentFragment() {
        return ContentFragmentItu.newInstance();
    }

}
