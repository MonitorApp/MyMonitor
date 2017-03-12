package com.outsource.monitor.monitor.base.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.WindowManager;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.outsource.monitor.R;
import com.outsource.monitor.base.BaseSlidingMenuActivity;
import com.outsource.monitor.base.OnTabChangeEvent;
import com.outsource.monitor.config.Tab;
import com.outsource.monitor.monitor.df.fragment.DfFragment;
import com.outsource.monitor.monitor.digit.DigitFragment;
import com.outsource.monitor.monitor.discrete.DiscreteFragment;
import com.outsource.monitor.monitor.fscan.fragment.FscanFragment;
import com.outsource.monitor.monitor.ifpan.fragment.IfpanFragment;
import com.outsource.monitor.monitor.itu.fragment.ItuFragment;
import com.outsource.monitor.other.map.MapFragment;
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
    private MapFragment mMapFragment;
    private TabMenuFragment mTabMenuFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        mCurrentTab = (Tab) getIntent().getSerializableExtra(TAB);
        if (mCurrentTab == null) mCurrentTab = Tab.ITU;
        // set the Behind View
        setBehindContentView(R.layout.left_menu_container);
        FragmentManager fragmentManager = getSupportFragmentManager();
        //left menu fragment
        mTabMenuFragment = TabMenuFragment.newInstance(mCurrentTab);
        fragmentManager.beginTransaction().add(R.id.left_menu_container, mTabMenuFragment).commitAllowingStateLoss();
        mTabMenuFragment.setOnMapSwitchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMapFragment.isVisible()) {
                    hideMapFragment();
                } else {
                    showMapFragment();
                }
            }
        });

        //SlidingMenu config
        SlidingMenu slidingMenu = getSlidingMenu();
        slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        slidingMenu.setShadowDrawable(R.drawable.shadow);
        slidingMenu.setBehindOffset(DisplayUtils.getScreenWidth() - DisplayUtils.dp2px(120));
        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        slidingMenu.setMode(SlidingMenu.LEFT);

        //contentView
        setContentView(R.layout.activity_monitor_center);
        mCurrentFragment = createContentFragment(mCurrentTab);
        fragmentManager.beginTransaction().add(R.id.fl_monitor_container, mCurrentFragment).commitAllowingStateLoss();

        mMapFragment = MapFragment.newInstance();
        fragmentManager.beginTransaction().add(R.id.fl_map_container, mMapFragment).hide(mMapFragment).commitAllowingStateLoss();
        findViewById(R.id.left_menu_container).post(new Runnable() {
            @Override
            public void run() {
                showMenu();
            }
        });

        EventBus.getDefault().register(this);
    }


//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus) {
//            FloatingManager.getInstance().show();
//        } else {
//            FloatingManager.getInstance().hide();
//        }
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTabChangeEvent(OnTabChangeEvent event) {
        if (mCurrentTab == event.tab) return;
        switchTab(event.tab);
    }

    private void switchTab(Tab tab) {
        if (mMapFragment.isVisible()) {
            hideMapFragment();
        }
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
            case FSCAN:
                return FscanFragment.newInstance();
            case DF:
                return DfFragment.newInstance();
            case DISCRETE:
                return DiscreteFragment.newInstance();
            case DIGIT:
                return DigitFragment.newInstance();
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
        return mTabMenuFragment.isPlaying();
    }

    public MapFragment getMapFragment() {
        return mMapFragment;
    }

    private void showMapFragment() {
        getSupportFragmentManager().beginTransaction().show(mMapFragment).commitAllowingStateLoss();
    }

    private void hideMapFragment() {
        getSupportFragmentManager().beginTransaction().hide(mMapFragment).commitAllowingStateLoss();
    }
 }
