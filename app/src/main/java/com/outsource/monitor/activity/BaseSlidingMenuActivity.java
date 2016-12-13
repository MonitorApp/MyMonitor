package com.outsource.monitor.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.outsource.monitor.R;
import com.outsource.monitor.fragment.TabFragment;
import com.outsource.monitor.ifpan.fragment.ContentFragmentMiddleFrequencyAnalyse;
import com.outsource.monitor.ifpan.fragment.MenuFragmentMiddleFrequencyAnalyse;
import com.outsource.monitor.utils.DisplayUtils;


public class BaseSlidingMenuActivity extends SlidingFragmentActivity {

	protected TabFragment mLeftMenuFragment;
	protected Fragment mRightMenuFragment;
	protected Fragment mContentFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set the Behind View
		setBehindContentView(R.layout.menu_frame);
		FragmentManager fragmentManager = getSupportFragmentManager();
		//left menu fragment
		Fragment leftMenuFragment = fragmentManager.findFragmentById(R.id.menu_frame);
		if (leftMenuFragment == null) {
			leftMenuFragment = TabFragment.newInstance();
			fragmentManager.beginTransaction().add(R.id.menu_frame, leftMenuFragment).commitAllowingStateLoss();
		}
		mLeftMenuFragment = (TabFragment) leftMenuFragment;

		//SlidingMenu config
		SlidingMenu slidingMenu = getSlidingMenu();
		slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
		slidingMenu.setShadowDrawable(R.drawable.shadow);
		slidingMenu.setBehindOffset(DisplayUtils.getWidthPixels() - DisplayUtils.dp2px(86));
		slidingMenu.setFadeDegree(0.35f);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		slidingMenu.setMode(SlidingMenu.LEFT_RIGHT);

		//contentView
		setContentView(R.layout.content_frame);
		Fragment contentFragment = fragmentManager.findFragmentById(R.id.content_frame);
		if (contentFragment == null) {
			contentFragment = ContentFragmentMiddleFrequencyAnalyse.newInstance();
			fragmentManager.beginTransaction().add(R.id.content_frame, contentFragment).commitAllowingStateLoss();
		}
		mContentFragment = contentFragment;

		//right menu fragment
		slidingMenu.setSecondaryMenu(R.layout.menu_frame_two);
		slidingMenu.setSecondaryShadowDrawable(R.drawable.shadow);
		Fragment rightMenuFragment = fragmentManager.findFragmentById(R.id.menu_frame_two);
		if (rightMenuFragment == null) {
			rightMenuFragment = MenuFragmentMiddleFrequencyAnalyse.newInstance();
			fragmentManager.beginTransaction().add(R.id.menu_frame_two, rightMenuFragment).commitAllowingStateLoss();
		}
		mRightMenuFragment = rightMenuFragment;
	}
}
