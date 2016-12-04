package com.outsource.monitor.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.outsource.monitor.R;

/**
 * Created by Administrator on 2016/11/20.
 */

public class TestCanvas extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_canvas);
    }
}
