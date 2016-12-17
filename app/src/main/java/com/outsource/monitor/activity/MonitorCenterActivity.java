package com.outsource.monitor.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.view.Gravity;
import android.view.View;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.outsource.monitor.R;
import com.outsource.monitor.base.BaseSlidingMenuActivity;
import com.outsource.monitor.base.OnTabChangeEvent;
import com.outsource.monitor.base.Tab;
import com.outsource.monitor.fragment.ContentFragmentBandScan;
import com.outsource.monitor.fragment.ContentFragmentDigitScan;
import com.outsource.monitor.fragment.ContentFragmentDiscreteScan;
import com.outsource.monitor.fragment.MenuFragmentBandScan;
import com.outsource.monitor.fragment.MenuFragmentDigitScan;
import com.outsource.monitor.fragment.MenuFragmentDiscreteScan;
import com.outsource.monitor.fragment.TabMenuFragment;
import com.outsource.monitor.ifpan.fragment.ContentFragmentMiddleFrequencyAnalyse;
import com.outsource.monitor.ifpan.fragment.MenuFragmentMiddleFrequencyAnalyse;
import com.outsource.monitor.singlefrequency.fragment.ContentFragmentSingleFrequencyMeasure;
import com.outsource.monitor.singlefrequency.fragment.MenuFragmentSingleFrequencyMeasure;
import com.outsource.monitor.utils.DisplayUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Field;

/**
 * Created by xionghao on 2016/12/17.
 */

public class MonitorCenterActivity extends BaseSlidingMenuActivity {

    public static final String TAB = "tab";
    private DrawerLayout mDrawerLayout;
    private Fragment mContentFragment;
    private Fragment mRightMenuFragment;
    private Tab mCurrentTab = Tab.ITU;

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
        slidingMenu.setBehindOffset(DisplayUtils.getWidthPixels() - DisplayUtils.dp2px(90));
        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        slidingMenu.setMode(SlidingMenu.LEFT);

        //contentView
        setContentView(R.layout.drawer_layout);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.dl_right);
        setDrawerEdgeSize();
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                drawerView.setClickable(true);
            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        //content fragment
        mContentFragment = createContentFragment(mCurrentTab);
        fragmentManager.beginTransaction().add(R.id.fl_main_container, mContentFragment).commitAllowingStateLoss();

        //right menu fragment
        mRightMenuFragment = createRightMenuFragment(mCurrentTab);
        fragmentManager.beginTransaction().add(R.id.right_menu_container, mRightMenuFragment).commitAllowingStateLoss();

        EventBus.getDefault().register(this);
    }

    private void setDrawerEdgeSize() {
        try {
            Field fieldDragger = mDrawerLayout.getClass().getDeclaredField("mRightDragger");
            fieldDragger.setAccessible(true);
            ViewDragHelper rightDragger = (ViewDragHelper) fieldDragger.get(mDrawerLayout);

            Field filedEdgeSize = rightDragger.getClass().getDeclaredField("mEdgeSize");
            filedEdgeSize.setAccessible(true);
            int edge = filedEdgeSize.getInt(rightDragger);

            filedEdgeSize.setInt(rightDragger, edge * 3);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTabChangeEvent(OnTabChangeEvent event) {
        if (mCurrentTab == event.tab) return;
        switchTab(event.tab);
    }

    private void switchTab(Tab tab) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment contentFragment = createContentFragment(tab);
        fragmentManager.beginTransaction().replace(R.id.fl_main_container, contentFragment).commitAllowingStateLoss();
        mContentFragment = contentFragment;

        Fragment rightMenuFragment = createRightMenuFragment(tab);
        fragmentManager.beginTransaction().replace(R.id.right_menu_container, rightMenuFragment).commitAllowingStateLoss();
        mRightMenuFragment = rightMenuFragment;

        mCurrentTab = tab;
    }

    private Fragment createContentFragment(Tab tab) {
        switch (tab) {
            case ITU:
                return ContentFragmentSingleFrequencyMeasure.newInstance();
            case IFPAN:
                return ContentFragmentMiddleFrequencyAnalyse.newInstance();
            case BAND_SCAN:
                return ContentFragmentBandScan.newInstance();
            case DISCRETE_SCAN:
                return ContentFragmentDiscreteScan.newInstance();
            case DIGIT_SCAN:
                return ContentFragmentDigitScan.newInstance();
            default:
                break;
        }
        return new Fragment();
    }

    private Fragment createRightMenuFragment(Tab tab) {
        switch (tab) {
            case ITU:
                return MenuFragmentSingleFrequencyMeasure.newInstance();
            case IFPAN:
                return MenuFragmentMiddleFrequencyAnalyse.newInstance();
            case BAND_SCAN:
                return MenuFragmentBandScan.newInstance();
            case DISCRETE_SCAN:
                return MenuFragmentDiscreteScan.newInstance();
            case DIGIT_SCAN:
                return MenuFragmentDigitScan.newInstance();
            default:
                break;
        }
        return new Fragment();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
