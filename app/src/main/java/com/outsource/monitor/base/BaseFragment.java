package com.outsource.monitor.base;

import android.support.v4.app.Fragment;

/**
 * Created by xionghao on 2017/6/24.
 */

public class BaseFragment extends Fragment {

    public boolean isFinishing() {
        return getActivity() == null || getActivity().isFinishing();
    }
}
