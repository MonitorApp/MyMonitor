package com.outsource.monitor.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.CheckBox;
import android.widget.TextView;

import com.outsource.monitor.R;
import com.outsource.monitor.ifpan.fragment.ContentFragmentMiddleFrequencyAnalyse;
import com.outsource.monitor.ifpan.fragment.MenuFragmentMiddleFrequencyAnalyse;

public class MiddleFrequencyAnalyseActivity extends TemplateActivity {

    private TextView mTvFrequency;      //频率
    private TextView mTvMdlBand;            //中频带宽
    private TextView mTvDemodulate;        //解调模式
    private TextView mTvStep;           //跨距
    private TextView mTvDetectionMode;  //检波方式
    private TextView mTvBandScanMode;   //带宽测量模式
    private TextView mTvIncreaseMode;      //增益模式
    private TextView mTvRfAttenuation;         //射频衰减
    private CheckBox mTvRecord;             //记录保存

    @Override
    public Fragment createMenuFragment() {
        return MenuFragmentMiddleFrequencyAnalyse.newInstance();
    }

    @Override
    public Fragment createContentFragment() {
        return ContentFragmentMiddleFrequencyAnalyse.newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTvFrequency = (TextView) findViewById(R.id.tv_frequence);
        mTvMdlBand = (TextView) findViewById(R.id.tv_mdl_band);
        mTvDemodulate = (TextView) findViewById(R.id.tv_demodulate);
        mTvStep = (TextView) findViewById(R.id.tv_step);
        mTvDetectionMode = (TextView) findViewById(R.id.tv_detection_mode);
        mTvBandScanMode = (TextView) findViewById(R.id.tv_band_scan_mode);
        mTvIncreaseMode = (TextView) findViewById(R.id.tv_increase_mode);
        mTvRfAttenuation = (TextView) findViewById(R.id.tv_rf_attenuation);
        mTvRecord = (CheckBox) findViewById(R.id.cb_record);
    }

    @Override
    public String getMyTitle() {
        return "中频分析";
    }

    @Override
    public void onOptionChange() {
        //// TODO: 2016/10/2
    }
}
