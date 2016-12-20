package com.outsource.monitor.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.outsource.monitor.R;
import com.outsource.monitor.base.BaseActivity;
import com.outsource.monitor.parser.Command;

/**
 * Created by Administrator on 2016/10/2.
 */
public abstract class TemplateActivity extends BaseActivity
{
    private DrawerLayout mDrawerLayout;
    protected Fragment mMenuFragment;
    protected Fragment mContentFragment;

    public abstract Fragment createMenuFragment();

    public abstract Fragment createContentFragment();

    public abstract String getMyTitle();

    public Command getCmd() {
        return  null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template);
        initViews();
    }

    private void initViews() {
        mMenuFragment = createMenuFragment();
        mContentFragment = createContentFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.right_menu_container, mMenuFragment, mMenuFragment.getClass().getSimpleName())
                .add(R.id.fl_main_container, mContentFragment, mContentFragment.getClass().getSimpleName())
                .commitAllowingStateLoss();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);

//        findViewById(R.id.btn_ok).setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_template, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_menu_toggle) {
            if(mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
            }else{
                mDrawerLayout.openDrawer(Gravity.RIGHT);
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onOptionChange()
    {
        Log.i("TemplateActivity","onOptionChange");
    }
}
