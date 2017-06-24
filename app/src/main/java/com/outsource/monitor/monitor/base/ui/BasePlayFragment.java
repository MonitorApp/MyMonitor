package com.outsource.monitor.monitor.base.ui;

import com.outsource.monitor.base.BaseFragment;

/**
 * Created by xionghao on 2017/3/12.
 */

public class BasePlayFragment extends BaseFragment {

    private boolean isPlay;

    public boolean isPlay() {
        return isPlay;
    }

    public void play() {
        isPlay = true;
        onPlay();
    }

    public void pause() {
        isPlay = false;
        onPaused();
    }

    protected void onPlay() {

    }

    protected void onPaused() {

    }
}
