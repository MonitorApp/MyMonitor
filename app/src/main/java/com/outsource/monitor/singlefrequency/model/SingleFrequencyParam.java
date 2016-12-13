package com.outsource.monitor.singlefrequency.model;

import com.outsource.monitor.utils.PreferenceUtils;

/**
 * Created by wuly on 2016/12/13.
 */
public class SingleFrequencyParam
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
        sb.append("RMTP:SGLFREQ");
        sb.append(String.format(":%s:", devId));
        sb.append(String.format("frequency%.1fMHz\n", (float)frequecy/1000000));
        sb.append(String.format("ifbw:%.1fkHz\n", mdlFrequencyBand));
        sb.append(String.format("span:%dkHz\n", step));
        sb.append(String.format("recordthreshold:=%d\n", 40));
        sb.append(String.format("demodmode:%s\n", demodulate));
        sb.append("#");
        return sb.toString();
    }

    static public void SaveParam(SingleFrequencyParam param)
    {
        PreferenceUtils.putLong("Sgl_Frequency", param.frequecy);
        PreferenceUtils.putFloat("Sgl_MdlFrequencyBand", param.mdlFrequencyBand);
        PreferenceUtils.putInt("Sgl_Step", param.step);
        PreferenceUtils.putString("Sgl_Demodulate", param.demodulate);
    }

    static public  SingleFrequencyParam LoadParam()
    {
        SingleFrequencyParam param = new SingleFrequencyParam();
        param.devId = "4403000100113";
        param.frequecy = PreferenceUtils.getLong("Sgl_Frequency", 98100000);
        param.mdlFrequencyBand = PreferenceUtils.getFloat("Sgl_MdlFrequencyBand", 30f);
        param.step = PreferenceUtils.getInt("Sgl_Step", 15);
        param.demodulate = PreferenceUtils.getString("Sgl_Demodulate", "FM");
        return param;
    }

}
