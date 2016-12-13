package com.outsource.monitor.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.outsource.monitor.R;

public class MainActivity extends FragmentActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitComponent();
    }

    private void InitComponent(){
        findViewById(R.id.tv_sgl_scan).setOnClickListener(this); //单频扫描
        findViewById(R.id.tv_midddle_analyse).setOnClickListener(this);
        findViewById(R.id.tv_band_scan).setOnClickListener(this);
        findViewById(R.id.tv_discrete_scan).setOnClickListener(this);
        findViewById(R.id.tv_digit_scan).setOnClickListener(this);
        findViewById(R.id.tv_map).setOnClickListener(this);
        findViewById(R.id.tv_record).setOnClickListener(this);
        findViewById(R.id.tv_option).setOnClickListener(this);
        findViewById(R.id.tv_about).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        //Toast.makeText(this, ((TextView)v).getText(), Toast.LENGTH_SHORT).show();
        switch (v.getId())
        {
            case R.id.tv_sgl_scan:
                startActivity(new Intent(MainActivity.this, SingleFrequencyMeasureActivity.class));
                break;
            case R.id.tv_midddle_analyse:
                startActivity(new Intent(MainActivity.this, BaseSlidingMenuActivity.class));
                break;
            case R.id.tv_band_scan:
                startActivity(new Intent(MainActivity.this, BandScanActivity.class));
                break;
            case R.id.tv_discrete_scan:
                startActivity(new Intent(MainActivity.this, DiscreteScanActivity.class));
                break;
            case R.id.tv_digit_scan:
                startActivity(new Intent(MainActivity.this, DigitScanActivity.class));
                break;
            case R.id.tv_map:
                break;
            case R.id.tv_record:
                startActivity(new Intent(MainActivity.this, RecordActivity.class));
                break;
            case R.id.tv_option:
                startActivity(new Intent(MainActivity.this, OptionActivity.class));
                break;
            case R.id.tv_about:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
        }
    }

}
