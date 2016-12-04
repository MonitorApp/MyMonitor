package com.outsource.monitor.config;

/**
 * Created by Administrator on 2016/10/23.
 */
public class DeviceConfig {

    public static final int DATA_SEND_INTERVAL = 100;//设备数据发送间隔，单位ms
    public static final float MIN_LEVEL = -40;//最低电平，单位dBμV
    public static final float MAX_LEVEL = 120;//最高电平，单位dBμV
    public static final float MIN_FREQUENCY = 90;//最低频率
    public static final float MAX_FREQUENCY = 160;//最高频率
}
