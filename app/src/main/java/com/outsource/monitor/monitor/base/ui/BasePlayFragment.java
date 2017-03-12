package com.outsource.monitor.monitor.base.ui;

import android.support.v4.app.Fragment;

/**
 * Created by xionghao on 2017/3/12.
 */

public class BasePlayFragment extends Fragment {

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
