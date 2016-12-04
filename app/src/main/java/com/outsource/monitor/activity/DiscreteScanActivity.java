package com.outsource.monitor.activity;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;

import com.outsource.monitor.R;
import com.outsource.monitor.fragment.ContentFragmentDiscreteScan;
import com.outsource.monitor.fragment.MenuFragmentDiscreteScan;

public class DiscreteScanActivity extends TemplateActivity {

    private TextView mTvFreqencyTable;      //频率表
    private TextView mTvMdlBand;            //中频带宽
    private TextView mTvDuration;           //驻留时间
    private TextView mTvDetectionMode;      //检波方式
    private TextView mTvBandScanMode;      //射频工作模式
    private TextView mTvIncreaseMode;      //增益模式
    private TextView mTvRfAttenuation;      //射频衰减
    private CheckBox mCbnRecord;            //记录文件

    @Override
    public Fragment createMenuFragment() {
        return MenuFragmentDiscreteScan.newInstance();
    }

    @Override
    public Fragment createContentFragment() {
        return ContentFragmentDiscreteScan.newInstance();
    }

    @Override
    public String getMyTitle() {
        return "离散扫描";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTvFreqencyTable = (TextView) findViewById(R.id.tv_frequence_table);
        mTvMdlBand = (TextView) findViewById(R.id.tv_mdl_band);
        mTvDuration = (TextView) findViewById(R.id.tv_duration);
        mTvDetectionMode = (TextView) findViewById(R.id.tv_detection_mode);
        mTvBandScanMode = (TextView) findViewById(R.id.tv_band_scan_mode);
        mTvIncreaseMode = (TextView) findViewById(R.id.tv_increase_mode);
        mTvRfAttenuation = (TextView) findViewById(R.id.tv_rf_attenuation);
        mCbnRecord = (CheckBox) findViewById(R.id.cb_record);
    }

    @Override
    protected void onOptionChange() {
        //// TODO: 2016/10/2
    }
}
