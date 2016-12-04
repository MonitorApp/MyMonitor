package com.outsource.monitor.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.outsource.monitor.ActivityManager;

/**
 * Created by Administrator on 2016/10/2.
 */
public class BaseActivity extends AppCompatActivity {

    private AlertDialog mLoadingDialog;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.instance().onCreate(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActivityManager.instance().onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ActivityManager.instance().onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.instance().onDestroy(this);
    }

    public void showLoadingDialog(String message) {
        if (mLoadingDialog == null) {
            mLoadingDialog = new AlertDialog.Builder(this)
                    .setMessage(message)
                    .create();
        }
        if (!mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
        }
    }

    public void dismissLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }
}
