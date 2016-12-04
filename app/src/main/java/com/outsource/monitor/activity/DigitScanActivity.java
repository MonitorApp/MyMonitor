package com.outsource.monitor.activity;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;

import com.outsource.monitor.R;
import com.outsource.monitor.fragment.ContentFragmentDigitScan;
import com.outsource.monitor.fragment.MenuFragmentDigitScan;

public class DigitScanActivity extends TemplateActivity {

    private TextView mTvFrequenceBegin;     //起始频率
    private TextView mTvFrequenceEnd;       //终止频率
    private TextView mTvStep;                //步长
    private TextView mTvDuration;           //驻留时间
    private TextView mTvDetectionMode;     //检波方式
    private TextView mTvBandScanMode;     //射频工作模式
    private TextView mTvIncreaseMode;     //增益模式
    private TextView mTvRfAttenuation;     //射频衰减
    private CheckBox mCbRecord;             //记录文件

    @Override
    public Fragment createMenuFragment() {
        return MenuFragmentDigitScan.newInstance();
    }

    @Override
    public Fragment createContentFragment() {
        return ContentFragmentDigitScan.newInstance();
    }

    @Override
    public String getMyTitle() {
        return "数字扫描";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTvFrequenceBegin = (TextView) findViewById(R.id.tv_frequence_begin);
        mTvFrequenceEnd = (TextView) findViewById(R.id.tv_frequence_end);
        mTvStep = (TextView) findViewById(R.id.tv_step);
        mTvDuration = (TextView) findViewById(R.id.tv_duration);
        mTvDetectionMode = (TextView) findViewById(R.id.tv_detection_mode);
        mTvBandScanMode = (TextView) findViewById(R.id.tv_band_scan_mode);
        mTvIncreaseMode = (TextView) findViewById(R.id.tv_increase_mode);
        mTvRfAttenuation = (TextView) findViewById(R.id.tv_rf_attenuation);
        mCbRecord = (CheckBox) findViewById(R.id.cb_record);
    }

    @Override
    protected void onOptionChange() {
        //// TODO: 2016/10/2
    }
}
