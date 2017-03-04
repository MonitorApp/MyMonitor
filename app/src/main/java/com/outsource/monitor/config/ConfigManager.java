package com.outsource.monitor.config;

import android.content.Context;
import android.text.TextUtils;

import com.outsource.monitor.R;
import com.outsource.monitor.utils.PromptUtils;
import com.thoughtworks.xstream.XStream;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xionghao on 2017/3/5.
 */

public class ConfigManager {

    private static final ConfigManager sInstance = new ConfigManager();
    private Config mConfig;


    public static ConfigManager getInstance() {
        return sInstance;
    }

    public void initFromXml(Context context) {
        try {
            InputStream in = context.getResources().openRawResource(R.raw.tci735v);
            XStream xs = new XStream();
            xs.setMode(XStream.NO_REFERENCES);
            xs.processAnnotations(new Class[]{Config.class});
            mConfig = (Config) xs.fromXML(in);
        } catch (Exception e) {
            e.printStackTrace();
            PromptUtils.showToast("解析参数配置文件出错！");
        }
    }

    public Config getConfig() {
        if (mConfig != null) {
            return mConfig;
        }
        return new Config();
    }

    public List<Param.Item> getFuncParams(int funcIndex) {
        List<Param.Item> curParams = new ArrayList<>(0);
        Config config = ConfigManager.getInstance().getConfig();
        if (config.device.funcs.size() > funcIndex && funcIndex >= 0) {
            List<Param.Item> allParams = config.device.param.items;

            Func func = config.device.funcs.get(funcIndex);
            List<Func.Items.FuncItem> items = func.items.items;
            for (Func.Items.FuncItem item : items) {
                if (item.living) {
                    for (Param.Item p : allParams) {
                        if (TextUtils.equals(p.name, item.name)) {
                            curParams.add(p);
                        }
                    }
                }
            }
        }
        return curParams;
    }
}
