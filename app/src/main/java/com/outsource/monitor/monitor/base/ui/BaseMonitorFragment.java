package com.outsource.monitor.monitor.base.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.outsource.monitor.R;
import com.outsource.monitor.config.Tab;

import java.lang.reflect.Field;

/**
 * Created by xionghao on 2016/12/17.
 */

public abstract class BaseMonitorFragment extends Fragment {

    protected Tab mCurrentTab = Tab.ITU;
    protected DrawerLayout mDrawerLayout;
    protected Fragment mContentFragment;
    protected Fragment mMenuFragment;

    public abstract Tab tab();
    public abstract Fragment createContentFragment();
    public abstract Fragment createMenuFragment();

    public BaseMonitorFragment() {
        mCurrentTab = tab();
    }

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_base_monitor, null);
        mDrawerLayout = (DrawerLayout) view.findViewById(R.id.drawer);
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
        FragmentManager fragmentManager = getChildFragmentManager();
        //content fragment
        mContentFragment = createContentFragment();
        fragmentManager.beginTransaction().add(R.id.fl_main_container, mContentFragment).commitAllowingStateLoss();

        //right menu fragment
        mMenuFragment = createMenuFragment();
        fragmentManager.beginTransaction().add(R.id.right_menu_container, mMenuFragment).commitAllowingStateLoss();

        return view;
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
}
