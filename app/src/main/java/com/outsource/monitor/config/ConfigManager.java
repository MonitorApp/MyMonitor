package com.outsource.monitor.config;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.outsource.monitor.R;
import com.outsource.monitor.monitor.base.parser.Command;
import com.outsource.monitor.utils.CollectionUtils;
import com.outsource.monitor.utils.PreferenceUtils;
import com.outsource.monitor.utils.PromptUtils;
import com.thoughtworks.xstream.XStream;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xionghao on 2017/3/5.
 */

public class ConfigManager {

    public static final String DEVICE_ID = "4403000100113";

    public enum FuncType {
        ITU(1, "itu_params"),// 映射xml里面的func的id
        IFPAN(2, "ifpan_params"),
        FSCAN(3, "fscan_params"),
        DF(4, "df_params"),
        DISCRETE(5, "discrete_params"),
        DIGIT(6, "digit_params");

        int funcId;
        String prefKey;

        FuncType(int funcId, String prefKey) {
            this.funcId = funcId;
            this.prefKey = prefKey;
        }

        public int getFuncId() {
            return funcId;
        }

        public String getPrefKey() {
            return prefKey;
        }
    }

    private static final ConfigManager sInstance = new ConfigManager();
    private Map<FuncType, List<UIParam>> mParamCache = new HashMap<>();
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

    public List<Param.Item> getFuncParamItems(FuncType funcType) {
        List<Param.Item> curItems = new ArrayList<>(0);
        Config config = ConfigManager.getInstance().getConfig();
        for (Func func : config.device.funcs) {
            if (TextUtils.equals(func.id, Integer.toString(funcType.getFuncId()))) {
                List<Param.Item> allItems = config.device.param.items;

                List<Func.Items.FuncItem> items = func.items.items;
                for (Func.Items.FuncItem item : items) {
                    if (item.living) {
                        for (Param.Item p : allItems) {
                            if (TextUtils.equals(p.name, item.name)) {
                                curItems.add(p);
                            }
                        }
                    }
                }
            }
        }
        return curItems;
    }

    public void saveParams(FuncType funcType, List<UIParam> params) {
        if (CollectionUtils.isEmpty(params)) return;

        mParamCache.put(funcType, params);

        Gson gson = new Gson();
        String json = gson.toJson(params, new TypeToken<List<UIParam>>(){}.getType());
        PreferenceUtils.putString(funcType.getPrefKey(), json);
    }

    public List<UIParam> loadParams(FuncType funcType) {
        List<UIParam> params = new ArrayList<>();
        List<UIParam> cacheParams = mParamCache.get(funcType);
        if (cacheParams == null) {
            String json = PreferenceUtils.getString(funcType.getPrefKey());
            if (!TextUtils.isEmpty(json)) {
                Gson gson = new Gson();
                List<UIParam> fileParams = gson.fromJson(json, new TypeToken<List<UIParam>>(){}.getType());
                params.addAll(fileParams);
            }
        } else {
            params.addAll(cacheParams);
        }
        return params;
    }

    public Command getCommand(FuncType funcType) {
        Command command = new Command("", Command.Type.ITU);
        List<UIParam> params = loadParams(funcType);
        StringBuilder sb = new StringBuilder();
        if (!CollectionUtils.isEmpty(params)) {
            switch (funcType) {
                case IFPAN:
                    command.type = Command.Type.IFPAN;
                    sb.append("RMTP:IFANALYSIS");
                    break;
                case FSCAN:
                    command.type = Command.Type.FSCAN;
                    sb.append("RMTP:FSCAN");
                    break;
                case DF:
                    command.type = Command.Type.DF;
                    sb.append("RMTP:DF");
                    break;
                case DISCRETE:
                    sb.append("RMTP:MSCAN");
                    break;
                case DIGIT:
                    sb.append("RMTP:DSCAN");
                    break;
                default:
                    command.type = Command.Type.ITU;
                    sb.append("RMTP:SGLFREQ");
                    break;
            }
            sb.append(String.format(":%s:", DEVICE_ID));
            for (UIParam param : params) {
                sb.append(param.name + ":" + param.value + param.unit + "\n");
            }
        }
        sb.append("#");
        command.command = sb.toString();
        return command;
    }

    public boolean isParamsValid(FuncType funcType) {
        List<UIParam> params = loadParams(funcType);
        if (CollectionUtils.isEmpty(params)) return false;
        for (UIParam param : params) {
            if (!param.isValueValid()) return false;
        }
        return true;
    }
}
