package com.outsource.monitor.activity;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.TextView;

import com.outsource.monitor.R;
import com.outsource.monitor.parser.Command;
import com.outsource.monitor.singlefrequency.fragment.ContentFragmentSingleFrequencyMeasure;
import com.outsource.monitor.singlefrequency.fragment.MenuFragmentSingleFrequencyMeasure;
import com.outsource.monitor.utils.ParamChangeEvent;
import com.outsource.monitor.utils.PreferenceUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2016/10/2.
 */
public class SingleFrequencyMeasureActivity extends TemplateActivity {

    static private String TAG = "SingleFrequencyMeasureActivity";

    private TextView mTvFrequency;          //频率
    private TextView mTvMdlFrequencyBand;  //中频带宽
    private TextView mTvDemodulate;         //解调模式
    private TextView mTvStep;               //跨距
    private TextView mTvDetectionMode;    //检波方式
    private TextView mTvBandScanMode;      //带宽测量模式
    private TextView mTvIncreaseMode;      //增益模式
    private TextView mTvRfAttenuation;      //射频衰减
    private CheckBox mCbRecord;                //记录保存

    private  SingleFrequencyParam param = new SingleFrequencyParam();

    @Override
    public Fragment createMenuFragment() {
        return MenuFragmentSingleFrequencyMeasure.newInstance();
    }

    @Override
    public Fragment createContentFragment() {
        return ContentFragmentSingleFrequencyMeasure.newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTvFrequency = (TextView) findViewById(R.id.tv_frequence);
        mTvMdlFrequencyBand = (TextView) findViewById(R.id.tv_mdl_band);
        mTvDemodulate  = (TextView) findViewById(R.id.tv_demodulate);
        mTvStep = (TextView) findViewById(R.id.tv_step);
        mTvDetectionMode = (TextView) findViewById(R.id.tv_detection_mode);
        mTvBandScanMode = (TextView) findViewById(R.id.tv_band_scan_mode);
        mTvIncreaseMode = (TextView) findViewById(R.id.tv_increase_mode);
        mTvRfAttenuation = (TextView) findViewById(R.id.tv_rf_attenuation);
        mCbRecord = (CheckBox) findViewById(R.id.cb_record);

        LoadParam();
        SyncParam2UI();
    }

    @Override
    public String getMyTitle() {
        return "单频测量";
    }

    @Override
    protected void onOptionChange() {
        //// TODO: 2016/10/2
        Log.d(TAG, "onOptionChange");
        SyncUI2Param();
        SaveParam();
        EventBus.getDefault().post(new ParamChangeEvent());
    }

    public Command getCmd() {
        if(param == null)
        {
            return  null;
        }
        return new Command(param.GetCommand(), Command.Type.ITU);
    }

    private void SyncUI2Param()
    {
        param.frequecy = (long)(Float.parseFloat(mTvFrequency.getText().toString())*1000000);
        param.mdlFrequencyBand = Float.parseFloat(mTvMdlFrequencyBand.getText().toString());
        param.demodulate = mTvDemodulate.getText().toString();
        param.step = Integer.parseInt(mTvStep.getText().toString());
    }

    private void SyncParam2UI()
    {
        if(param == null)
        {
            return;
        }

        mTvFrequency.setText(String.format ("%.1f", (float)param.frequecy/1000000));
        mTvMdlFrequencyBand.setText(String.format("%f", param.mdlFrequencyBand));
        mTvDemodulate.setText(param.demodulate);
        mTvStep.setText(String.format("%d", param.step));
    }

    private void SaveParam()
    {
        PreferenceUtils.putLong("Sgl_Frequency", param.frequecy);
        PreferenceUtils.putFloat("Sgl_MdlFrequencyBand", param.mdlFrequencyBand);
        PreferenceUtils.putInt("Sgl_Step", param.step);
        PreferenceUtils.putString("Sgl_Demodulate", param.demodulate);
    }

    private  void LoadParam()
    {
        param = new SingleFrequencyParam();
        param.devId = "4403000100113";
        param.frequecy = PreferenceUtils.getLong("Sgl_Frequency", 98100000);
        param.mdlFrequencyBand = PreferenceUtils.getFloat("Sgl_MdlFrequencyBand", 30f);
        param.step = PreferenceUtils.getInt("Sgl_Step", 15);
        param.demodulate = PreferenceUtils.getString("Sgl_Demodulate", "FM");
    }

    private class SingleFrequencyParam
    {
        public String devId= "4403000100113";
        public long frequecy;       //频率
        public float mdlFrequencyBand;  //中频带宽
        public String demodulate;           //解调模式 am/fm
        public int step;                //跨距
        public String detectMode;   //检波方式 avg(max/peak)
        public String bandScanMode; //带宽测量模式
        public String increaseMode; //增益模式 (自动增益 对应 ATT ON)
        public boolean bRfAttenuation; //射频衰减  对应噪声（SQL）
        public boolean bRecord;     //记录保存

        //String cmd = "RMTP:IFANALYSIS:4403000100113:frequency:98.1MHz\nifbw:30kHz\nspan:15kHz\nrecordthreshold:=40\ndemodmode:FM\n#";
        public String GetCommand()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("RMTP:IFANALYSIS");
            sb.append(String.format(":%s:", devId));
            sb.append(String.format("frequency%.1fMHz\n", (float)frequecy/1000000));
            sb.append(String.format("ifbw:%.1fkHz\n", mdlFrequencyBand));
            sb.append(String.format("span:%dkHz\n", step));
            sb.append(String.format("recordthreshold:%d\n", 40));
            sb.append(String.format("demodmode:%s\n", demodulate));
            sb.append("#");
            return sb.toString();
        }
    }
}
