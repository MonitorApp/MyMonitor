package com.outsource.monitor.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.outsource.monitor.R;
import com.outsource.monitor.base.BaseActivity;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initView();
    }

    private void initView()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("关于");
        toolbar.setSubtitle("");
    }
}
