package com.outsource.monitor.itu.model;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.outsource.monitor.config.UIParam;
import com.outsource.monitor.utils.CollectionUtils;
import com.outsource.monitor.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuly on 2016/12/13.
 */
public class SingleFrequencyParam {
    public String devId = "4403000100113";
    public float frequecy;       //频率 @97.1 min:20 max:26500.0
    public float ifbw;  //中频带宽 @120khz
    public String demodmode;           //解调模式 am/fm
    public int span;                //跨距
    public String detector;   //检波方式 avg(max/peak)
    public String bandScanMode; //带宽测量模式 @XdB带宽 β带宽
    public int gainctrl; //增益模式 (自动增益 对应 ATT ON)  min:-30 ,max:-130
    public boolean rfatt; //射频衰减  //@常态 低噪声 低失真
    public boolean bRecord;     //记录保存

    public List<UIParam> uiParams = new ArrayList<>();

    //String cmd = "RMTP:IFANALYSIS:4403000100113:frequency:98.1MHz\nifbw:30kHz\nspan:15kHz\nrecordthreshold:=40\ndemodmode:FM\n#";
    public String getCommand() {
        StringBuilder sb = new StringBuilder();
        sb.append("RMTP:SGLFREQ");
        sb.append(String.format(":%s:", devId));
//        sb.append(String.format("frequency%.1fMHz\n", frequecy));
//        sb.append(String.format("ifbw:%.1fkHz\n", ifbw));
//        sb.append(String.format("demodmode:%s\n", demodmode));
//        sb.append(String.format("detector:%s\n", detector)); //AVG;
//        sb.append(String.format("gainctrl:%d\n", gainctrl));
//        //sb.append(String.format("rfatt:%.1fkHz\n", rfatt)); //常态 低噪声 低失真
//        //sb.append(String.format("afc:%.1fkHz\n", afc)); //自动频率控制 @关 开
//        //sb.append(String.format("bandmode:%.1fkHz\n", bandScanMode)); //常态 低噪声 低失真
//        sb.append(String.format("span:%dkHz\n", span));
//        sb.append(String.format("recordthreshold:%d\n", 40));
//        sb.append("#");
        for (UIParam param : uiParams) {
            sb.append(param.name + ":" + param.value + param.unit + "\n");
        }
        Log.e("getCommand", sb.toString());
        return sb.toString();
    }

    public void save() {
        PreferenceUtils.putFloat("Sgl_frequency", frequecy);
        PreferenceUtils.putFloat("Sgl_ifbw", ifbw);
        PreferenceUtils.putString("Sgl_demodmode", demodmode);
        PreferenceUtils.putString("Sgl_detector", detector);
        PreferenceUtils.putInt("Sgl_gainctrl", gainctrl);
        PreferenceUtils.putInt("Sgl_span", span);

        Gson gson = new Gson();
        String json = gson.toJson(uiParams, new TypeToken<List<UIParam>>(){}.getType());
        PreferenceUtils.putString("itu_param", json);
    }

    public static SingleFrequencyParam loadFromCache() {
        SingleFrequencyParam param = new SingleFrequencyParam();
        param.devId = "4403000100113";
        param.frequecy = PreferenceUtils.getFloat("Sgl_frequency", 0);
        param.ifbw = PreferenceUtils.getFloat("Sgl_ifbw", 0);
        param.demodmode = PreferenceUtils.getString("Sgl_demodmode", "FM");
        param.detector = PreferenceUtils.getString("Sgl_detector", "AVG");
        param.gainctrl = PreferenceUtils.getInt("Sgl_gainctrl", 0);
        param.span = PreferenceUtils.getInt("Sgl_span", 0);

        String json = PreferenceUtils.getString("itu_param");
        if (!TextUtils.isEmpty(json)) {
            Gson gson = new Gson();
            List<UIParam> params = gson.fromJson(json, new TypeToken<List<UIParam>>(){}.getType());
            if (!CollectionUtils.isEmpty(params)) {
                param.uiParams.addAll(params);
            }
        }
        return param;
    }

}
