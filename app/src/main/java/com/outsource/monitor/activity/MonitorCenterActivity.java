package com.outsource.monitor.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.view.WindowManager;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.outsource.monitor.R;
import com.outsource.monitor.base.BaseSlidingMenuActivity;
import com.outsource.monitor.base.OnTabChangeEvent;
import com.outsource.monitor.base.Tab;
import com.outsource.monitor.floating.FloatingBall;
import com.outsource.monitor.floating.FloatingViewWrapper;
import com.outsource.monitor.fragment.BaseMonitorFragment;
import com.outsource.monitor.fragment.TabMenuFragment;
import com.outsource.monitor.ifpan.fragment.IfpanFragment;
import com.outsource.monitor.singlefrequency.fragment.ItuFragment;
import com.outsource.monitor.utils.DisplayUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by xionghao on 2016/12/17.
 */

public class MonitorCenterActivity extends BaseSlidingMenuActivity {

    public static final String TAB = "tab";
    private BaseMonitorFragment mCurrentFragment;
    private Tab mCurrentTab = Tab.ITU;
    private FloatingViewWrapper mFloatingViewWrapper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentTab = (Tab) getIntent().getSerializableExtra(TAB);
        if (mCurrentTab == null) mCurrentTab = Tab.ITU;
        // set the Behind View
        setBehindContentView(R.layout.left_menu_container);
        FragmentManager fragmentManager = getSupportFragmentManager();
        //left menu fragment
        fragmentManager.beginTransaction().add(R.id.left_menu_container, TabMenuFragment.newInstance(mCurrentTab)).commitAllowingStateLoss();

        //SlidingMenu config
        SlidingMenu slidingMenu = getSlidingMenu();
        slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        slidingMenu.setShadowDrawable(R.drawable.shadow);
        slidingMenu.setBehindOffset(DisplayUtils.getScreenWidth() - DisplayUtils.dp2px(90));
        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        slidingMenu.setMode(SlidingMenu.LEFT);

        //contentView
        setContentView(R.layout.activity_monitor_center);
        mCurrentFragment = createContentFragment(mCurrentTab);
        fragmentManager.beginTransaction().add(R.id.fl_monitor_container, mCurrentFragment).commitAllowingStateLoss();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            inflateFloatingBall();
        }
    }

    private void inflateFloatingBall() {
        IBinder token = getWindow().getDecorView().getWindowToken();
        if (token == null) {
           return;
        }
        if (mFloatingViewWrapper == null) {
            FloatingBall floatingBall = new FloatingBall(this);
            mFloatingViewWrapper = new FloatingViewWrapper(this);
            mFloatingViewWrapper.addViewToWindow(floatingBall);
        }
    }

    @Override
    public void finish() {
        if (mFloatingViewWrapper != null) {
            WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            windowManager.removeViewImmediate(mFloatingViewWrapper);
        }
        super.finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTabChangeEvent(OnTabChangeEvent event) {
        if (mCurrentTab == event.tab) return;
        switchTab(event.tab);
    }

    private void switchTab(Tab tab) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        BaseMonitorFragment contentFragment = createContentFragment(tab);
        fragmentManager.beginTransaction().replace(R.id.fl_monitor_container, contentFragment).commitAllowingStateLoss();
        mCurrentFragment = contentFragment;

        mCurrentTab = tab;
    }

    private BaseMonitorFragment createContentFragment(Tab tab) {
        switch (tab) {
            case ITU:
                return ItuFragment.newInstance();
            case IFPAN:
                return IfpanFragment.newInstance();
            case BAND_SCAN:
            case DISCRETE_SCAN:
            case DIGIT_SCAN:
                return IfpanFragment.newInstance();
            default:
                break;
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public boolean isPlaying() {
        if (mFloatingViewWrapper != null && mFloatingViewWrapper.getChildCount() > 0) {
            FloatingBall floatingBall = (FloatingBall) mFloatingViewWrapper.getChildAt(0);
            return floatingBall.isPlaying();
        }
        return false;
    }
}
